#!/bin/sh
# Run the desktop app (compiles first if needed).
set -e
cd "$(dirname "$0")"
if [ ! -d out ]; then
  ./compile.sh
fi
java -cp out com.rental.Main
