#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-lower"
echo "##########################################################################"

cd test-project-lower
sbt test -Dmolecule=true
cd ..