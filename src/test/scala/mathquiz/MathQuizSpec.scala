package mathquiz

import org.junit.runner.RunWith
import org.scalatest.{ FlatSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit }
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import scala.concurrent.duration._
import akka.actor.ActorRef
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MathQuizSpec extends FlatSpecLike with Matchers with MathQuizTestKit {

  import Puzzle._

  "A Puzzle" should "contain only valid responses for its question" in {
    for (puzzle <- testQuiz) {
      puzzle.asInstanceOf[SimplePuzzle].validResponses.foreach(response => puzzle.validateResponse(response) should be(true))
    }
  }
}