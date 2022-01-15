#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-with-partitions"
echo "##########################################################################"

cd test-project-with-partitions
sbt test -Dmolecule=true
cd ..