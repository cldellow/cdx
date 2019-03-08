package com.cldellow.cdx

import org.scalatest._

class CdxTest extends FlatSpec with Matchers {
  "simple references" should "" in {
    val cdx = Cdx("/tmp/tests")
    println(cdx.collections)

    cdx.query(
      "https://index.commoncrawl.org/CC-MAIN-2018-51-index",
      "kwknittersguild.ca/*"
    ).take(1).foreach { entry =>
      println(cdx.fetchWarc(entry.s3Url, entry.range))
    }

    println(cdx.fetchWarc("http://s3.amazonaws.com/commoncrawl/crawl-data/CC-MAIN-2018-51/segments/1544376823712.21/warc/CC-MAIN-20181212022517-20181212044017-00195.warc.gz", (934231816, 3310)))
  }
}
