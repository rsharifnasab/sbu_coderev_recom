#!/usr/bin/env bash

set -euo pipefail

# Check if an argument is provided
if [ $# -ne 2 ]; then
  echo "Usage: $0 <input_directory> <output_directory>"
  exit 1
fi

# Set the root directory from the argument
INP_DIR="$1"
OUTPUT_DIR="$2"

mkdir "$OUTPUT_DIR"

# Check if the provided directory exists
if [ ! -d "$INP_DIR" ]; then
  echo "Error: Directory '$INP_DIR' does not exist."
  exit 1
fi

# Function to process each CSV file
process_file() {
  local file=$1
  local subdir=$2

  # Extract the base name without extension
  base_name=$(basename "$file" .csv)

  # Create the new directory
  mkdir -p "$OUTPUT_DIR/$base_name/$subdir"

  # Move the file to the new location
  mv "$file" "$OUTPUT_DIR/$base_name/$subdir/"
}

# Find all subdirectories in the root directory
subdirs=$(find "$INP_DIR" -maxdepth 1 -type d -not -path "$INP_DIR")

# Process files in each subdirectory
for subdir in $subdirs; do
  subdir_name=$(basename "$subdir")

  # Process CSV files in the current subdirectory
  for file in "$subdir"/*.csv; do
    if [ -f "$file" ]; then
      process_file "$file" "$subdir_name"
    fi
  done
  echo "Processing $subdir_name complete"
done

echo "Directory restructuring complete."
