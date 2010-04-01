import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}
import io.Source
import java.io._
import sbt._
import Process._

object ProguardProject {
  val DefaultMinJarName = defaultJarBaseName + ".min.jar"
}

abstract class ProguardProject(info: ProjectInfo) extends DefaultProject(info) {
  import ProguardProject._
  
  def minJarName = DefaultMinJarName
  def minJarPath = outputPath / minJarName

  def proguardOption = ""
  def proguardInJars = runClasspath --- proguardExclude
  def proguardExclude = libraryJarPath +++ mainCompilePath +++ mainResourcesPath +++ managedClasspath(Configurations.Provided)
  def libraryJarPath: PathFinder = ""
  
  /** Forward compatibility with sbt 0.6+ Scala build versions */
  def scalaLibraryJar = try {
    type xsbtProject = { def buildScalaInstance: { def libraryJar: File } }
    this.asInstanceOf[xsbtProject].buildScalaInstance.libraryJar
  } catch {
    case e: NoSuchMethodException => FileUtilities.scalaLibraryJar
  }

  lazy val proguard = proguardAction
  def proguardAction = proguardTask dependsOn(compile) describedAs("Optimize class files.")
  def proguardTask = task { 
    val args = "-injars" ::  mainCompilePath.absolutePath+File.pathSeparator+
                             scalaLibraryJar.getAbsolutePath+"(!META-INF/MANIFEST.MF,!library.properties)"+
                             proguardInJars.getPaths.map(_+"(!META-INF/MANIFEST.MF)").mkString(File.pathSeparator, File.pathSeparator, "") ::                             
               "-outjars" :: minJarPath.absolutePath ::
               "-libraryjars" :: libraryJarPath.getPaths.mkString(File.pathSeparator) :: 
               "-dontwarn" :: "-dontoptimize" :: "-dontobfuscate" :: 
               proguardOption :: Nil
    
    val config = new ProGuardConfiguration
    new ConfigurationParser(args.toArray[String], info.projectPath.asFile).parse(config)    
    new ProGuard(config).execute
    None
  }
}
