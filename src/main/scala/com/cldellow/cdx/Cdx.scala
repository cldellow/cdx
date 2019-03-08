package com.cldellow.cdx

import java.io._
import java.net._
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import net.openhft.hashing._


class Cdx(root: File) {
  private val url = "https://index.commoncrawl.org"
  private val xx = LongHashFunction.xx()
  private val murmur = LongHashFunction.murmur_3()
  private val mapper = new ObjectMapper

  lazy val collections = {
    val inp = new String(fetchUrl(Some("misc"), s"${url}/collinfo.json", name = "collinfo.json"), "UTF-8")
    mapper.readValue[Array[CrawlPojo]](inp, classOf[Array[CrawlPojo]]).toList
  }

  private def hash(input: String): String = {
    def pad(x: String): String = ("0" * (16 - x.length)) + x
    pad(xx.hashChars(input).toHexString) + pad(murmur.hashChars(input).toHexString)
  }

  private def fetchUrl(cachePrefix: Option[String], url: String, range: (Int, Int) = null, name: String = null): Array[Byte] = {
    val filehash = hash(url)
    val filehashDir = filehash.take(2) + "/" + filehash.drop(2).take(2)

    val dirPrefix = List(cachePrefix, if(name == null) Some(filehashDir) else None).flatten
    val fname = Option(name).getOrElse(filehash) +
      Option(range).map { case (start, len) => "-" + start + "-" + len }.getOrElse("")

    if(dirPrefix.nonEmpty)
      new File(root, dirPrefix.mkString("/")).mkdirs()

    val finalf = new File(root, (dirPrefix ++ List(fname)).mkString("/"))
    if(finalf.exists)
      return IOUtils.toByteArray(new FileInputStream(finalf))


    val conn = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    Option(range).foreach { case (start, len) =>
      conn.setRequestProperty("Range", s"bytes=${start}-${start + len - 1}")
    }

    val bytes =
      try {
        IOUtils.toByteArray(conn.getInputStream)
      } catch {
        case e: FileNotFoundException => new Array[Byte](0)
      }

    val tmpf = new File(root, "_" + filehash)
    val fos = new BufferedOutputStream(new FileOutputStream(tmpf))
    bytes.foreach { b => fos.write(b) }
    fos.flush()
    fos.close()
    tmpf.renameTo(finalf)
    bytes
  }

  private def parseCdxLine(line: String): CdxEntry = {
    val str = line.substring(line.indexOf("{"))

    mapper.readValue(str, classOf[CdxEntry])
  }

  def fetchWarc(url: String, range: (Int, Int)): Array[Byte] =
    fetchUrl(Some("warc"), url, range = range)

  def query(api: String, url: String): Seq[CdxEntry] =
    new String(
      fetchUrl(Some("cdx"), s"${api}?pageSize=1&url=${URLEncoder.encode(url, "UTF-8")}&filter=mime:html&filter==status:200"),
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

