package com.cldellow.cdx

import com.fasterxml.jackson.annotation._

class CrawlPojo {
  @JsonProperty("id")
  var id: String = null

  @JsonProperty("name")
  var name: String = null

  @JsonProperty("timegate")
  var timegate: String = null

  @JsonProperty("cdx-api")
  var cdxApi: String = null

  override def toString = s"CrawlPojo(id=${id})"
}


