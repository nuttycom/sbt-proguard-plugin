import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}
import java.io._
import sbt._
import Process._

object ProguardProject {
  val Description = "Aggregate and minimize the project's files and all dependencies into a single jar."
}

abstract class ProguardProject(info: ProjectInfo) extends DefaultProject(info) {
  def minJarName = artifactBaseName + ".min.jar"
  def minJarPath = outputPath / minJarName

  def proguardOptions: List[String] = Nil

  // Forward compatibility with sbt 0.6+ Scala build versions 
  def scalaLibraryJar = try {
    type xsbtProject = { def buildScalaInstance: { def libraryJar: File } }
    this.asInstanceOf[xsbtProject].buildScalaInstance.libraryJar
  } catch {
    case e: NoSuchMethodException => FileUtilities.scalaLibraryJar
  }

  def allDependencyJars = Path.lazyPathFinder { 
    topologicalSort.flatMap { 
      case p: ScalaPaths => p.jarPath.getFiles.map(Path.fromFile); 
      case _ => Set() 
    } 
  }

  //def proguardInJars = runClasspath --- proguardExclude
  def proguardInJars = ((compileClasspath +++ allDependencyJars) ** "*.jar") --- proguardExclude
  def proguardExclude = libraryJarPath +++ mainCompilePath +++ mainResourcesPath +++ managedClasspath(Configurations.Provided) 
  def libraryJarPath = Path.emptyPathFinder 

  def proguardKeepLimitedSerializability = """
    -keepclassmembers class * implements java.io.Serializable {
        static long serialVersionUID;
        private void writeObject(java.io.ObjectOutputStream);
        private void readObject(java.io.ObjectInputStream);
        java.lang.Object writeReplace();
        java.lang.Object readResolve();
    }
  """

  def proguardKeepSerializability = "-keep class * implements java.io.Serializable { *; }"

  def proguardKeepAllScala = "keep class scala.** { *; }"

  lazy val proguard = proguardAction
  def proguardAction = proguardTask dependsOn(`package`) describedAs(ProguardProject.Description)
  def proguardTask = task {
    val inPaths = proguardInJars.get.foldLeft(Map.empty[String, Path])((m, p) => m + (p.asFile.getName -> p)).values

    val args = "-injars" :: inPaths.map(_.absolutePath+"(!META-INF/MANIFEST.MF)").mkString(File.pathSeparator) ::
               "-outjars" :: minJarPath.absolutePath ::
               "-dontwarn" :: "-dontoptimize" :: "-dontobfuscate" :: 
               proguardOptions 

    val config = new ProGuardConfiguration
    new ConfigurationParser(args.toArray[String], info.projectPath.asFile).parse(config)
    new ProGuard(config).execute
    None
  }
}

// vim: set ts=4 sw=4 et:
