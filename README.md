# sbt-molecule
SBT plugin to generate and package Molecule DSL boilerplate code.

## 1. Setup

For sbt 0.13.6+ add sbt-molecule as a dependency in `project/buildinfo.sbt`:

```scala
addSbtPlugin("org.scalamolecule" % "sbt-molecule" % "0.1.0")
```

Add the following in your `build.sbt`:

```scala
lazy val moleculeDemo = project.in(file("demo"))
  .enablePlugins(MoleculePlugin)
  .settings(
    moleculeSchemas := Seq("demo")
  )
```

### Tell sbt about your Schema definition files

A Schema definition file contains a plain Scala object where you define 
partitions/namespaces/attributes of your Datomic database. Molecule use the information
defined there to create all the boilerplate code needed to use Molecule in your code.

You can have several Schema definition files in a project and each definition file will
define a single database. This is useful if you for instance want to experiment with various
database designs during development.

Schema definiton files should reside in directories named `schema` anywhere in your source code.

Use the `moleculeSchemas` sbt settings key to list the directories in your project source
code that contains your `schema` directories.

Say you have a project `demo` and a single Schema definition file `YourDomainDefinition.scala`
defining your database:

![](img/dirs1.png)

Then you simply add `moleculeSchemas := Seq("demo")` as we saw above.

In the main Molecule project's examples module we have several Schema definition files:

![](img/dirs2.png)

And we then list the paths to those like this in our `build.sbt`:

```scala
moleculeSchemas := Seq(
  "molecule/examples/dayOfDatomic",
  "molecule/examples/graph",
  "molecule/examples/mbrainz",
  "molecule/examples/seattle"
)
```

## 2. Compile

Now compile your project from the terminal

```
> cd yourProjectRoot
> sbt compile
```

The MoleculePlugin will now automatically as part of the compilation process do 5 things:

1. Generate Molecule boilerplate dsl source code files (in the `src_managed` directory in target)
2. Generate a schema file with the necessary code to transact the Datomic schema  
3. Compile the generated code
4. Package both the source code and compiled classes into two `jar`s and place them in the `lib` directory of your module
5. Remove the generated source code and compiled classes

The MoleculePlugin create the `jars` so that you can use the boilerplate code without having to recompile any 
generated boilerplate code each time you recompile your project. In our demo example two jars are created:

![](img/jars.png)


## 3. Use Molecule!

Having the necessary Molecule boilerplate code we can now create our Datomic database with our new Schema:

```scala
implicit val conn = recreateDbFrom(demo.schema.YourDomainSchema)
```

... and start using Molecule

```scala
import demo.dsl.yourDomain._

// Insert data
val companyId = Person.name("John").age(26).gender("male").add.eid

// Retrieve data
val (person, age, gender) = Person.name.age.gender.one
```

Read more on [scalamolecule.org](http://www.scalamolecule.org)