package mathquiz

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object MathQuizCommandLine {

  case class StartQuiz(quiz: Seq[Puzzle[String]])

}

class MathQuizCommandLine() extends Actor with ActorLogging {

  import MathQuizCommandLine._
  import MathQuizConversation._

  var conversation: Option[ActorRef] = None
  var quiz: Seq[Puzzle[String]] = Seq.empty

  val separator = "#" * 90

  val maxAttempts = 8
  val minValidAnswers = 5
  val responseTimeout: FiniteDuration = 10 seconds

  def receive = {

    case StartQuiz(quiz) =>
      this.quiz = quiz
      conversation foreach context.stop
      conversation = Some(context.actorOf(Props(classOf[MathQuizConversation], quiz, maxAttempts, minValidAnswers, responseTimeout)))
      context.watch(conversation.get)
      conversation ! Start
      println("Quiz tutorial:")
      println("type `next' to skip question")
      println("type `quit' to end quiz")
      println("type `exit' to exit application")

    case Question(question, attemptsLeft, validAnswers) =>
      printQuestionAndReadResponse(s"[$validAnswers diamond(s)] $question ")

    case Message(message: String) =>
      println(message)

    case Wow =>
      println("You are right!")

    case Buuu =>
      println("Wrong answer, try again ...")

    case Winner =>
      printImportantMessage("You are the Winner!")
      askIfContinue()

    case Looser(reason: String) =>
      printImportantMessage(s"So sorry, you lost that quiz :-( [$reason]")
      askIfContinue()

  }

  def printQuestionAndReadResponse(question: String): Unit = {
    val response = scala.io.StdIn.readLine(s"$question = ")
    response match {
      case null => printQuestionAndReadResponse(question)
      case "next" => conversation ! NextQuestion
      case "exit" => askIfContinue()
      case "quit" =>
        println("You are leaving the quiz :-(")
        askIfContinue()
      case _ => conversation ! Response(response.trim())
    }
  }

  def printImportantMessage(message: String): Unit = {
    println(separator)
    println(s"###### $message")
    println(separator)
  }

  def askIfContinue() = {
    scala.io.StdIn.readLine("Do you want to start new quiz [yes] or exit the game [no]? ") match {
      case "yes" =>
        self ! StartQuiz(quiz)
      case _ =>
        System.exit(0)
    }
  }

}