#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-lower"
echo "--------------------------------------------------------------------------"

cd test-project-lower
sbt compile test -Dmolecule=true
cd ..