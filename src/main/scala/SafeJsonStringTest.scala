package org.openjdk.jmh.samples

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class SafeJsonStringTest {

  object strings {
    val short = "Waiting"
    val medium = "Please be nice in the chat!"
    val long = """Free online Chess server. Play Chess now in a clean interface. No registration, no ads, no plugin required. Play Chess with the computer, friends or random opponents."""
    val arabic = """قد يكون خصمك قد ترك المباراة. يمكنك المطالبة بالنصر، أو التعادل، أو إنتظر."""
  }

  @scala.inline def isSafe(c: Char): Boolean =
    c != '<' && c != '>' && c != '&' && c != '"' && c != '\'' && /* html */
      c != '\\' && /* remaining js */
      c != '`' && c != '/' && /* extra care */
      32 <= c.toInt && c.toInt <= 126 /* printable ascii */

  object impls {
    /*
     * Benchmark                       Mode  Cnt      Score      Error  Units
     * SafeJsonStringTest.base_arabic  avgt   10  40841.062 ± 2646.024  ns/op
     * SafeJsonStringTest.base_long    avgt   10   4006.184 ±  153.502  ns/op
     * SafeJsonStringTest.base_medium  avgt   10    812.464 ±   51.683  ns/op
     * SafeJsonStringTest.base_short   avgt   10    354.905 ±   49.701  ns/op
     */
    def base(str: String): String = {
      val escaped = str.flatMap { c =>
        val code = c.toInt
        if (isSafe(c)) Some(c)
        else {
          def hexCode = code.toHexString.reverse.padTo(4, '0').reverse
          '\\' +: s"u${hexCode.toUpperCase}"
        }
      }
      s""""${escaped}""""
    }
    /*
     * Benchmark                       Mode  Cnt      Score      Error  Units
     * SafeJsonStringTest.opt1_arabic  avgt   10  31157.899 ± 2131.986  ns/op
     * SafeJsonStringTest.opt1_long    avgt   10    822.963 ±   12.034  ns/op
     * SafeJsonStringTest.opt1_medium  avgt   10    161.983 ±    6.035  ns/op
     * SafeJsonStringTest.opt1_short   avgt   10     68.615 ±    4.548  ns/op
     */
    def opt1(s: String): String = {
      val sb = new StringBuilder(s.size)
      var i = 0
      while (i < s.length) {
        val c = s charAt i
        if (isSafe(c)) sb.append(c)
        else {
          def hexCode = c.toInt.toHexString.reverse.padTo(4, '0').reverse
          ('\\' +: s"u${hexCode.toUpperCase}") foreach sb.append
        }
        i += 1
      }
      sb.toString
    }

    /*
[info] Benchmark                       Mode  Cnt      Score      Error  Units
[info] SafeJsonStringTest.opt2_arabic  avgt   10  23550.124 ± 1080.420  ns/op
[info] SafeJsonStringTest.opt2_long    avgt   10    827.895 ±   27.924  ns/op
[info] SafeJsonStringTest.opt2_medium  avgt   10    160.550 ±    4.402  ns/op
[info] SafeJsonStringTest.opt2_short   avgt   10     67.328 ±    4.376  ns/op
*/
    def opt2(s: String): String = {
      val sb = new StringBuilder(s.size)
      var i = 0
      while (i < s.length) {
        val c = s charAt i
        if (isSafe(c)) sb.append(c)
        else sb.append(s"\\u${c.toInt.toHexString.reverse.toUpperCase.padTo(4, '0').reverse}")
        i += 1
      }
      sb.toString
    }
  }

  // @Benchmark
  // def base_short = impls.base(strings.short)

  // @Benchmark
  // def base_medium = impls.base(strings.medium)

  // @Benchmark
  // def base_long = impls.base(strings.long)

  // @Benchmark
  // def base_arabic = impls.base(strings.arabic)

  // @Benchmark
  // def opt1_short = impls.opt1(strings.short)

  // @Benchmark
  // def opt1_medium = impls.opt1(strings.medium)

  // @Benchmark
  // def opt1_long = impls.opt1(strings.long)

  // @Benchmark
  // def opt1_arabic = impls.opt1(strings.arabic)

  @Benchmark
  def opt2_short = impls.opt2(strings.short)

  @Benchmark
  def opt2_medium = impls.opt2(strings.medium)

  @Benchmark
  def opt2_long = impls.opt2(strings.long)

  @Benchmark
  def opt2_arabic = impls.opt2(strings.arabic)
}
