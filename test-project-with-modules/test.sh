#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "##########################################################################"
echo "test-project-with-modules"
echo "##########################################################################"

cd test-project-with-modules
if [ -d "app/lib"  ]; then
    rm -r app/lib
fi
if [ -d "app/target"  ]; then
    rm -r app/target
fi

# Create jars with boilerplate code and delete generated sources
sbt compile -Dmolecule=true

# Test with boilerplate code in jars
sbt test

cd ..