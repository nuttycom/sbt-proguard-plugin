##Usage

Requires [SBT](http://simple-build-tool.googlecode.com/).

To use the plugin in a project, you need to create `project/plugins/Plugins.scala`:

    import sbt._
    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val proguard = "org.scala-tools.sbt" % "sbt-proguard-plugin" % "0.0.+"
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

The plugin provides a few standard options to make your life easier. If you
wish to keep all Scala classes, use the following:

    override def proguardOptions = List(
      ...,
      proguardKeepAllScala
    )

If you wish to keep your `main()` entry points, use:

    override def proguardOptions = List(
      ...,
      proguardKeepMains
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
