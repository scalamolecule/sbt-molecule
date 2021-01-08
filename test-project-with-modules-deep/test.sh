#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-with-modules-deep"
echo "--------------------------------------------------------------------------"

cd test-project-with-modules-deep
sbt compile -Dmolecule=true
sbt test
cd ..