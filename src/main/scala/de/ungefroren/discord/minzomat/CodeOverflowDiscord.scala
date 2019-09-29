package de.ungefroren.discord.minzomat

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.managers.GuildController

import scala.collection.JavaConverters._

object CodeOverflowDiscord {

  val GUILD_ID = 577412066994946060L
  val `#rules_CHANNEL_ID` = 580789858537308160L
  val `has_read_the_#📜rules_ROLE_ID` = 615153102139686912L
  val EMOTES = Seq("""👌""", """👍""")

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

  /**
    * Grants writing permissions to users that reacted while the bot was inactive
    * @param JDA JDA instance
    */
  def manageOldReactions(JDA: JDA): Unit = {
    val guild = JDA.getGuildById(GUILD_ID)
    var i = 0
    for (message <- guild.getTextChannelById(`#rules_CHANNEL_ID`).getIterableHistory.asScala) {
      if (i >= 20) {
        return //Overflow protection for large channels
      } else {
        i += 1
        message.getReactions.asScala.filter(r => EMOTES.contains(r.getReactionEmote.getName)).foreach(reaction => {
          reaction.getUsers().asScala.map(u => guild.getMember(u)).filter(Option(_).isDefined).foreach(member => {
            if (!member.getRoles.asScala.exists(_.getIdLong == `has_read_the_#📜rules_ROLE_ID`)) {
              val controller = new GuildController(guild)
              controller.addRolesToMember(member, guild.getRoleById(`has_read_the_#📜rules_ROLE_ID`)).queue()
            }
          })
        })
      }
    }
  }
}
