#!/usr/bin/env python3
"""Normalize a built-in-generated atlas and make a tiny library thumbnail.

No image API calls or creative editing. Original generated PNG remains untouched.
FFmpeg normalizes the grid; cwebp preserves alpha with lossless encoding.
"""
import argparse
import json
import pathlib
import subprocess
import tempfile

ROOT=pathlib.Path(__file__).resolve().parents[1]
DEST=ROOT/'app/src/main/assets/VerbMotion'

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--id',required=True)
    p.add_argument('--source',type=pathlib.Path,required=True)
    p.add_argument('--columns',type=int,required=True)
    p.add_argument('--rows',type=int,required=True)
    args=p.parse_args()
    if not args.id.isalpha() or args.columns not in (2,4) or args.rows not in (2,4):
        p.error('Expected a known animal/grid.')
    if not args.source.is_file():p.error('Generated source is missing.')
    DEST.mkdir(parents=True,exist_ok=True)
    width,height=args.columns*384,args.rows*384
    atlas=DEST/(args.id+'.webp')
    dimensions=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_entries','stream=width,height',
                                                  '-of','json',str(args.source)]))['streams'][0]
    sw,sh=dimensions['width'],dimensions['height']
    raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(args.source),'-f','rawvideo','-pix_fmt','rgba','-'])
    cw,ch=sw//args.columns,sh//args.rows
    # Alpha is inspected only to measure a shared crop; no mask or painted pixels
    # are changed. A common crop preserves registration between poses.
    bounds=[]
    for row in range(args.rows):
        for col in range(args.columns):
            left,top,right,bottom=cw,ch,0,0
            for y in range(ch):
                start=((row*ch+y)*sw+col*cw)*4+3
                opaque=[x for x,a in enumerate(raw[start:start+cw*4:4]) if a>96]
                if len(opaque)>2:
                    left=min(left,opaque[0]);right=max(right,opaque[-1]+1)
                    top=min(top,y);bottom=max(bottom,y+1)
            if right<=left or bottom<=top:raise ValueError('An atlas cell is empty')
            bounds.append((left,top,right,bottom))
    left=max(0,min(b[0] for b in bounds)-3);top=max(0,min(b[1] for b in bounds)-3)
    right=min(cw,max(b[2] for b in bounds)+3);bottom=min(ch,max(b[3] for b in bounds)+3)
    cropwidth,cropheight=right-left,bottom-top
    with tempfile.TemporaryDirectory(prefix='basti-verb-atlas-') as scratch:
        normalized=pathlib.Path(scratch)/'normalized.png'
        # Some generated sheets use rectangular cells. Fit each cell separately:
        # stretching the whole sheet would distort the animal's proportions.
        count=args.columns*args.rows
        filters=['[0:v]split='+str(count)+''.join(f'[s{i}]' for i in range(count))]
        for i in range(count):
            col,row=i%args.columns,i//args.columns
            filters.append(f'[s{i}]crop={cropwidth}:{cropheight}:{col*cw+left}:{row*ch+top},'
                           'scale=320:320:force_original_aspect_ratio=decrease:flags=lanczos,'
                           f'pad=384:384:(ow-iw)/2:326-ih:color=0x00000000,setsar=1[c{i}]')
        layout='|'.join(f'{i%args.columns*384}_{i//args.columns*384}' for i in range(count))
        filters.append(''.join(f'[c{i}]' for i in range(count))+f'xstack=inputs={count}:layout={layout}:fill=0x00000000[out]')
        subprocess.run(['ffmpeg','-y','-v','error','-i',str(args.source),'-filter_complex',';'.join(filters),
                        '-map','[out]','-frames:v','1',str(normalized)],check=True)
        subprocess.run(['cwebp','-quiet','-lossless',str(normalized),'-o',str(atlas)],check=True)
        subprocess.run(['cwebp','-quiet','-lossless','-crop','0','0','384','384','-resize','192','192',
                        str(normalized),'-o',str(DEST/(args.id+'-thumb.webp'))],check=True)
    print(json.dumps({'id':args.id,'path':str(atlas.relative_to(ROOT)), 'width':width,'height':height,'bytes':atlas.stat().st_size}))

if __name__=='__main__':main()
