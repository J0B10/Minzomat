package de.ungefroren.discord.minzomat

import java.time.OffsetDateTime

import de.ungefroren.discord.minzomat.utils.{RestartScheduler, WithLogger}
import javax.security.auth.login.LoginException
import net.dv8tion.jda.core.{JDA, JDABuilder}

class Minzomat(private val apiToken: String, private val restartScheduler: Option[RestartScheduler] = None) extends WithLogger {

  private var jdaInstance: Option[JDA] = None
  private var eventHandler: Option[EventHandler] = None

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
    JDA.addEventListener(eventHandler.get)
  }

  private def onShutdown(): Unit = {
    restartScheduler.foreach(_.cancel())
  }

  def JDA: JDA = jdaInstance.getOrElse(throw new IllegalStateException("Jda is not initialized yet"))
}
