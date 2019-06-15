package de.ungefroren.discord.minzomat

import de.ungefroren.discord.minzomat.embeds.AminzingEmbed
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message.MentionType
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener

class EventHandler(private val JDA: JDA) extends EventListener{

  override def onEvent(event: Event): Unit = {
    event match {
      case e: MessageReceivedEvent =>
        messageReceived(e)
        helpWanted(e)
    }
  }

  def messageReceived(event: MessageReceivedEvent): Unit = {
    AminzingEmbed(event.getMessage) match {
      case Some(embed) =>
        event.getChannel.sendMessage(embed).queue()
        event.getMessage.delete.queue()
    }
  }

  def helpWanted(event: MessageReceivedEvent): Unit = {
    if (event.getChannelType == ChannelType.PRIVATE || event.getMessage.isMentioned(JDA.getSelfUser, MentionType.USER)) {
      event.getChannel.sendMessage(Help.Embed).queue()
    }
  }
}
