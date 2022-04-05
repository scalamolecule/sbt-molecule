#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-crossbuilding-src"
echo "##########################################################################"

cd test-project-crossbuilding-src
if [ -d "lib"  ]; then
    rm -r lib
fi
if [ -d "target"  ]; then
    rm -r target
fi
sbt +test -Dmolecule=true
cd ..