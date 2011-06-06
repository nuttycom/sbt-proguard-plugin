##Usage

Requires [SBT](http://simple-build-tool.googlecode.com/). For SBT 0.9.0 and higher, please see siasia's version for XSBT: https://github.com/siasia/xsbt-proguard-plugin.git

To use the plugin in a project, you need to create `project/plugins/Plugins.scala`:

    import sbt._
    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val proguard = "org.scala-tools.sbt" % "sbt-proguard-plugin" % "0.0.5"
    }

and modify your project definition in `project/build/Project`. For example:

    import sbt._
    class Project(info: ProjectInfo) extends DefaultProject(info) with ProguardProject {
      override def proguardOptions = List(
        "-keep class MyClass { myMethod; }"
      )
    }

This will add a `proguard` action which will run Proguard and generate output
in `target/project_vers.min.jar`.

##Examples

If your project is a Scala project, you will need to add the Scala library to
the set of input jar files. Do so as follows:

    override def proguardInJars = super.proguardInJars +++ scalaLibraryPath

If you wish to include all Scala classes in your output (regardless of whether
they are used), use the following option:

    override def proguardOptions = List(
      ...,
      proguardKeepAllScala
    )

If you wish to keep the `main()` entry point of a class, use:

    override def proguardOptions = List(
      ...,
      proguardKeepMain("somepackage.SomeClass")
    )

If you wish to keep everything that is `Serializable`, use:

    override def proguardOptions = List(
      ...,
      proguardKeepLimitedSerializability
    )

By default Proguard will be instructed to include everything except classes
from the Java runtime. To treat additional libraries as external (i.e. to
add them to the list of `-libraryjars` passed to Proguard), do the following:

    val jarToExclude = "lib_extra" / "mylib.jar"
    override def proguardLibraryJars = super.proguardLibraryJars +++ jarToExclude

By default all jar files passed to Proguard (except for the one that contains
your project's classes) are filtered using
`somejar.jar(!META-INF/MANIFEST.MF)`. This is necessary to prevent conflicts
when Proguard generates a single final jar. If you wish to filter other
resources from a jar file, do the following:

    override makeInJarFilter (file :String) = file match {
      case "some-special.jar" => super.makeInJarFilter(file) + ",!images/**"
      case _ => super.makeInJarFilter(file)
    }

The argument to `makeJarFilter` will be the filename of the jar file in
question (minus any path). Note that your project's jar file is always included
without any filtering.

Other customizations are possible, take a look at the source to [ProguardProject](http://github.com/nuttycom/sbt-proguard-plugin/tree/master/src/main/scala/ProguardProject.scala).

##Hacking on the plugin

If you need make modifications to the plugin itself, you can compile and install it locally (you need at least sbt 0.7.x to build it):

    $ git clone git://github.com/nuttycom/sbt-proguard-plugin.git
    $ cd sbt-proguard-plugin
    $ sbt update publish-local

##License

This plugin depends upon ProGuard (http://proguard.sourceforge.net/),
which is licensed under the GNU General Public License version 2.0.
As such, this plugin is distributed under the same license; you are
free to use and modify this work so long as any derivative work complies
with the distribution terms. See LICENSE for additional information.

##Credits

This code is based on work by Jan Berkel for the SBT android plugin.
