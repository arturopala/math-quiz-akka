package mathquiz

trait Puzzle[T] {
  def getQuestion: T
  def validateResponse(response: T): Boolean
}

object Puzzle {

  case class SimplePuzzle(question: String, validResponses: Seq[String]) extends Puzzle[String] {
    def getQuestion = question
    def validateResponse(response: String) = validResponses.find(_ == response.trim()).isDefined
  }

  def apply(question: String, response: String*): Puzzle[String] = new SimplePuzzle(question, response)
}