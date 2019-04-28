package com.cldellow.cdx

import java.io._
import java.net._
import com.fasterxml.jackson.databind.ObjectMapper
import com.cldellow.urlcache.Cache

class Cdx(root: File) {
  private val cache = new Cache(root)
  private val url = "https://index.commoncrawl.org"
  private val mapper = new ObjectMapper

  lazy val collections = {
    val inp = new String(cache.fetchNamed(s"${url}/collinfo.json"), "UTF-8")
    mapper.readValue[Array[CrawlPojo]](inp, classOf[Array[CrawlPojo]]).toList
  }

  private def parseCdxLine(line: String): CdxEntry = {
    val str = line.substring(line.indexOf(" {"))

    mapper.readValue(str, classOf[CdxEntry])
  }

  def fetchWarc(url: String, range: (Int, Int)): Array[Byte] =
    cache.fetch(url, prefix = "warc", range = range)

  def query(api: String, url: String): Seq[CdxEntry] =
    new String(
      cache.fetch(
        s"${api}?pageSize=1&url=${URLEncoder.encode(url, "UTF-8")}&filter==status:200",
        prefix = "cdx"
      ),
      "UTF-8"
    ).split("\n")
    .filter(_.nonEmpty)
    .map(parseCdxLine)

}

object Cdx {
  def apply(dir: String): Cdx = {
    val dirFile = new File(dir)
    if (!dirFile.exists())
      dirFile.mkdirs()

    new Cdx(dirFile)
  }
}

