#!/bin/bash

DIRECTORY="../minijava-examples-new"

for file in "$DIRECTORY"/*; do
    if [ -d "$file" ]; then
        continue
    fi
    
    echo "Performing semantic analysis on: $file"
    java Main "$file"
    echo ""
done