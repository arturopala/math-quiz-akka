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
class MathQuizConversationSpec extends FlatSpecLike with Matchers with ActorSystemTestKit with MathQuizTestKit {

  import MathQuizConversation._

  "A MathQuizData" should "move to the next question when got valid response" in {
    val state = new MathQuizData(testQuiz, 8)
    val newState = state.gotValidResponse
    newState.quiz should be(testQuiz.tail)
    newState.validAnswers should be(1)
    newState.attemptsLeft should be(8)
    newState.isSuccess(5) should be(false)
    newState.canContinue should be(true)
  }

  it should "stay with same question when got invalid response" in {
    val state = new MathQuizData(testQuiz, 8)
    val newState = state.gotInvalidResponse
    newState.quiz should be(testQuiz)
    newState.validAnswers should be(0)
    newState.attemptsLeft should be(7)
    newState.isSuccess(5) should be(false)
    newState.canContinue should be(true)
  }

  it should "move to the new question when skipped" in {
    val state = new MathQuizData(testQuiz, 8)
    val newState = state.skipQuestion
    newState.quiz should be(testQuiz.tail)
    newState.validAnswers should be(0)
    newState.attemptsLeft should be(7)
    newState.isSuccess(5) should be(false)
    newState.canContinue should be(true)
  }

  it should "return success when all required questions were answered" in {
    val state = new MathQuizData(testQuiz, 8)
    val newState = state.gotValidResponse.gotValidResponse.gotValidResponse
    newState.validAnswers should be(3)
    newState.attemptsLeft should be(8)
    newState.isSuccess(3) should be(true)
    newState.canContinue should be(true)
  }

  it should "not allow to continue quiz when none attempts were left" in {
    val state = new MathQuizData(testQuiz, 3)
    val newState = state.gotInvalidResponse.gotInvalidResponse.gotInvalidResponse
    newState.validAnswers should be(0)
    newState.attemptsLeft should be(0)
    newState.isSuccess(3) should be(false)
    newState.canContinue should be(false)
  }

  it should "not allow to continue quiz when there are no more questions" in {
    val state = new MathQuizData(testQuiz.take(3), 8)
    val newState = state.skipQuestion.skipQuestion.skipQuestion
    newState.validAnswers should be(0)
    newState.attemptsLeft should be(5)
    newState.isSuccess(3) should be(false)
    newState.canContinue should be(false)
  }

  "A MathQuizConversation" should "start with welcome message and first question, then await valid response" in new ActorSystemTest {
    val conversation = TestMathQuizConversation(testQuiz, 8, 5, 10 seconds)
    conversation ! Start
    expectMsgClass(classOf[Message])
    expectMsg(Question("1+1", 8, 0))
    conversation ! Response("1")
    expectMsg(Buuu)
    expectMsg(Question("1+1", 7, 0))
    conversation ! Response("2")
    expectMsg(Wow)
    expectMsg(Question("sqrt(1)", 7, 1))
  }

  it should "left question unanswered and move to the next after max number of attempts" in {
    //TODO
  }

}