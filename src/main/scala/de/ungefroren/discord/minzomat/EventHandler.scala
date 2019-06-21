package de.ungefroren.discord.minzomat

import java.io.BufferedInputStream

import de.ungefroren.discord.minzomat.embeds.AminzingEmbed
import de.ungefroren.discord.minzomat.utils.WithLogger
import net.dv8tion.jda.core.entities.{ChannelType, Icon}
import net.dv8tion.jda.core.entities.Message.MentionType
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.managers.GuildController
import net.dv8tion.jda.core.{JDA, Permission}

class EventHandler(private val JDA: JDA) extends EventListener with WithLogger {

  private val BOT_EMOTE = Icon.from(new BufferedInputStream(getClass.getResourceAsStream("/minzomat_emote.png")))

  override def onEvent(event: Event): Unit = {
    event match {
      case e: MessageReceivedEvent =>
        messageReceived(e)
        helpWanted(e)
      case _ => //Do Nothing
    }
  }

  def messageReceived(event: MessageReceivedEvent): Unit = {
    AminzingEmbed(event.getMessage) match {
      case Some(embed) =>
        event.getChannel.sendMessage(embed).queue()
        event.getMessage.delete.queue()
      case None => //Do nothing
    }
  }

  def helpWanted(event: MessageReceivedEvent): Unit = {
    if (event.getChannelType == ChannelType.PRIVATE || event.getMessage.isMentioned(JDA.getSelfUser, MentionType.USER)) {
      event.getChannel.sendMessage(Help.Embed).queue()
    }
  }

  def serverAdded(event: GuildJoinEvent): Unit = {
    if (event.getGuild.getMember(JDA.getSelfUser).hasPermission(Permission.MANAGE_EMOTES)) {
      new GuildController(event.getGuild).createEmote("minzomat", BOT_EMOTE)
    }
  }
}
