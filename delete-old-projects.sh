#!/bin/bash

# רשימת הפרויקטים למחיקה
projects_to_delete=(
  "graphic-outlook-414411"
  "realestateapp-41f15"
  "rising-capsule-414411"
  "sound-bit-414410"
  "the-respect-414410"
)

echo "⚠️ נתחיל בתהליך מחיקת פרויקטים לא רלוונטיים..."
echo "---------------------------------------------"

for project_id in "${projects_to_delete[@]}"; do
  echo "🗑️ עומד למחוק את הפרויקט: $project_id"
  read -p "האם אתה בטוח? (y/n): " confirm
  if [[ "$confirm" == "y" ]]; then
    gcloud projects delete "$project_id"
  else
    echo "❌ מדלג על: $project_id"
  fi
  echo "---------------------------------------------"
done

echo "✅ תהליך המחיקה הסתיים."

