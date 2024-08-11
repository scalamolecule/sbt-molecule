#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-2"
echo "##########################################################################"

cd test-project-2
if [ -d "lib"  ]; then
    rm -r lib
fi
if [ -d "target"  ]; then
    rm -r target
fi
sbt test -Dmolecule=true
cd ..

