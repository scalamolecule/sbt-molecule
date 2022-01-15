#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-crossbuilding-src"
echo "##########################################################################"

cd test-project-crossbuilding-src
sbt +test -Dmolecule=true
cd ..