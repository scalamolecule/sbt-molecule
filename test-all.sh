#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "Compiling and testing each MoleculePlugin test-project"
echo "This will give you time to grab a coffee..."
echo "Abort with ctrl-c"

sh test-project/test.sh
sh test-project-with-modules/test.sh
sh test-project-with-modules-deep/test.sh
sh test-project-with-segments/test.sh

echo "======================================================"
echo "DONE testing all MoleculePlugin projects"
echo "======================================================"
