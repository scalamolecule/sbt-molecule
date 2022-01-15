#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project"
echo "##########################################################################"

cd test-project
sbt test -Dmolecule=true
cd ..

