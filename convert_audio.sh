#!/usr/bin/env bash
# Converts every file from src_audio/ (mp3/m4a/...) to OGG Vorbis and equalises loudness
# (integrated -18 LUFS, true-peak limited). Output: src/main/resources/assets/frierenmusic/sounds/music/
# Usage: tools/convert_audio.sh        (needs ffmpeg with libvorbis)
set -euo pipefail
cd "$(dirname "$0")/.."
OUT=src/main/resources/assets/frierenmusic/sounds/music
TARGET=-18
mkdir -p "$OUT"
for f in src_audio/*; do
  name=$(basename "$f"); name="${name%.*}"
  measured=$(ffmpeg -hide_banner -nostats -i "$f" -af loudnorm=I=$TARGET:TP=-1.5:LRA=20:print_format=json -f null - 2>&1 \
    | python3 -c "import sys,re,json;t=sys.stdin.read();print(json.loads(re.search(r'\{[^{}]*\"input_i\"[^{}]*\}',t,re.S).group(0))['input_i'])")
  gain=$(python3 -c "print(round($TARGET-($measured),2))")
  echo "$name: measured ${measured} LUFS -> gain ${gain} dB"
  ffmpeg -hide_banner -loglevel error -y -i "$f" -vn -af "volume=${gain}dB,alimiter=limit=0.84:level=disabled" \
    -ac 2 -ar 44100 -c:a libvorbis -q:a 5 "$OUT/$name.ogg"
done

# Regenerate sounds.json from the OGG files (names must start with calm_ / combat_ / boss_)
python3 - <<'PY'
import json, os
d = "src/main/resources/assets/frierenmusic/sounds/music"
out = {}
for f in sorted(os.listdir(d)):
    if f.endswith(".ogg"):
        n = f[:-4]
        out[n] = {"category": "music", "sounds": [{"name": "frierenmusic:music/" + n, "stream": True}]}
json.dump(out, open("src/main/resources/assets/frierenmusic/sounds.json", "w"), indent=2)
print("sounds.json:", len(out), "tracks")
PY
