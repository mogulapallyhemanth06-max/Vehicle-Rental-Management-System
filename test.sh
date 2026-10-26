#!/bin/sh
# Compile and run the dependency-free service tests.
set -e
cd "$(dirname "$0")"
rm -rf out-test
mkdir -p out-test
find src tests -name '*.java' > /tmp/vehicle-test-sources.txt
javac --release 17 -d out-test @/tmp/vehicle-test-sources.txt
java -cp out-test com.rental.RentalServiceTest
