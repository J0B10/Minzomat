package de.ungefroren.discord.minzomat

import de.ungefroren.discord.minzomat.StatusManager.{SpreadStatus, Status}
import de.ungefroren.discord.minzomat.utils.{RestartScheduler, WithLogger}
import javax.security.auth.login.LoginException
import net.dv8tion.jda.core.entities.Game.GameType
import net.dv8tion.jda.core.entities.Icon
import net.dv8tion.jda.core.{JDA, JDABuilder}

import scala.concurrent.duration._
import scala.language.postfixOps

class Minzomat(private val apiToken: String, private val restartScheduler: Option[RestartScheduler] = None) extends WithLogger {

  private var jdaInstance: Option[JDA] = None
  private var eventHandler: Option[EventHandler] = None
  private var statusManager: Option[StatusManager] = None

  private val STATUS = Seq(
    Status(GameType.WATCHING, "git.io/minzomat \uD83E\uDD16", 7 seconds)
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
    restartScheduler.foreach(_.init())
    eventHandler = Some(new EventHandler(JDA))
    statusManager = Some(new StatusManager(JDA, STATUS))
    statusManager.get.init()
    JDA.addEventListener(eventHandler.get)
  }

  private def onShutdown(): Unit = {
    log info "Shutting down the bot..."
    restartScheduler.foreach(_.cancel())
    statusManager.foreach(_.cancel())
  }

  def JDA: JDA = jdaInstance.getOrElse(throw new IllegalStateException("Jda is not initialized yet"))
}
object Minzomat {
  val BOT_EMOTE = Icon.from(getClass.getResourceAsStream("/minzomat_emote.png"))
  val QUOTE_EMOTE = Icon.from(getClass.getResourceAsStream("/quote_emote.png"))
}
