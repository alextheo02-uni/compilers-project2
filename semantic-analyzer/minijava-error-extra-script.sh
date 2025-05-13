#!/bin/bash

DIRECTORY="../minijava-examples-new/minijava-error-extra"

for file in "$DIRECTORY"/*; do
    echo "Performing semantic analysis on: $file"
    java Main "$file"
    echo ""
done