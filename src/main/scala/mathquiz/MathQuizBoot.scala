package mathquiz

import scala.concurrent.duration.DurationInt
import akka.actor.ActorSystem
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.can.server.UHttp

object MathQuizBoot extends App {

  implicit val system = ActorSystem("mathquizapp")
  implicit val module = new Module {}

  implicit val timeout = Timeout(5.seconds)

  val quiz = QuizSource.readQuizDefinition("quiz.txt")

  //start HTTP service
  //IO(UHttp) ! Http.Bind(module.httpServiceActor, interface = "0.0.0.0", port = 8080)

  //start command-line service
  module.mathQuizCommandLineActor ! MathQuizCommandLine.StartQuiz(quiz)

}

object QuizSource {
  def readQuizDefinition(fileName: String): Seq[Puzzle[String]] = {
    val url = this.getClass.getResource(s"/$fileName")
    try {
      val in = url.openStream()
      in.close()
    } catch {
      case e: Exception =>
        println("Missing $fileName file!")
        System.exit(1)
    }
    val quiz: Seq[Puzzle[String]] = scala.io.Source
      .fromURL(url)
      .getLines()
      .map(line => line.split("="))
      .map(array => Puzzle(array(0).trim(), array.drop(1).map(_.trim()): _*))
      .toSeq
    quiz
  }
}
