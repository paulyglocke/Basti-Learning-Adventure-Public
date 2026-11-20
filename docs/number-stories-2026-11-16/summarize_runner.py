#!/usr/bin/env python3
"""Validate a completed direct AndroidJUnitRunner log; not a Gradle XML report."""
import json,re,sys
from pathlib import Path
source=Path(sys.argv[1]);expected=int(sys.argv[2]);raw=source.read_text()
codes=[int(n) for n in re.findall(r'^INSTRUMENTATION_STATUS_CODE: (-?\d+)$',raw,re.M)]
assert len(codes)==expected*2 and codes.count(0)==expected and codes.count(1)==expected,codes
assert all(n in (0,1) for n in codes),codes
assert re.search(rf'OK \({expected} tests?\)',raw),raw[-1000:]
assert re.search(r'^INSTRUMENTATION_CODE: -1$',raw,re.M),raw[-1000:]
cases=[];current={}
for line in raw.splitlines():
 if line.startswith('INSTRUMENTATION_STATUS: class='):current['class']=line.split('=',1)[1]
 if line.startswith('INSTRUMENTATION_STATUS: test='):current['test']=line.split('=',1)[1]
 if line=='INSTRUMENTATION_STATUS_CODE: 0':cases.append(dict(current,outcome='passed'))
assert len(cases)==expected
summary={'source':source.name,'format':'Derived from direct AndroidJUnitRunner raw text; not Gradle XML','tests':expected,'failures':0,'errors':0,'skipped':0,'instrumentationCode':-1,'cases':cases}
source.with_suffix('.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps({k:v for k,v in summary.items() if k!='cases'}))
