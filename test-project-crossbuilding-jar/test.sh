#!/bin/bash

# All abort with ctrl-c
trap "exit" INT


echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-crossbuilding-jar"
echo "--------------------------------------------------------------------------"

cd test-project-crossbuilding-jar

# Note that we add + to compile to compile to both Scala 2.12 and 2.13
sbt clean +compile -Dmolecule=true

# Test against Scala 2.12 and 2.13
sbt +test

cd ..
