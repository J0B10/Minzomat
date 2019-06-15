package de.ungefroren.discord.minzomat

import de.ungefroren.discord.minzomat.embeds.AminzingEmbed


object Help {

  val Embed: AminzingEmbed = AminzingEmbed(
    """
      |$embed:
      |$author: Do you need help for using Minzomat?
      |$suthor-url:
    """.stripMargin).get
}