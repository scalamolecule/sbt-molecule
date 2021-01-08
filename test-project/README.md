Compile and run test:

    sbt clean compile -Dmolecule=true
    sbt test

Note that the first command will generate molecule boilerplate code since the -Dmolecule flag is set to true. When running the test command, we avoid generating the boilerplate code again since the flag is now off by default.