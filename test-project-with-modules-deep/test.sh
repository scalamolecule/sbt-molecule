#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-with-modules-deep"
echo "##########################################################################"

cd test-project-with-modules-deep
sbt test -Dmolecule=true
cd ..