#!/bin/bash

# All abort with ctrl-c
trap "exit" INT

echo "======================================================"
echo "Compiling and testing each MoleculePlugin test-project"
echo "This will give you time to grab a coffee..."
echo "Abort with ctrl-c"
echo "======================================================"

sh test-project/test.sh
sh test-project-crossbuilding-jar/test.sh
sh test-project-crossbuilding-src/test.sh
sh test-project-crossbuilding-src-aggr/test.sh
sh test-project-lower/test.sh
sh test-project-scalajs-full/test.sh
sh test-project-scalajs-pure/test.sh
sh test-project-with-modules/test.sh
sh test-project-with-modules-deep/test.sh
sh test-project-with-partitions/test.sh
sh test-project-with-partitions-lower/test.sh

echo "======================================================"
echo "DONE testing all MoleculePlugin projects"
echo "======================================================"
