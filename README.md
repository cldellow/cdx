# cdx

A subset of https://github.com/ikreymer/cdx-index-client

Designed to make it easier to create subsets of the Common
Crawl, for manipulation in other programs.

## Usage

```
./fetch CC-MAIN-2018-51 https://kwknittersguild.ca/fair/ # print out 1 200 OK copy of the URL
```

```
./one-hop CC-MAIN-2018-51 https://kwknittersguild.ca/fair/ # print out 1 200 OK copy of the URL and its first 10 internal links
```

## Cleanup

Files are stored in `./cache/{cdx,warc,misc}` by default.

You can change the default path of `./cache` by overriding the `CDX_ROOT` environment variable.
