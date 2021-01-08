#!/bin/bash

# All abort with ctrl-c
trap "exit" INT


echo "--------------------------------------------------------------------------"
echo "Compiling and testing: test-project-crossbuilding-src-aggr"
echo "--------------------------------------------------------------------------"

cd test-project-crossbuilding-src

# When testing from sbt it seems necessary to take one scala version at a time
sbt ++2.12.12 compile test -Dmolecule=true
sbt ++2.13.4 compile test -Dmolecule=true

cd ..