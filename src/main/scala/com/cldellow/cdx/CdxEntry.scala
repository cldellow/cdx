package com.cldellow.cdx

import com.fasterxml.jackson.annotation._

@JsonIgnoreProperties(ignoreUnknown = true)
class CdxEntry {
  @JsonProperty("url")
  var url: String = null

  @JsonProperty("mime")
  var mime: String = null

  @JsonProperty("status")
  var status: String = null

  @JsonProperty("length")
  var length: String = null

  @JsonProperty("offset")
  var offset: String = null

  @JsonProperty("filename")
  var filename: String = null

  @JsonProperty("languages")
  var languagesAsString: String = null

  def languages: List[String] = Option(languagesAsString).map(_.split(",").toList).getOrElse(Nil).filter(_.nonEmpty)


  override def toString = {
    val maybeLanguages = languages match {
      case Nil => ""
      case xs => ", " + xs.mkString("/")
    }

    s"WARC(url=$url, ${status}${maybeLanguages})"
  }

  def s3Url = s"http://s3.amazonaws.com/commoncrawl/" + filename
  def range = (offset.toInt, length.toInt)
}

