#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-with-partitions-lower"
echo "##########################################################################"

cd test-project-with-partitions-lower
sbt test -Dmolecule=true
cd ..