#!/bin/bash

# Exit immediately on any error
set -e
trap "exit" INT # All abort with ctrl-c

echo "Compiling and testing each MoleculePlugin test-project"
echo "Abort with ctrl-c"

projects=(
  "test-project1-basic"
  "test-project2-segments"
  "test-project3-multi-project"
  "test-project4-cross-full"
  "test-project5-cross-pure"
  "test-project6-mixed"
)

for project in "${projects[@]}"; do
  echo "##########################################################################"
  echo "$project"
  echo "##########################################################################"

  (
    cd "$project"
    sbt "clean; moleculeGen; test"
    #    sbt "clean; moleculePackage; test"
  )
done

./test-project8-migration/test-all-workflows.sh
./src/test/scala/sbtmolecule/migrate/test-migration-flags.sh

echo "======================================================"
echo "✅ DONE testing all MoleculePlugin test projects"
echo "======================================================"
