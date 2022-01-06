import cats.effect.IO

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global

import java.io.File
import java.nio.file.Files
import scala.io.Source

object calculatingAverageLengthOfWordsApp extends App {

  val processorCount = Runtime.getRuntime.availableProcessors()

  val textHamlet = Source.fromURL("https://raw.githubusercontent.com/benschw/shakespeare-txt/master/shakespeare-hamlet-25.txt")
    .mkString
    .replaceAll("\\P{L}", " ")
    .split(" ")
    .filter(element => element.nonEmpty)

  /*def readInts(f: File): Array[Int] =
    Files
      .readString(f.toPath)
      .split('\n')
      .map(_.toInt)*/

  val loadInts = textHamlet.map(_.length)

  println(textHamlet(4))
  println(loadInts(4))
  println(textHamlet.length)
  println(loadInts.length)

  val isEven: Int => Boolean = _ % 2 == 0
  val isOdd:  Int => Boolean = _ % 2 == 1

  val evenOddThreshold = 2.0

  val ioInts: IO[Array[Int]] = IO{loadInts}

  extension (arr: Array[Int])
    def average: Double = 1.0 * arr.sum / arr.length

  def filteredAverage(pred: Int => Boolean)(ints: Array[Int]): IO[Double] =
    IO{
      println(Thread.currentThread.getName)
      ints
        .filter(pred)
        .average
    }

  val cpuPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  val ioAvgEven = filteredAverage(isEven)(_)
  val ioAvgOdd = filteredAverage(isOdd)(_)
  val ioDiff =
    for
      ints <- ioInts
      even <- ioAvgEven(ints).evalOn(cpuPool)
      odd  <- ioAvgOdd(ints).evalOn(cpuPool)
    yield
      math.abs(even - odd)

  val diff = ioDiff.unsafeRunSync()
  //assertTrue(diff < evenOddThreshold)
  println(average)
  println(diff)

}
