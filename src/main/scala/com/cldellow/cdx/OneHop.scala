package com.cldellow.cdx

import java.io._
import java.util.zip._
import org.apache.commons.io.IOUtils
import org.jsoup._
import scala.collection.JavaConverters._

object OneHop {
  def main(args: Array[String]): Unit = {
    if(args.length < 2) {
      println("usage: ./one-hop <index> <url-1> <url-2>")
      println("   eg: ./one-hop CC-MAIN-2018-51 https://kwknittersguild.ca/fair/")
      println()
      println("   You can set the CDX_ROOT variable to control where files are written to.")
      System.exit(1)
    }

    val index = args(0)
    val cdxPath = Option(System.getenv("CDX_ROOT")).getOrElse("./cache")
    val cdx = Cdx(cdxPath)
    val api = cdx.collections.filter(_.id == index).headOption

    def normalizeUrl(url: String): String =
      url
        .stripPrefix("http://")
        .stripPrefix("https://")
        .stripPrefix("www.")
        .replaceAll("#.*", "")
        .replaceAll("/$", "")


    api match {
      case None =>
        println(s"${index} is not a valid index.")
        System.exit(1)
      case Some(api) =>
        args.drop(1).foreach { url =>
          val host = url.stripPrefix("http://").stripPrefix("https://").stripPrefix("www.").takeWhile(_ != '/')
          cdx.query(api.cdxApi, url).headOption.foreach { entry =>
            val rawStr = new String(
              IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(cdx.fetchWarc(entry.s3Url, entry.range)))),
              "UTF-8"
            )
            val docStr = rawStr.substring(rawStr.indexOf("\r\n\r\n") + 4)
            val doc = Jsoup.parse(docStr, url)
            val links = doc.select("a[href]")

            val allUrls = links.eachAttr("abs:href").asScala.filter { url =>
              val isAbs = url.startsWith("http://") || url.startsWith("https://")

              isAbs && host == url.stripPrefix("http://").stripPrefix("https://").stripPrefix("www.").takeWhile(_ != '/')
            }

            val top10Links = allUrls.take(10)

            val mapWords = Set(
              "directions",
              "venue",
              "contact",
              "about",
              "map",
              "location",
              "get-here",
              "getting-here"
            )

            val probablyMapRelated =
              allUrls
                .flatMap(url => mapWords.find(mapWord => url.contains(mapWord)).map(_ -> url))
                .groupBy(_._1)
                .values
                .map(_.head._2)

            val urls = (List(url) ++ top10Links ++ probablyMapRelated)
              .map(normalizeUrl)
              .distinct

            println(urls)

            var taken: Set[String] = Set.empty
            def shouldTake(f: String): Boolean = {
              if(taken.contains(f))
                return false

              taken = taken + f
              true
            }
            cdx.query(api.cdxApi, host + "/*")
              .filter { entry => urls.contains(normalizeUrl(entry.url)) && shouldTake(normalizeUrl(entry.url)) }
              .foreach { entry =>
                val zis = new GZIPInputStream(new ByteArrayInputStream(cdx.fetchWarc(entry.s3Url, entry.range)))
                IOUtils.copy(zis, System.out)

              }
          }
        }
    }
  }
}
