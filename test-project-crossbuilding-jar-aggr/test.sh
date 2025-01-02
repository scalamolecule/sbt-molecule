#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-crossbuilding-jar-aggr"
echo "##########################################################################"

cd test-project-crossbuilding-jar-aggr
if [ -d "app/lib"  ]; then
    rm -r app/lib
fi
if [ -d "app/target"  ]; then
    rm -r app/target
fi

sbt +compile -Dmolecule=true
sbt +test

cd ..