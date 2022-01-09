import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global
import cats.effect.IO
import scala.io.Source
import math.BigDecimal.double2bigDecimal

object calculatingAverageLengthOfWordsApp1 {

  def main (args: Array[String]): Unit =

    val textHamlet1 = Source.fromURL("https://raw.githubusercontent.com/benschw/shakespeare-txt/master/shakespeare-hamlet-25.txt")
    val textHamlet = textHamlet1
      .mkString
      .replaceAll("\\P{L}", " ")
      .split(" ")
      .filter(element => element.nonEmpty)

    textHamlet1.close()

    val isEven: String => Boolean = textHamlet.indexOf(_) % 2 == 0
    val isOdd: String => Boolean = textHamlet.indexOf(_) % 2 == 1

    val ioText: IO[Array[String]] = IO{textHamlet}

    extension (arr: Array[String])
      def average: Double = 1.0 * arr.map(_.length).sum / arr.length

    def filteredAverage(pred: String => Boolean)(ints: Array[String]): IO[Double] =
      IO{
        println(Thread.currentThread.getName)
        ints
          .filter(pred)
          .average
    }

    val cpuPool = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

    val ioAvgEven = filteredAverage(isEven)(_)
    val ioAvgOdd = filteredAverage(isOdd)(_)
    val ioDiff =
      for
        text <- ioText
        even <- ioAvgEven(text).evalOn(cpuPool)
        odd  <- ioAvgOdd(text).evalOn(cpuPool)
      yield
        println((even + odd) / 2 setScale(1, BigDecimal.RoundingMode.HALF_UP))

    ioDiff.unsafeRunSync()
    cpuPool.shutdown()
}
