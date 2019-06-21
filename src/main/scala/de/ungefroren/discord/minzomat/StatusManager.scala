package de.ungefroren.discord.minzomat

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import de.ungefroren.discord.minzomat.StatusManager.{SpreadStatus, Status}
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Game.GameType

import scala.concurrent.duration._

class StatusManager(private val JDA: JDA, private val status: Seq[Status]) {

  private var statusIterator = status.iterator
  private var changeStatus: Option[Cancellable] = None
  private val system = ActorSystem()
  private var actor: Option[ActorRef] = None
  private var _current: Option[Status] = None


  /**
    * @return on how many servers the bot is currently running
    */
  def serversAmount: Int = JDA.getGuilds.size()

  /**
    * @return the status that is currently displayed
    */
  def current: Option[Status] = _current

  def init(): Unit = {
    actor = Some(system actorOf Props(classOf[StatusActor]))
    scheduleNext()
  }

  /**
    * Cancels all actions
    *
    * @return true if the actions were canceled, also returns false if they were previously (concurrently) canceled
    */
  def cancel(): Boolean = changeStatus.forall(_.cancel())

  private def scheduleNext(): Unit = {
    if (status.nonEmpty) {
      if (!statusIterator.hasNext) {
        statusIterator = status.iterator
      }
      val next = statusIterator.next()
      import system.dispatcher
      changeStatus = Some(system.scheduler.scheduleOnce(current.map(_.displayDuration).getOrElse(0 seconds), actor.get, next))
    }
  }

  class StatusActor extends Actor {
    override def receive: Receive = {
      case s: SpreadStatus => {
        JDA.getPresence.setGame(Game.of(s.activity, s.title.replace("$serversAmount", serversAmount.toString)))
        _current = Some(s)
        scheduleNext()
      }
      case s: Status => {
        JDA.getPresence.setGame(Game.of(s.activity, s.title))
        _current = Some(s)
        scheduleNext()
      }
    }
  }
}
object StatusManager {
  case class Status(activity: GameType, title: String, displayDuration: FiniteDuration)
  class SpreadStatus(override val displayDuration: FiniteDuration) extends Status(GameType.WATCHING, "$serversAmount Servers \uD83E\uDD16\uD83C\uDF89", displayDuration) {}
}

