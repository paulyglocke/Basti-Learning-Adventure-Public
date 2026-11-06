/* Authored presentation map; English keys adapt the unchanged legacy catalogue, never translated labels. */
(function(root){
'use strict';
const assets={
  "eagle": {
    "path": "VerbMotion/eagle.webp",
    "columns": 4,
    "rows": 2
  },
  "rabbit": {
    "path": "VerbMotion/rabbit.webp",
    "columns": 4,
    "rows": 4
  },
  "dog": {
    "path": "VerbMotion/dog.webp",
    "columns": 4,
    "rows": 4
  },
  "cat": {
    "path": "VerbMotion/cat.webp",
    "columns": 4,
    "rows": 4
  },
  "horse": {
    "path": "VerbMotion/horse.webp",
    "columns": 4,
    "rows": 4
  },
  "duck": {
    "path": "VerbMotion/duck.webp",
    "columns": 4,
    "rows": 2
  },
  "penguin": {
    "path": "VerbMotion/penguin.webp",
    "columns": 4,
    "rows": 2
  },
  "snake": {
    "path": "VerbMotion/snake.webp",
    "columns": 4,
    "rows": 2
  },
  "frog": {
    "path": "VerbMotion/frog.webp",
    "columns": 4,
    "rows": 2
  },
  "monkey": {
    "path": "VerbMotion/monkey.webp",
    "columns": 4,
    "rows": 2
  },
  "cow": {
    "path": "VerbMotion/cow.webp",
    "columns": 4,
    "rows": 2
  },
  "pig": {
    "path": "VerbMotion/pig.webp",
    "columns": 2,
    "rows": 2
  },
  "bee": {
    "path": "VerbMotion/bee.webp",
    "columns": 2,
    "rows": 2
  },
  "owl": {
    "path": "VerbMotion/owl.webp",
    "columns": 2,
    "rows": 2
  },
  "wolf": {
    "path": "VerbMotion/wolf.webp",
    "columns": 4,
    "rows": 2
  },
  "lion": {
    "path": "VerbMotion/lion.webp",
    "columns": 4,
    "rows": 2
  },
  "chicken": {
    "path": "VerbMotion/chicken.webp",
    "columns": 2,
    "rows": 2
  },
  "parrot": {
    "path": "VerbMotion/parrot.webp",
    "columns": 4,
    "rows": 2
  },
  "flamingo": {
    "path": "VerbMotion/flamingo.webp",
    "columns": 2,
    "rows": 2
  },
  "beaver": {
    "path": "VerbMotion/beaver.webp",
    "columns": 4,
    "rows": 2
  },
  "spider": {
    "path": "VerbMotion/spider.webp",
    "columns": 2,
    "rows": 2
  },
  "elephant": {
    "path": "VerbMotion/elephant.webp",
    "columns": 4,
    "rows": 2
  },
  "giraffe": {
    "path": "VerbMotion/giraffe.webp",
    "columns": 2,
    "rows": 2
  },
  "crab": {
    "path": "VerbMotion/crab.webp",
    "columns": 2,
    "rows": 2
  },
  "octopus": {
    "path": "VerbMotion/octopus.webp",
    "columns": 2,
    "rows": 2
  },
  "otter": {
    "path": "VerbMotion/otter.webp",
    "columns": 2,
    "rows": 2
  },
  "bat": {
    "path": "VerbMotion/bat.webp",
    "columns": 2,
    "rows": 2
  },
  "hedgehog": {
    "path": "VerbMotion/hedgehog.webp",
    "columns": 4,
    "rows": 2
  },
  "kangaroo": {
    "path": "VerbMotion/kangaroo.webp",
    "columns": 2,
    "rows": 2
  },
  "tiger": {
    "path": "VerbMotion/tiger.webp",
    "columns": 4,
    "rows": 2
  },
  "seal": {
    "path": "VerbMotion/seal.webp",
    "columns": 2,
    "rows": 2
  },
  "snail": {
    "path": "VerbMotion/snail.webp",
    "columns": 2,
    "rows": 2
  },
  "butterfly": {
    "path": "VerbMotion/butterfly.webp",
    "columns": 2,
    "rows": 2
  },
  "ant": {
    "path": "VerbMotion/ant.webp",
    "columns": 2,
    "rows": 2
  },
  "dragon": {
    "path": "VerbMotion/dragon.webp",
    "columns": 2,
    "rows": 2
  },
  "dinosaur": {
    "path": "VerbMotion/dinosaur.webp",
    "columns": 2,
    "rows": 2
  },
  "sauropod": {
    "path": "VerbMotion/sauropod.webp",
    "columns": 2,
    "rows": 2
  },
  "shark": {
    "path": "VerbMotion/shark.webp",
    "columns": 2,
    "rows": 2
  },
  "croc": {
    "path": "VerbMotion/croc.webp",
    "columns": 4,
    "rows": 2
  }
};
const lessons={
  "fly": {
    "id": "verb.fly",
    "animal": "eagle",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "fly",
    "duration": 1600,
    "still": [
      0,
      3,
      5,
      7
    ]
  },
  "jump": {
    "id": "verb.jump",
    "animal": "rabbit",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "jump",
    "duration": 2600,
    "still": [
      1,
      3,
      5,
      7
    ]
  },
  "hop": {
    "id": "verb.hop",
    "animal": "frog",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "hop",
    "duration": 2200,
    "still": [
      1,
      3,
      4,
      6
    ]
  },
  "swim in the sea": {
    "id": "verb.swim_in_the_sea",
    "animal": "shark",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "swim",
    "duration": 2200,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "dive underwater": {
    "id": "verb.dive_underwater",
    "animal": "penguin",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "dive",
    "duration": 3200,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "climb": {
    "id": "verb.climb",
    "animal": "monkey",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "climb",
    "duration": 3200,
    "still": [
      0,
      2,
      5,
      7
    ]
  },
  "slither": {
    "id": "verb.slither",
    "animal": "snake",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5
    ],
    "mode": "slither",
    "duration": 2400,
    "still": [
      0,
      2,
      4,
      5
    ]
  },
  "gallop": {
    "id": "verb.gallop",
    "animal": "horse",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "run",
    "duration": 1600,
    "still": [
      0,
      2,
      5,
      7
    ]
  },
  "bark": {
    "id": "verb.bark",
    "animal": "dog",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "sound",
    "duration": 1800,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "meow": {
    "id": "verb.meow",
    "animal": "cat",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "sound",
    "duration": 2000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "roar": {
    "id": "verb.roar",
    "animal": "lion",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "sound",
    "duration": 3200,
    "still": [
      0,
      3,
      4,
      7
    ]
  },
  "moo": {
    "id": "verb.moo",
    "animal": "cow",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "sound",
    "duration": 2400,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "oink": {
    "id": "verb.oink",
    "animal": "pig",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "sound",
    "duration": 2000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "quack": {
    "id": "verb.quack",
    "animal": "duck",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "sound",
    "duration": 1800,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "buzz": {
    "id": "verb.buzz",
    "animal": "bee",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "buzz",
    "duration": 900,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "hoot": {
    "id": "verb.hoot",
    "animal": "owl",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "perchSound",
    "duration": 2400,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "howl": {
    "id": "verb.howl",
    "animal": "wolf",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "sound",
    "duration": 3600,
    "still": [
      0,
      3,
      4,
      7
    ]
  },
  "hiss": {
    "id": "verb.hiss",
    "animal": "snake",
    "frames": [
      7,
      6,
      6,
      7
    ],
    "mode": "hiss",
    "duration": 2400,
    "still": [
      7,
      6,
      6,
      7
    ]
  },
  "peck": {
    "id": "verb.peck",
    "animal": "chicken",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "peck",
    "duration": 1800,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "dig": {
    "id": "verb.dig",
    "animal": "rabbit",
    "frames": [
      8,
      9,
      10,
      11
    ],
    "mode": "dig",
    "duration": 2400,
    "still": [
      8,
      9,
      10,
      11
    ]
  },
  "scratch": {
    "id": "verb.scratch",
    "animal": "cat",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "scratch",
    "duration": 2200,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "kick": {
    "id": "verb.kick",
    "animal": "horse",
    "frames": [
      8,
      9,
      10,
      11,
      12,
      13
    ],
    "mode": "kick",
    "duration": 2600,
    "still": [
      8,
      9,
      11,
      13
    ]
  },
  "wag its tail": {
    "id": "verb.wag_its_tail",
    "animal": "dog",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "wag",
    "duration": 1200,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "flap its wings": {
    "id": "verb.flap_its_wings",
    "animal": "parrot",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5
    ],
    "mode": "flap",
    "duration": 1200,
    "still": [
      0,
      2,
      4,
      5
    ]
  },
  "stand on one leg": {
    "id": "verb.stand_on_one_leg",
    "animal": "flamingo",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "balance",
    "duration": 6000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "build a dam": {
    "id": "verb.build_a_dam",
    "animal": "beaver",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "dam",
    "duration": 5000,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "spin a web": {
    "id": "verb.spin_a_web",
    "animal": "spider",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "web",
    "duration": 5000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "use a trunk": {
    "id": "verb.use_a_trunk",
    "animal": "elephant",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "trunk",
    "duration": 3800,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "reach high leaves": {
    "id": "verb.reach_high_leaves",
    "animal": "giraffe",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "leaves",
    "duration": 3200,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "walk sideways": {
    "id": "verb.walk_sideways",
    "animal": "crab",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "sideways",
    "duration": 2200,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "squirt ink": {
    "id": "verb.squirt_ink",
    "animal": "octopus",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "ink",
    "duration": 3400,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "float on its back": {
    "id": "verb.float_on_its_back",
    "animal": "otter",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "float",
    "duration": 6000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "copy sounds": {
    "id": "verb.copy_sounds",
    "animal": "parrot",
    "frames": [
      6,
      6,
      7,
      7
    ],
    "mode": "copy",
    "duration": 4000,
    "still": [
      6,
      6,
      7,
      7
    ]
  },
  "sleep upside down": {
    "id": "verb.sleep_upside_down",
    "animal": "bat",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "hang",
    "duration": 7000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "snap its jaws": {
    "id": "verb.snap_its_jaws",
    "animal": "croc",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "snap",
    "duration": 2800,
    "still": [
      0,
      2,
      4,
      7
    ]
  },
  "stomp": {
    "id": "verb.stomp",
    "animal": "elephant",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "stomp",
    "duration": 2400,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "nibble": {
    "id": "verb.nibble",
    "animal": "rabbit",
    "frames": [
      12,
      13,
      14,
      15
    ],
    "mode": "eat",
    "duration": 2200,
    "still": [
      12,
      13,
      14,
      15
    ]
  },
  "chew wood": {
    "id": "verb.chew_wood",
    "animal": "beaver",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "eat",
    "duration": 2400,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "waddle": {
    "id": "verb.waddle",
    "animal": "penguin",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "waddle",
    "duration": 2800,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "paddle on a pond": {
    "id": "verb.paddle_on_a_pond",
    "animal": "duck",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "paddle",
    "duration": 2400,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "curl into a ball": {
    "id": "verb.curl_into_a_ball",
    "animal": "hedgehog",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "curl",
    "duration": 4000,
    "still": [
      0,
      2,
      4,
      6
    ]
  },
  "carry its baby in a pouch": {
    "id": "verb.carry_its_baby_in_a_pouch",
    "animal": "kangaroo",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "pouch",
    "duration": 6000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "pounce": {
    "id": "verb.pounce",
    "animal": "tiger",
    "frames": [
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7
    ],
    "mode": "pounce",
    "duration": 2800,
    "still": [
      0,
      2,
      5,
      7
    ]
  },
  "graze on grass": {
    "id": "verb.graze_on_grass",
    "animal": "cow",
    "frames": [
      4,
      5,
      6,
      7
    ],
    "mode": "graze",
    "duration": 3200,
    "still": [
      4,
      5,
      6,
      7
    ]
  },
  "clap its flippers": {
    "id": "verb.clap_its_flippers",
    "animal": "seal",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "clap",
    "duration": 1600,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "chase mice": {
    "id": "verb.chase_mice",
    "animal": "cat",
    "frames": [
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15
    ],
    "mode": "chase",
    "duration": 2400,
    "still": [
      8,
      10,
      13,
      15
    ]
  },
  "fetch a ball": {
    "id": "verb.fetch_a_ball",
    "animal": "dog",
    "frames": [
      8,
      9,
      10,
      11,
      12,
      13,
      14,
      15
    ],
    "mode": "fetch",
    "duration": 5400,
    "still": [
      8,
      14,
      11,
      15
    ]
  },
  "crawl slowly": {
    "id": "verb.crawl_slowly",
    "animal": "snail",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "crawl",
    "duration": 6000,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "flutter": {
    "id": "verb.flutter",
    "animal": "butterfly",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "flutter",
    "duration": 1200,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "carry food": {
    "id": "verb.carry_food",
    "animal": "ant",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "carry",
    "duration": 3400,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "breathe fire": {
    "id": "verb.breathe_fire",
    "animal": "dragon",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "fire",
    "duration": 3200,
    "still": [
      0,
      1,
      2,
      3
    ]
  },
  "stomp like a dinosaur": {
    "id": "verb.stomp_like_a_dinosaur",
    "animal": "dinosaur",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "stomp",
    "duration": 2600,
    "still": [
      0,
      1,
      3,
      3
    ]
  },
  "stretch its long neck": {
    "id": "verb.stretch_its_long_neck",
    "animal": "sauropod",
    "frames": [
      0,
      1,
      2,
      3
    ],
    "mode": "neck",
    "duration": 3600,
    "still": [
      0,
      1,
      2,
      3
    ]
  }
};
const content=Object.freeze({assets,lessons});
root.VerbMotionContent=content;
if(typeof module!=='undefined'&&module.exports)module.exports=content;
})(typeof globalThis!=='undefined'?globalThis:this);
