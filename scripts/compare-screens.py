#!/usr/bin/env python3
"""Generate an objective, non-gating image-difference report from emulator screenshots."""
import csv
import io
from pathlib import Path
from PIL import Image, ImageChops, ImageStat, ImageEnhance
import cairosvg

captures = Path("visual-qa/captures")
originals = Path("branding/screens")
diffs = Path("visual-qa/differences")
diffs.mkdir(parents=True, exist_ok=True)
rows = []
for image_path in sorted(captures.glob("*.png")):
    name = image_path.stem
    original_path = originals / (name + ".svg")
    if not original_path.exists():
        continue
    capture = Image.open(image_path).convert("RGB")
    golden_bytes = cairosvg.svg2png(url=str(original_path),
        output_width=capture.width, output_height=capture.height)
    reference = Image.open(io.BytesIO(golden_bytes)).convert("RGB")
    delta = ImageChops.difference(reference,capture)
    stats = ImageStat.Stat(delta)
    mae = round(sum(stats.mean) / len(stats.mean), 3)
    changed = sum(1 for value in delta.convert("L").getdata() if value > 15)
    pixel_ratio = round(100 * changed / (capture.width * capture.height), 2)
    ImageEnhance.Contrast(delta).enhance(3).save(diffs/(name + ".png"))
    rows.append({"screen":name, "mean_absolute_error":mae,
        "pixels_above_15_percent":pixel_ratio, "image_width":capture.width,
        "image_height":capture.height})

report = Path("visual-qa/report.csv")
with report.open("w",newline="") as file:
    writer = csv.DictWriter(file,fieldnames=list(rows[0]) if rows else ["screen"])
    writer.writeheader()
    writer.writerows(rows)
print(f"Visual QA: compared {len(rows)} screens. Results in {report}.")
for entry in rows:
    print(f"{entry['screen']}: MAE={entry['mean_absolute_error']}, >15={entry['pixels_above_15_percent']}%")
