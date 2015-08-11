package mathquiz

import com.softwaremill.macwire.Macwire
import akka.actor.Props
import scala.concurrent.duration._
import akka.actor.IndirectActorProducer
import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.ActorRefFactory
import scala.reflect.ClassTag

class Module(implicit system: ActorSystem) extends Macwire with ActorOf {

  import http._

  val period: FiniteDuration = 10.seconds

  lazy val mathQuizCommandLineActor = actorOf[MathQuizCommandLine]("mathquiz-commandline")
  lazy val httpService: MathQuizHttpService = wire[MathQuizHttpService]
  lazy val httpServiceActor: ActorRef = actorOf[MathQuizHttpServiceActor]("http", httpService)
}

trait ActorOf {
  def actorOf[T](name: String, args: Any*)(implicit factory: ActorRefFactory, ct: ClassTag[T]): ActorRef = factory.actorOf(Props(ct.runtimeClass, args: _*), name)
}
