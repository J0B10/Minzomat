package de.ungefroren.discord.minzomat.embeds

import java.time.OffsetDateTime

import net.dv8tion.jda.core.EmbedBuilder.ZERO_WIDTH_SPACE
import net.dv8tion.jda.core.entities.MessageEmbed._
import net.dv8tion.jda.core.entities.{EmbedType, Message, MessageEmbed, MessageType}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class QuoteEmbed private(
                          val quoted_message: Message,
                          url: String,
                          title: String,
                          description: String,
                          tYpe: EmbedType,
                          timestamp: OffsetDateTime,
                          color: Int,
                          thumbnail: Thumbnail,
                          siteProvider: Provider,
                          author: AuthorInfo,
                          videoInfo: VideoInfo,
                          footer: Footer,
                          image: ImageInfo,
                          fields: java.util.List[Field]
                        ) extends MessageEmbed(
  url,
  title,
  description,
  tYpe,
  timestamp,
  color,
  thumbnail,
  siteProvider,
  author,
  videoInfo,
  footer,
  image,
  fields
)

object QuoteEmbed {

  @throws(classOf[IllegalArgumentException])
  def apply(message: Message): QuoteEmbed = {
    if (message.getType != MessageType.DEFAULT) throw new IllegalArgumentException("Message is not from MessageType default")
    val author = new AuthorInfo(
      message.getMember.getEffectiveName,
      null,
      message.getAuthor.getEffectiveAvatarUrl,
      null
    )
    val description = message.getContentRaw
    val color = message.getMember.getColorRaw
    val timestamp = message.getCreationTime
    val footer = new Footer(ZERO_WIDTH_SPACE, "https://raw.githubusercontent.com/joblo2213/Minzomat/master/images/quote_footer_icon.png", null)
    val embeded = ListBuffer[String]()
    val attachments = ListBuffer[String]()
    message.getEmbeds.forEach(e => {
      e.getType match {
        case EmbedType.VIDEO => embeded += e.getVideoInfo.getUrl
        case EmbedType.IMAGE => embeded += e.getImage.getProxyUrl
        case EmbedType.LINK => embeded += e.getUrl
        case _ if e.getUrl != null && !e.getUrl.isEmpty => embeded += e.getUrl
        case _ => //No url provided, embed is ignored
      }
    })
    message.getAttachments.forEach(a => {
      attachments += s"[${a.getFileName}](${if (a.isImage) a.getProxyUrl else a.getUrl})"
    })
    val fields = ListBuffer[Field]()
    if (attachments.nonEmpty) {
      fields += new Field("\uD83D\uDCCE Attachments:", attachments.mkString("\n"), false)
    }
    if (embeded.nonEmpty) {
      fields += new Field("\uD83C\uDF10 Embeded:", embeded.mkString("\n"), false)
    }
    fields += new Field(ZERO_WIDTH_SPACE, s"[🔍 show message](${message.getJumpUrl})", false)
    new QuoteEmbed(
      message,
      null,
      null,
      description,
      EmbedType.RICH,
      timestamp,
      color,
      null,
      null,
      author,
      null,
      footer,
      null,
      fields.toList.asJava
    )
  }
}

