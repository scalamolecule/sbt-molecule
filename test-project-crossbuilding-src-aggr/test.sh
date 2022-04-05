#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-crossbuilding-src-aggr"
echo "##########################################################################"

cd test-project-crossbuilding-src
if [ -d "app/lib"  ]; then
    rm -r app/lib
fi
if [ -d "app/target"  ]; then
    rm -r app/target
fi
sbt +test -Dmolecule=true
cd ..