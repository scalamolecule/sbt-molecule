#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-crossbuilding-jar"
echo "##########################################################################"

cd test-project-crossbuilding-jar
sbt +test -Dmolecule=true
cd ..
