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
  def rtJarPath = Path.fromFile(System.getProperty("java.home")) / "lib" / "rt.jar"


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
  def proguardInJars      = ((compileClasspath +++ allDependencyJars) ** "*.jar") --- jarPath --- proguardExclude
  def proguardExclude     = proguardLibraryJars +++ mainCompilePath +++ mainResourcesPath +++ managedClasspath(Configurations.Provided) 
  def proguardLibraryJars = rtJarPath

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

  def proguardInJarsArg = {
    val inPaths = proguardInJars.get.foldLeft(Map.empty[String, Path])((m, p) => m + (p.asFile.getName -> p)).values
    "-injars" :: (List(jarPath.absolutePath).elements ++ inPaths.map(_.absolutePath+"(!META-INF/MANIFEST.MF)")).mkString(File.pathSeparator) :: Nil
  }

  def proguardOutJarsArg = "-outjars" :: minJarPath.absolutePath :: Nil

  def proguardLibJarsArg = {
    println(proguardLibraryJars.get)
    val libPaths = proguardLibraryJars.get.foldLeft(Map.empty[String, Path])((m, p) => m + (p.asFile.getName -> p)).values
    if (libPaths.hasNext) "-libraryjars" :: libPaths.mkString(File.pathSeparator) :: Nil else Nil
  }

  def proguardDefaultArgs = "-dontwarn" :: "-dontoptimize" :: "-dontobfuscate" :: proguardOptions 

  lazy val proguard = proguardAction
  def proguardAction = proguardTask dependsOn(`package`) describedAs(ProguardProject.Description)
  def proguardTask = task {
    val args = proguardInJarsArg ::: proguardOutJarsArg ::: proguardLibJarsArg ::: proguardDefaultArgs
    val config = new ProGuardConfiguration
    new ConfigurationParser(args.toArray[String], info.projectPath.asFile).parse(config)
    new ProGuard(config).execute
    None
  }
}

// vim: set ts=4 sw=4 et:
