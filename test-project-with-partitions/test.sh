#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-with-partitions"
echo "--------------------------------------------------------------------------"

cd test-project-with-partitions
sbt compile test -Dmolecule=true
cd ..