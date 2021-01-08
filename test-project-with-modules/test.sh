#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-with-modules"
echo "--------------------------------------------------------------------------"

cd test-project-with-modules
sbt compile -Dmolecule=true
sbt test
cd ..