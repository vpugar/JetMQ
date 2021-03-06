package net.jetmq.broker

import akka.actor.{ActorLogging, ActorRef}
import akka.pattern.ask
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import akka.util.Timeout
import net.jetmq.broker.Helpers.ConnectionException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class MqttConnectionActor(sessions: ActorRef) extends ActorSubscriber with ActorLogging with ActorPublisherWithBuffer[Packet] {

  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  var session = ActorRef.noSender

  def receive = {

    case OnNext(c: Connect) if (session != ActorRef.noSender) => {

      onErrorThenStop(ConnectionException("Actor already connected"))
    }

    case OnNext(c: Connect) => {

      implicit val timeout = Timeout(1 seconds)

      val sessionF: Future[ActorRef] = ask(sessions, c).mapTo[ActorRef]
      session = Await.result(sessionF, timeout.duration)

      session ! c

      log.info("Got " + c)
    }

    case OnNext(p: Packet) if (session == ActorRef.noSender) => onErrorThenStop(new ConnectionException("Actor not connected yet"))

    case OnNext(d: Disconnect) => {
      session ! Disconnect(Header(dup = false, qos = 0, retain = false))

      onCompleteThenStop()
    }

    case OnNext(p: Packet) => {
      session ! p

      log.info("Got " + p)
    }

    case p: Packet => {

      log.info("Buffer length " + buffer.length + " and demand " + totalDemand)

      onNextBuffered(p)
    }

    case Request(count) => {
      log.info("Requested: " + count + " demand is " + totalDemand + " and buffer is " + buffer.length)
      deliverBuffer()
    }

    case Cancel => {
      log.info("was canceled")
      onCompleteThenStop()
    }

    case WrongState => {
      log.info("Session was in a wrong state")

      onErrorThenStop(new ConnectionException("Session was in a wrong state"))
    }

    case KeepAliveTimeout => {
      log.info("Keep alive timed out. Closing connection")

      onErrorThenStop(new ConnectionException("Keep alive timed out. Closing connection"))
    }

    case OnComplete => onCompleteThenStop()

    case OnError(err: Exception) => onErrorThenStop(err)

    case x => {

      println("Got " + x.getClass().getCanonicalName() + " " + x + " from " + sender)
    }
  }
}
