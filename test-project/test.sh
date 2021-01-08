#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "------------------------------------------------------"
echo "Compiling and testing: test-project"
echo "------------------------------------------------------"

cd test-project
sbt clean compile -Dmolecule=true
sbt test
cd ..

