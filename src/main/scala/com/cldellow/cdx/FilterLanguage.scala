package com.cldellow.cdx

import java.io._
import com.github.luben.zstd._
import com.cldellow.warc.io.tsv._

object FilterLanguage {
  def main(args: Array[String]): Unit = {
    if(args.length < 2) {
      println("usage: ./filter-language <language> <file> [<file> ...]")
      println("   eg: ./filter-language eng 000001.zst")
      println()
      println("   You can set the CDX_ROOT variable to control where files are written to.")
      System.exit(1)
    }

    val language = args(0)
    val cdxPath = Option(System.getenv("CDX_ROOT")).getOrElse("./cache")
    val cdx = Cdx(cdxPath)

    val it: Iterator[TsvContext] = args.drop(1).map { fileName =>
      val it: Iterator[TsvContext] = new TsvIterator(new ZstdInputStream(new FileInputStream(fileName)))
      it
    }.reduce(_ ++ _)

    val crawlIdRe = "^s3://commoncrawl/crawl-data/(CC-MAIN-[0-9]+-[0-9]+)/.*".r

    val writer = new TsvWriter(System.out)

    it.foreach { context =>
      val api =
        context.warcUrl match {
          case crawlIdRe(api) =>
            cdx.collections.find(_.id == api)
              .getOrElse {
                println(s"could not find index with ID: ${api}")
                println(s"you may need to delete collinfo.json from ${cdxPath}")
                System.exit(1)
                null
              }
          case url: String =>
            println(s"could not extract crawl ID from: ${url}")
            System.exit(1)
            null
        }

      val rv = cdx.query(api.cdxApi, context.url)
      if(rv.exists(_.languages == List(language))) {
        writer.write(context)
        writer.flush()
      }
    }

    writer.flush()
  }
}
