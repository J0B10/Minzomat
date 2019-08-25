package de.ungefroren.discord.minzomat


import de.ungefroren.discord.minzomat.embeds.{AminzingEmbed, QuoteEmbed}
import de.ungefroren.discord.minzomat.utils.WithLogger
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message.MentionType
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.managers.GuildController
import net.dv8tion.jda.core.{JDA, Permission}

class EventHandler(private val JDA: JDA) extends EventListener with WithLogger {

  override def onEvent(event: Event): Unit = {
    event match {
      case e: MessageReceivedEvent =>
        messageReceived(e)
        helpWanted(e)
        log info e.getMessage.getContentRaw
      case e: GuildMemberRoleAddEvent => serverAdded(e)
      case e: GuildMessageReactionAddEvent =>
        quoteMessage(e)
        CodeOverflowDiscord.acceptRules(e)
      case _ => //Do Nothing
    }
  }

  def messageReceived(event: MessageReceivedEvent): Unit = {
    AminzingEmbed(event.getMessage) match {
      case Some(embed) =>
        if (event.getChannelType == ChannelType.TEXT
          && event.getMember.hasPermission(event.getTextChannel, Permission.MESSAGE_MANAGE)) {
          event.getChannel.sendMessage(embed).queue()
          event.getMessage.delete.queue()
        }
      case None => //Do nothing
    }
  }

  def helpWanted(event: MessageReceivedEvent): Unit = {
    if (event.getAuthor != JDA.getSelfUser) {
      if (event.getChannelType == ChannelType.PRIVATE || event.getMessage.isMentioned(JDA.getSelfUser, MentionType.USER)) {
        event.getChannel.sendMessage(Help.Embed).queue()
      }
    }
  }

  def serverAdded(event: GuildMemberRoleAddEvent): Unit = {
    if (JDA.getSelfUser == event.getUser) {
      if (event.getMember.hasPermission(Permission.MANAGE_EMOTES)) {
        val controller = new GuildController(event.getGuild)
        if (event.getGuild.getEmotesByName("minzomat", true).isEmpty)
          controller.createEmote("minzomat", Minzomat.BOT_EMOTE).queue()
        if (event.getGuild.getEmotesByName("quote", true).isEmpty)
          controller.createEmote("quote", Minzomat.QUOTE_EMOTE).queue()
      }
    }
  }

  def quoteMessage(event: GuildMessageReactionAddEvent): Unit = {
    if (event.getReactionEmote.getName == "quote") {
      if (event.getMember.hasPermission(event.getChannel, Permission.MESSAGE_WRITE)) {
        event.getChannel.getMessageById(event.getMessageIdLong).queue(
          m => event.getChannel.sendMessage(QuoteEmbed(m)).queue()
        )
      }
    }
  }
}
