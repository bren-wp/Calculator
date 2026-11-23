#!/usr/bin/env bash
set -euo pipefail
APP="com.brendigo.calculator"
mkdir -p visual-qa/captures
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell wm size 432x960
adb shell wm density 160
adb shell settings put system font_scale 1.0
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
captured=0
while IFS='|' read -r filename route; do
  if [[ -z "$filename" || "$filename" == \#* ]]; then continue; fi
  adb shell am force-stop "$APP" </dev/null || true
  adb shell am start -W -n "$APP/.MainActivity" --es preview_screen "$route" </dev/null >/dev/null
  sleep 1
  adb exec-out screencap -p </dev/null > "visual-qa/captures/${filename}.png"
  captured=$((captured+1))
  echo "Captured ${filename}"
done <<'SCREENS'
01_standard_calculator|Standard Calculator
02_scientific_calculator|Scientific Calculator
03_programmer_calculator|Programmer Calculator
04_history|History
05_favorites|Favorites
06_tools_hub|Tools
07_unit_converter|Unit Converter
08_currency_converter|Currency Converter
09_percentage_calculator|Percentage
10_tip_and_split|Tip & Split
11_loan_planner|Loan Planner
12_discount_calculator|Discount
13_vat_and_tax|VAT & Tax
14_date_calculator|Date Calculator
15_graphing|Graphing
16_settings|Settings
17_appearance_themes|Appearance
18_calculation_preferences|Calculation Preferences
19_quick_access|Quick Access
20_privacy_data|Privacy & Data
21_about|About
22_empty_history|Empty History
23_error_state|Error State
24_light_theme_standard|Light Theme
25_amoled_theme_standard|AMOLED Theme
26_widget_previews|Widgets
SCREENS

if [ "$captured" -ne 26 ]; then
  echo "Expected 26 separate screen captures, received $captured" >&2
  exit 1
fi
