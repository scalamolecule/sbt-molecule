#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "Compiling and testing each MoleculePlugin test-project"
echo "This will give you time to grab a coffee..."
echo "Abort with ctrl-c"

sh test-project-2/test.sh
sh test-project-3/test.sh
sh test-project-crossbuilding-jar/test.sh
sh test-project-crossbuilding-src/test.sh
sh test-project-crossbuilding-src-aggr/test.sh
sh test-project-with-modules/test.sh
sh test-project-with-modules-deep/test.sh
sh test-project-with-partitions/test.sh

echo "======================================================"
echo "DONE testing all MoleculePlugin projects"
echo "======================================================"
