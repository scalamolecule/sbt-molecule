#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-crossbuilding-jar"
echo "##########################################################################"

cd test-project-crossbuilding-jar
if [ -d "lib"  ]; then
    rm -r lib
fi
if [ -d "target"  ]; then
    rm -r target
fi

sbt +compile -Dmolecule=true
sbt +test

cd ..
