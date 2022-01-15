#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-with-modules"
echo "##########################################################################"

cd test-project-with-modules
sbt test -Dmolecule=true
cd ..