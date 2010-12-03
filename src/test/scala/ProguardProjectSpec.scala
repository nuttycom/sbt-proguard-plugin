import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ProguardProjectSpec extends FlatSpec with ShouldMatchers {

  "ProguardProject" should "locateAnActualRtJarFile" in {
    ProguardProject.rtJarPath.asFile.exists() should equal (true)
  }

}

