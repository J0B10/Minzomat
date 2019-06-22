package de.ungefroren.discord.minzomat

import de.ungefroren.discord.minzomat.embeds.AminzingEmbed


object Help {

  val Embed: AminzingEmbed = AminzingEmbed(
    """$embed:
      |$author: Do you need help for using Minzomat?
      |$author-url: https://github.com/joblo2213/Minzomat/wiki
      |$author-icon: https://raw.githubusercontent.com/joblo2213/Minzomat/master/images/minzomat.png
      |$color: #8C9EFF
      |_Available translations:_ [🇩🇪](https://github.com/joblo2213/Minzomat/wiki/Home_de)
      |
      |Minzomat makes your server aminzing™ by adding the following stuff:
      |
      |1️⃣  **[Write your own embeds](https://github.com/joblo2213/Minzomat/wiki/write_embeds_en)** [🇬🇧](https://github.com/joblo2213/Minzomat/wiki/write_embeds_en) / [🇩🇪](https://github.com/joblo2213/Minzomat/wiki/write_embeds_de)
      |   _Allows you to send a embed just like a normal message_
      |
      |🔧  **Quote messages  _[WIP]_**
      |   _Allows you to show what another user wrote some time ago inside an embed_
      |   _This Feature is not finished yet._
      |
      |🔧  **Large reactions  _[WIP]_**
      |   _React on a message with the `:minzomat:` emoji adn the bot will display all reactions in one large image._
      |   _This Feature is not finished yet._
      |$footer: git.io/minzomat
      |$footerImage: https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png""".stripMargin).get
}