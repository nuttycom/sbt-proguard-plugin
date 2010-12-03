import sbt._

class ProguardPlugin(info: ProjectInfo) extends PluginProject(info) {
  val proguard = "net.sf.proguard" % "proguard" % "4.4" % "compile"
  val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"

  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.fromFile(System.getProperty("user.home")) / ".ivy2" / ".credentials", log)
}
