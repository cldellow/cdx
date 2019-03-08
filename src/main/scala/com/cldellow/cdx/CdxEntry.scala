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

  override def toString = s"WARC(url=$url)"

  def s3Url = s"http://s3.amazonaws.com/commoncrawl/" + filename
  def range = (offset.toInt, length.toInt)
}

