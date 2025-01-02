#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-with-partitions"
echo "##########################################################################"

cd test-project-with-partitions
if [ -d "lib"  ]; then
    rm -r lib
fi
if [ -d "target"  ]; then
    rm -r target
fi

sbt compile -Dmolecule=true
sbt test

cd ..