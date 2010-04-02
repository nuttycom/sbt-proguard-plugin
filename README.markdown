##Usage

Requires [sbt](http://simple-build-tool.googlecode.com/)

To use the plugin in a project, you just need to create project/plugins/Plugins.scala:

    import sbt._
    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val proguard = "org.scala-tools.sbt" % "sbt-proguard-plugin" % "0.0.1"
    }

and make the project definition in project/build/Project (for example):

    import sbt._
    import java.io.File

    class Project(info: ProjectInfo) extends ProguardProject(info) {
    }

##Hacking on the plugin

If you need make modifications to the plugin itself, you can compile and install it locally (you need at least sbt 0.7.x to build it):

    $ git clone git://github.com/nuttycom/sbt-proguard-plugin.git
    $ cd sbt-proguard-plugin
    $ sbt publish-local    

##License

This plugin depends upon ProGuard (http://proguard.sourceforge.net/), 
which is licensed under the GNU General Public License version 2.0.
As such, this plugin is distributed under the same license; you are 
free to use and modify this work so long as any derivative work complies 
with the distribution terms. See LICENSE for additional information.

##Credits

This code is based on work by Jan Berkel for the SBT android plugin
