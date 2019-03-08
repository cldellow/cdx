package com.cldellow.cdx

import java.util.zip._
import java.io._
import org.apache.commons.io.IOUtils

object Fetch {
  def main(args: Array[String]): Unit = {
    if(args.length < 2) {
      println("usage: ./fetch <index> <url-1> <url-2>")
      println("   eg: ./fetch CC-MAIN-2018-51 https://kwknittersguild.ca/fair/")
      println()
      println("   You can set the CDX_ROOT variable to control where files are written to.")
      System.exit(1)
    }

    val index = args(0)
    val cdxPath = Option(System.getenv("CDX_ROOT")).getOrElse("./cache")
    val cdx = Cdx(cdxPath)
    val api = cdx.collections.filter(_.id == index).headOption

    api match {
      case None =>
        println(s"${index} is not a valid index.")
        System.exit(1)
      case Some(api) =>
        args.drop(1).foreach { url =>
          cdx.query(api.cdxApi, url).headOption.foreach { entry =>
            val zis = new GZIPInputStream(new ByteArrayInputStream(cdx.fetchWarc(entry.s3Url, entry.range)))
            IOUtils.copy(zis, System.out)
          }
        }
    }
  }
}
