package de.ungefroren.discord.minzomat

import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.managers.GuildController

import scala.collection.JavaConverters._

object CodeOverflowDiscord {

  val GUILD_ID = 577412066994946060L
  val `#rules_CHANNEL_ID` = 580789858537308160L
  val `has_read_the_#📜rules_ROLE_ID` = 615153102139686912L
  val EMOTES = Seq(""""👌"""", """"👍"""")

  /**
    * If a user accepts the rules by reacting with 👌 or 👍 to a message in the roles channel
    * this function grants him writing permissions.
    *
    * @param event the reaction add event that triggered ths function
    */
  def acceptRules(event: GuildMessageReactionAddEvent): Unit = {
    if (event.getGuild.getIdLong == GUILD_ID
      && event.getChannel.getIdLong == `#rules_CHANNEL_ID`
      && EMOTES.contains(event.getReactionEmote.getName)
      && !event.getMember.getRoles.asScala.exists(_.getIdLong == `has_read_the_#📜rules_ROLE_ID`)) {
      val controller = new GuildController(event.getGuild)
      controller.addRolesToMember(event.getMember, event.getGuild.getRoleById(`has_read_the_#📜rules_ROLE_ID`)).queue()
    }
  }
}
