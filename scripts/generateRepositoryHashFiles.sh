#!/bin/bash

# Helper script to generate hash files for Maven artifacts. For example useful when manually
# migrating the Maven repository but only the jar files are available.

# Ensure a directory is specified
if [ -z "$1" ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

dir="$1"

# Find all files (excluding directories) and process them
find "$dir" -type f | while read -r file; do
    # Skip hash files to prevent recursive hashing
    if [[ "$file" =~ \.(md5|sha1|sha256|sha512)$ ]]; then
        continue
    fi

    # Compute hashes and store in respective files without newline
    md5sum "$file" | awk '{printf "%s", $1}' > "$file.md5"
    echo "Generated: $file.md5"

    sha1sum "$file" | awk '{printf "%s", $1}' > "$file.sha1"
    echo "Generated: $file.sha1"

    sha256sum "$file" | awk '{printf "%s", $1}' > "$file.sha256"
    echo "Generated: $file.sha256"

    sha512sum "$file" | awk '{printf "%s", $1}' > "$file.sha512"
    echo "Generated: $file.sha512"
done

echo "Hashing complete."
