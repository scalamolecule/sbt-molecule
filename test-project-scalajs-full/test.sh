#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-scalajs-full"
echo "--------------------------------------------------------------------------"

cd test-project-scalajs-full
sbt clean compile -Dmolecule=true
sbt fooJVM/test
sbt fooJS/test
cd ..