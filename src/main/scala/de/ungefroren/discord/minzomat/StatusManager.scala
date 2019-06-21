package de.ungefroren.discord.minzomat

import java.util.concurrent.Executors

import de.ungefroren.discord.minzomat.StatusManager.Status
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Game.GameType

import scala.concurrent.duration._

class StatusManager(private val JDA: JDA, private val status: Seq[Status]) {

  private val executor = Executors.newSingleThreadScheduledExecutor()

  private var statusIterator = status.iterator
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
    nextStatus.run()
  }

  /**
    * Cancels all actions
    */
  def cancel(): Unit = {
    executor.shutdownNow()
  }

  private val nextStatus: Runnable =() => {
    if (status.nonEmpty) {
      if (!statusIterator.hasNext) {
        statusIterator = status.iterator
      }
      _current = Some(statusIterator.next())
      JDA.getPresence.setGame(Game.of(current.get.activity, current.get.title))
      executor.schedule(nextStatus, current.get.displayDuration.toMillis, MILLISECONDS)
    }
  }
}
object StatusManager {
  case class Status(activity: GameType, title: String, displayDuration: FiniteDuration)
  class SpreadStatus(override val displayDuration: FiniteDuration) extends Status(GameType.WATCHING, "$serversAmount Servers \uD83E\uDD16\uD83C\uDF89", displayDuration) {}
}

