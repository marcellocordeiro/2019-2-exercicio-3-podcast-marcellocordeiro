package br.ufpe.cin.android.podcast

import br.ufpe.cin.android.podcast.db.ItemFeed
import br.ufpe.cin.android.podcast.helpers.DateHelper
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

object Parser {

    //Este metodo faz o parsing de RSS gerando objetos ItemFeed
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(rssFeed: String): List<ItemFeed> {
        val factory = XmlPullParserFactory.newInstance()
        val xpp = factory.newPullParser()
        xpp.setInput(StringReader(rssFeed))
        xpp.nextTag()
        return readRss(xpp)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readRss(parser: XmlPullParser): List<ItemFeed> {
        val items = ArrayList<ItemFeed>()
        parser.require(XmlPullParser.START_TAG, null, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "channel") {
                items.addAll(readChannel(parser))
            } else {
                skip(parser)
            }
        }
        return items
    }

    @Throws(IOException::class, XmlPullParserException::class)
    fun readChannel(parser: XmlPullParser): List<ItemFeed> {
        val items = ArrayList<ItemFeed>()
        parser.require(XmlPullParser.START_TAG, null, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "item") {
                items.add(readItem(parser))
            } else {
                skip(parser)
            }
        }
        return items
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readItem(parser: XmlPullParser): ItemFeed {
        var title: String? = null
        var link: String? = null
        var length: Int? = null
        var pubDate: Long? = null
        var description: String? = null
        var imageLink: String? = null
        var downloadLink: String? = null
        parser.require(XmlPullParser.START_TAG, null, "item")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readData(parser, "title")
                "link" -> link = readData(parser, "link")
                "pubDate" -> pubDate = DateHelper.parseToLong(readData(parser, "pubDate"))
                "description" -> description = readData(parser, "description")
                "itunes:image" -> imageLink = readAttribute(parser, "itunes:image", "href")
                "enclosure" -> {
                    parser.require(XmlPullParser.START_TAG, null, "enclosure")
                    downloadLink = parser.getAttributeValue(null, "url")
                    length = parser.getAttributeValue(null, "length").toInt()
                    parser.nextTag()
                    parser.require(XmlPullParser.END_TAG, null, "enclosure")
                }
                else -> skip(parser)
            }
        }
        return ItemFeed(
            title!!,
            link!!,
            length!!,
            pubDate!!,
            description!!,
            imageLink ?: "",
            downloadLink!!
        )
    }

    // Processa atributos de forma parametrizada no feed.
    @Throws(IOException::class, XmlPullParserException::class)
    fun readAttribute(parser: XmlPullParser, tag: String, attribute: String): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        val data = parser.getAttributeValue(null, attribute)
        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, null, tag)
        return data
    }

    // Processa tags de forma parametrizada no feed.
    @Throws(IOException::class, XmlPullParserException::class)
    fun readData(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        val data = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, tag)
        return data
    }

    @Throws(IOException::class, XmlPullParserException::class)
    fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    /**/

    //Este metodo faz o parsing do titulo e banner do RSS
    @Throws(XmlPullParserException::class, IOException::class)
    fun parseInfo(rssFeed: String): Pair<String?, String?> {
        val factory = XmlPullParserFactory.newInstance()
        val xpp = factory.newPullParser()
        xpp.setInput(StringReader(rssFeed))
        xpp.nextTag()
        return readRssInfo(xpp)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readRssInfo(parser: XmlPullParser): Pair<String?, String?> {
        parser.require(XmlPullParser.START_TAG, null, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "channel") {
                return readInfo(parser)
            } else {
                skip(parser)
            }
        }
        return Pair(null, null)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    fun readInfo(parser: XmlPullParser): Pair<String?, String?> {
        var rssTitle: String? = null
        var rssImageLink: String? = null

        parser.require(XmlPullParser.START_TAG, null, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> rssTitle = readData(parser, "title")
                "image" -> rssImageLink = readImage(parser)
                else -> skip(parser)
            }
        }
        return Pair(rssTitle, rssImageLink)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readImage(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "image")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "url" -> return readData(parser, "url")
                else -> skip(parser)
            }
        }
        return null
    }
}
