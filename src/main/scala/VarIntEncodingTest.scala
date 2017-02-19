package org.openjdk.jmh.samples

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

import org.lila.clockencoder.{BitReader,BitWriter,VarIntEncoder}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class VarIntEncodingTest extends EncodingTestData {

  @Benchmark
  def testEncode = {
    val writer = new BitWriter
    VarIntEncoder.encode(encodedRounds, writer)
    writer.toArray
  }

  @Benchmark
  def testDecode = VarIntEncoder.decode(new BitReader(encoded), moves)
}
