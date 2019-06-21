package de.ungefroren.discord.minzomat

import java.time.OffsetDateTime

import de.ungefroren.discord.minzomat.StatusManager.{SpreadStatus, Status}
import de.ungefroren.discord.minzomat.utils.{RestartScheduler, WithLogger}
import javax.security.auth.login.LoginException
import net.dv8tion.jda.core.entities.Game.GameType
import net.dv8tion.jda.core.{JDA, JDABuilder}

import scala.concurrent.duration._

class Minzomat(private val apiToken: String, private val restartScheduler: Option[RestartScheduler] = None) extends WithLogger {

  private var jdaInstance: Option[JDA] = None
  private var eventHandler: Option[EventHandler] = None
  private var statusManager: Option[StatusManager] = None

  private val STATUS = Seq(
    Status(GameType.DEFAULT, "Mention or PM for help!", 5 seconds),
    new SpreadStatus(3 seconds),
    Status(GameType.DEFAULT, "Mention or PM for help!", 5 seconds),
    Status(GameType.WATCHING, "https://git.io/minzomat \uD83E\uDD16", 5 seconds)
  )

  def init(): Boolean = {
    try {
      log info "Waiting while JDA is logging in..."
      jdaInstance = Some(new JDABuilder(apiToken).build.awaitReady)
      sys addShutdownHook onShutdown
      log info "JDA successfully logged in."
      onStart
      true
    } catch {
      case e: LoginException =>
        log error s"Login failed: ${e.getMessage}"
        false
      case _: InterruptedException =>
        log warn "Login process was interrupted"
        false
    }
  }

  private def onStart(): Unit = {
    val now = OffsetDateTime.now()
    var restartTime = now.withHour(4).withMinute(20).withSecond(0)
    if (now.compareTo(restartTime) > 0) restartTime = restartTime.plusDays(1)
    //restartScheduler = Some(RestartScheduler(restartTime, 10 seconds, 6))
    restartScheduler.foreach(_.init())
    eventHandler = Some(new EventHandler(JDA))
    statusManager = Some(new StatusManager(JDA, STATUS))
    JDA.addEventListener(eventHandler.get)
  }

  private def onShutdown(): Unit = {
    log info "Shutting down the bot..."
    restartScheduler.foreach(_.cancel())
  }

  def JDA: JDA = jdaInstance.getOrElse(throw new IllegalStateException("Jda is not initialized yet"))
}
