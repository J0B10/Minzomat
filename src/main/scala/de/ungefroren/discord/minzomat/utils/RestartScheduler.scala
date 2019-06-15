package de.ungefroren.discord.minzomat.utils

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{Actor, ActorSystem, Cancellable, Props}
import de.ungefroren.discord.minzomat.utils.RestartScheduler._

import scala.concurrent.duration._

/**
  * Schedules a shutdown of the bot after a given time interval
  *
  * @param scheduleIn duration after which the bot should be shut down
  * @param notifyInterval interval between the notifications before shutdown
  * @param notifyTimes how often should be notified before shutdown (0 to deactivate notifications)
  */
class RestartScheduler(scheduleIn: FiniteDuration, val notifyInterval: FiniteDuration, val notifyTimes: Int) extends WithLogger {

  /**
    * Time when the restart scheduler was setup
    */
  val setup_time: OffsetDateTime = OffsetDateTime.now

  /**
    * Time at which the next restart will be performed
    */
  val restart_time: OffsetDateTime = OffsetDateTime.now.plus(scheduleIn.toMillis, ChronoUnit.MILLIS)

  private var i = 0

  private var countdown: Option[Cancellable] = None
  private var restart: Option[Cancellable] = None

  /**
    * Start this restart scheduler
    */
  def init(): Unit = {
    val system = ActorSystem()
    val actor = system actorOf Props(classOf[RestartActor])
    import system.dispatcher
    countdown = if (i > 0) Some(system.scheduler.schedule(
      scheduleIn - notifyInterval * notifyTimes,
      notifyInterval,
      actor,
      Countdown({
        val left = (notifyTimes - i) * notifyInterval
        i += 1
        left
      }))) else None
    restart = Some(system.scheduler.scheduleOnce(scheduleIn, actor, Restart))
  }

  /**
    * Stops this scheduler and cancels all actions
    *
    * @return true if the actions were canceled, also returns false if they were previously (concurrently) canceled
    */
  def cancel(): Boolean = {
    val c = countdown.map(_.cancel)
    val r = restart.map(_.cancel())
    c.getOrElse(true) && r.getOrElse(true)
  }
}

object RestartScheduler {

  /**
    * Schedules a shutdown of the bot after a given time interval
    *
    * @param scheduleIn duration after which the bot should be shut down
    * @param notifyInterval interval between the notifications before shutdown
    * @param notifyTimes how often should be notified before shutdown (0 to deactivate notifications)
    */
  def apply(scheduleIn: FiniteDuration, notifyInterval: FiniteDuration, notifyTimes: Int): RestartScheduler =
    new RestartScheduler(scheduleIn, notifyInterval, notifyTimes)

  /**
    * Schedules a shutdown of the bot after a given time interval<br>
    * Won't display any notifications before shutdown
    * @param scheduleIn duration after which the bot should be shut down
    */
  def apply(scheduleIn: FiniteDuration): RestartScheduler =
    new RestartScheduler(scheduleIn, 0 seconds, 0)

  /**
    * Schedules a shutdown of the bot at a given time
    *
    * @param scheduleAt time at which the bot should shut down
    * @param notifyInterval interval between the notifications before shutdown
    * @param notifyTimes how often should be notified before shutdown (0 to deactivate notifications)
    */
  def apply(scheduleAt: OffsetDateTime, notifyInterval: FiniteDuration, notifyTimes: Int) =
    new RestartScheduler(OffsetDateTime.now().until(scheduleAt, ChronoUnit.MILLIS) milliseconds, notifyInterval, notifyTimes)

  /**
    * Shut down the bot
    */
  case class Restart()

  /**
    * Log a info message that notifies about a upcoming restart
    * @param i duration till restart
    */
  case class Countdown(i: FiniteDuration)

  class RestartActor extends Actor with WithLogger {
    override def receive: Receive = {
      case Restart() =>
        log info "SHUTING DOWN NOW!"
        sys exit 0
      case Countdown(i) =>
        log info s"Restart scheduled in $i"
    }
  }
}