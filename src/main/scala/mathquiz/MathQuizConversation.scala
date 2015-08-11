package mathquiz

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import akka.actor.ActorLogging
import akka.actor.Stash
import akka.actor.FSM

object MathQuizConversation {
  object Start
  object NextQuestion
  case class Question(question: String, attemptsLeft: Int, validAnswers: Int)
  case class Response(response: String)
  object Response {
    val Timeout = Response("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
  }
  object Wow
  object Buuu
  case class Message(message: String)
  object Winner
  case class Looser(reason: String)
}

sealed trait MathQuizState
object Idle extends MathQuizState
object Game extends MathQuizState
object End extends MathQuizState

case class MathQuizData(quiz: Seq[Puzzle[String]], attemptsLeft: Int, validAnswers: Int = 0) {
  def currentPuzzle: Puzzle[String] = if (quiz.size > 0) quiz.head else throw new IllegalStateException()
  def skipQuestion: MathQuizData = copy(quiz = quiz.tail, attemptsLeft = attemptsLeft - 1)
  def gotValidResponse: MathQuizData = copy(quiz = quiz.tail, validAnswers = validAnswers + 1)
  def gotInvalidResponse: MathQuizData = copy(attemptsLeft = attemptsLeft - 1)
  def isSuccess(minValidAnswers: Int): Boolean = validAnswers >= minValidAnswers
  def canContinue: Boolean = !quiz.isEmpty && attemptsLeft > 0
  def summary: String = s"attempts left: $attemptsLeft, valid answers: $validAnswers"
}

class MathQuizConversation(quiz: Seq[Puzzle[String]], maxAttempts: Int = 8, minValidAnswers: Int = 5, responseTimeout: FiniteDuration = 10 seconds)
    extends Actor with ActorLogging with FSM[MathQuizState, MathQuizData] {

  val QUESTION_TIMER = "questionTimer"

  import MathQuizConversation._

  startWith(Idle, MathQuizData(quiz, maxAttempts, 0))

  when(Idle) {
    case Event(Start, state) =>
      sayWelcome()
      askQuizQuestion(state)
      goto(Game)
    case _ =>
      sender ! Message("Waiting to start game, insert coin ...")
      stay
  }

  when(Game) {
    case Event(Response(response: String), state) =>
      cancelTimer(QUESTION_TIMER)
      val newState = state.currentPuzzle.validateResponse(response) match {
        case true =>
          sender ! Wow
          state.gotValidResponse
        case false =>
          sender ! Buuu
          state.gotInvalidResponse
      }
      if (newState.isSuccess(minValidAnswers)) {
        sender ! Winner
        goto(End)
      } else if (newState.canContinue) {
        askQuizQuestion(newState)
        stay using newState
      } else {
        sender ! Looser(newState.summary)
        goto(End)
      }

    case Event(NextQuestion, state) =>
      val newState = state.skipQuestion
      if (newState.canContinue) {
        askQuizQuestion(newState)
        stay using newState
      } else {
        sender ! Looser(newState.summary)
        goto(End)
      }

    case _ =>
      sender ! Message("Waiting for response ...")
      stay
  }

  when(End) {
    case Event(_, state) =>
      sender ! Message("Game is really over! Insert new coin ...")
      stay
  }

  onTransition {
    case _ -> End => sender ! Message("Game is over!")
  }

  initialize()

  def sayWelcome() = {
    sender ! Message(s"Welcome to MathQuiz!")
  }

  def askQuizQuestion(state: MathQuizData) = {
    sender ! Question(state.currentPuzzle.getQuestion, state.attemptsLeft, state.validAnswers)
    setTimer(QUESTION_TIMER, Response.Timeout, responseTimeout, false)
  }

}