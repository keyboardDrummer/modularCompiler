package core.parsers

import deltas.json.JsonLanguage
import org.scalatest.FunSuite
import util.SourceUtils

class PerformanceTest extends FunSuite {

  test("performance") {
    val source = SourceUtils.getTestFileContents("AutoScalingMultiAZWithNotifications.json")
    val json = JsonLanguage.language
    val tenTimesSource = s"[${1.to(10).map(_ => source).reduce((a,b) => a + "," + b)}]"

    val timeA = System.currentTimeMillis()
    for(_ <- 1.to(10)) {
      json.compileString(source)
    }

    val timeB = System.currentTimeMillis()
    json.compileString(tenTimesSource)

    val timeC = System.currentTimeMillis()

    val tenSingleRuns = timeB - timeA
    val oneTenRun = timeC - timeB
    assert(tenSingleRuns < 2000)
    assert(oneTenRun < tenSingleRuns)
  }
}
