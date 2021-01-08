#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-with-partitions-lower"
echo "--------------------------------------------------------------------------"

cd test-project-with-partitions-lower
sbt compile test -Dmolecule=true
cd ..