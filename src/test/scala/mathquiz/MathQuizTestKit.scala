package mathquiz

import akka.testkit.TestActorRef
import scala.concurrent.duration._
import akka.actor._

trait MathQuizTestKit {

  val testQuiz = Seq(
    Puzzle("1+1", "2"),
    Puzzle("sqrt(1)", "1"),
    Puzzle("0^1", "1"),
    Puzzle("1^0", "1"),
    Puzzle("7*8", "56"),
    Puzzle("1/2 + 2/1", "2.5", "5/2"),
    Puzzle("10-10", "0"),
    Puzzle("1/2 + 1/3", "5/6"),
    Puzzle("100*100", "10000"),
    Puzzle("sin(0)", "0")
  )

  object TestMathQuizConversation {
    def apply(quiz: Seq[Puzzle[String]], maxAttempts: Int = 8, minValidAnswers: Int = 5, responseTimeout: FiniteDuration = 10 seconds)(implicit system: ActorSystem) =
      TestActorRef(new MathQuizConversation(quiz, maxAttempts, minValidAnswers, responseTimeout))
  }

}