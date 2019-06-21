package de.ungefroren.discord.minzomat.embeds

import java.awt.Color
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{OffsetDateTime, ZoneOffset}

import net.dv8tion.jda.core.EmbedBuilder.ZERO_WIDTH_SPACE
import net.dv8tion.jda.core.entities.MessageEmbed._
import net.dv8tion.jda.core.entities.{EmbedType, Message, MessageEmbed, Role}

import scala.collection.JavaConverters._
import scala.collection.mutable

class AminzingEmbed private (
                    val instruction: String,
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

object AminzingEmbed {

  // language=RegExp
  private val TITLE_REGEX = "\\s*(#+)\\s*(.*)".r
  // language=RegExp
  private val COLOR_NAME_REGEX = "\\w+[ _]?\\w*".r
  // language=RegExp
  private val COLOR_HEX_REGEX = "#[0-9a-fA-F]{6}".r
  // language=RegExp
  private val COLOR_RGB_REGEX = "rgb\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)".r
  // language=RegExp
  private val COLOR_HSB_REGEX = "hsb\\(\\s*(\\d+.?\\d*)\\s*,\\s*(\\d+.?\\d*)\\s*,\\s*(\\d+.?\\d*)\\s*\\)".r

  /**
    * Tries to create an aminzing embed from the provided string
    *
    * @param instruction contains the instructions for building the embed
    * @return a aminzing embed or an empty optional
    */
  def apply(instruction: String): Option[AminzingEmbed] = apply(instruction, OffsetDateTime.now.getOffset)

  /**
    * Tries to create an aminzing embed from the provided message
    *
    * @param message a message send from an user, received by JDA
    * @return a aminzing embed or an empty optional if the message doesn't provide instructions
    */
  def apply(message: Message): Option[AminzingEmbed] = apply(message.getContentRaw, message.getCreationTime.getOffset)

  /**
    * Tries to create an aminzing embed from the provided string
    *
    * @param instruction contains the instructions for building the embed
    * @param offset zone offset that should be used for timestamps
    * @return a aminzing embed or an empty optional
    */
  def apply(instruction: String, offset: ZoneOffset): Option[AminzingEmbed] = {
    var lines = instruction.split("\r?\n").toBuffer
    val startLine = lines.indexWhere(_.trim.toLowerCase.equals("$embed:"))
    if (startLine == -1) {
      None
    }
    else {
      lines = lines.drop(startLine + 1)
      val authorName = lines.parse("author", "a")
      val authorUrl = lines.parse("authorurl", "author-url", "author_url", "au")
      val authorIcon = lines.parse("authoricon", "author-icon", "author_icon", "authorimage", "author-image", "author_image", "ai")
      val url = lines.parse("titleurl", "title-url", "title_url", "url")
      val thumbnailUrl = lines.parse("thumbnail", "thumbnailimage", "thumbnail-image", "thumbnail_image", "ti")
      val imageUrl = lines.parse("image", "i")
      val videoUrl = lines.parse("video", "v")
      val timestampString = lines.parse("timestamp", "t")
      val footerText = lines.parse("footer", "f")
      val footerImage = lines.parse("footerimage", "footer-image", "footer_image", "fi")
      val colorString = lines.parse("color", "c")
      val author = authorName.map(new AuthorInfo(_, authorUrl.orNull, authorIcon.orNull, null)).orNull
      val image = imageUrl.map(new ImageInfo(_, null, 0, 0)).orNull
      val thumbnail = thumbnailUrl.map(new Thumbnail(_, null, 0, 0)).orNull
      val video = videoUrl.map(new VideoInfo(_, 0, 0)).orNull
      val footer = footerText.map(new Footer(_, footerImage.orNull, null)).orNull
      val timestamp = timestampString.map(s =>
        if (s.equalsIgnoreCase("now")) {
          OffsetDateTime.now().withOffsetSameInstant(offset)
        } else {
          try OffsetDateTime.parse(s, DateTimeFormatter.ISO_INSTANT).withOffsetSameInstant(offset)
          catch {
            case _: DateTimeParseException => null
          }
        }).orNull
      val color = colorString.map {
        case s@COLOR_HEX_REGEX() => Color.decode(s).getRGB
        case COLOR_RGB_REGEX(r, g, b) => new Color(r.toInt, g.toInt, b.toInt).getRGB
        case COLOR_HSB_REGEX(h, s, b) => Color.getHSBColor(h.toFloat, s.toFloat, b.toFloat).getRGB
        case s@COLOR_NAME_REGEX() =>
          try {
            classOf[Color].getField(s.trim.toUpperCase.replace(' ', '_')).get(null).asInstanceOf[Color].getRGB
          } catch {
            case _: Exception => Role.DEFAULT_COLOR_RAW
          }
        case _ => Role.DEFAULT_COLOR_RAW
      }.getOrElse(Role.DEFAULT_COLOR_RAW)
      val fields = mutable.ListBuffer[Field]()
      var field_title: Option[String] = None
      var _title: Option[String] = None
      var field_description = new mutable.StringBuilder
      var field_inline = false
      var _description = new mutable.StringBuilder
      for (line <- lines) {
        line match {
          case TITLE_REGEX(h, s) =>
            if (_title.isEmpty) {
              _title = Some(s)
            } else if (field_title.isEmpty) {
              field_title = Some(s)
              field_inline = h.length > 1
            } else {
              fields += new Field(
                field_title.get.fixEmpty,
                field_description.mkString.fixEmpty,
                field_inline
              )
              field_title = Some(s)
              field_description = new mutable.StringBuilder
              field_inline = h.length > 1
            }
          case _ =>
            if (field_title.isEmpty) {
              _description ++= line
            } else {
              field_description ++= line
            }
        }
      }
      if (field_title.isDefined) {
        fields += new Field(
          field_title.get.fixEmpty,
          field_description.mkString.fixEmpty,
          field_inline
        )
      }
      if (url.isEmpty && (_title.isDefined || _description.nonEmpty)) {
        fields.insert(0, new Field(
          _title.get.fixEmpty,
          _description.mkString.fixEmpty,
          false
        ))
        _title = None
        _description = new mutable.StringBuilder
      }
      val title = _title.orNull
      val description = if (_description.isEmpty) null else _description.mkString.fixEmpty
      Some(new AminzingEmbed(
        instruction,
        url.orNull,
        title,
        description,
        if (image != null) EmbedType.IMAGE else if (video != null) EmbedType.VIDEO else EmbedType.RICH,
        timestamp,
        color,
        thumbnail,
        null,
        author,
        video,
        footer,
        image,
        fields.toList.asJava
      ))
    }
  }

  /**
    * Helper for parsing a instruction from a list of lines
    *
    * @param lines lines where the instruction should be searched for
    */
  private implicit class InstructionParser(lines: mutable.Buffer[String]) {

    /**
      * Parse a instruction from this array containing multiple lines of a string<br>
      * <br>
      * The instruction must be provided in one of the following ways:<br>
      * {{{
      *   key: instruction
      *   key: 'instruction'
      *   key: "instruction"
      *   key: `instruction`
      * }}}
      *
      * @param key   key to search for (not case sensitive)
      * @param alias alternate keys to search for
      * @return the instruction provided or an empty option
      */
    def parse(key: String, alias: String*): Option[String] = {
      parse(key).orElse(alias.map(parse).find(_.isDefined).flatten)
    }

    /**
      * Parse a instruction from this array containing multiple lines of a string<br>
      * <br>
      * The instruction must be provided in one of the following ways:<br>
      * {{{
      *   key: instruction
      *   key: 'instruction'
      *   key: "instruction"
      *   key: `instruction`
      * }}}
      *
      * @param key key to search for (not case sensitive)
      * @return the instruction provided or an empty option
      */
    def parse(key: String): Option[String] = {
      lines.find(_.trim.toLowerCase.startsWith(s"$$$key:".toLowerCase)).map(line => {
        lines -= line
        val s = line.substring(key.length + 2).trim
        if ((s.startsWith("'") && s.endsWith("'"))
          || s.startsWith("`") && s.endsWith("`")
          || s.startsWith("\"") && s.endsWith("\"")) {
          s.substring(1, s.length - 1)
        } else {
          s
        }
      }).filter(!_.isEmpty)
    }
  }

  private implicit class StringUtils(val string: String) {

    /**
      * If the string is empty (or only contains whitespace characters) use discords ZERO_WIDTH_SPACE character instead, so the emebed does still work
      *
      * @return character `\``u200E` if the string is empty, otherwise the original string
      */
    def fixEmpty: String = if (string.isEmpty || string.matches("\\s+")) ZERO_WIDTH_SPACE else string
  }
}