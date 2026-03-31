package it.mygroup.org.data

import android.util.Log
import it.mygroup.org.network.CacciaPescaApi
import it.mygroup.org.network.RssItem
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

interface RssRepository {
    suspend fun getRssFeed(url: String): List<RssItem>
}

class NetworkRssRepository : RssRepository {
    override suspend fun getRssFeed(url: String): List<RssItem> {
        return try {
            val responseString = CacciaPescaApi.retrofitService.getRssFeed(url)
            Log.d("RssRepo", "Response Received: ${responseString.take(200)}...")

            // Sanitize XML to handle unescaped ampersands which cause XmlPullParserException
            val sanitizedXml = responseString.replace("&(?!(amp|lt|gt|quot|apos|#[0-9]+|#x[0-9a-fA-F]+);)".toRegex(), "&amp;")
            parseRss(sanitizedXml)
        } catch (e: Exception) {
            Log.e("RssRepo", "Error fetching RSS", e)
            throw e
        }
    }

    private fun parseRss(xml: String): List<RssItem> {
        val items = mutableListOf<RssItem>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var currentTitle = ""
            var currentLink = ""
            var currentPubDate = ""
            var currentDescription = ""
            var currentImageUrl: String? = null
            var insideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name?.lowercase()
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name == "item") {
                            insideItem = true
                        } else if (insideItem) {
                            when (name) {
                                "title" -> currentTitle = safeNextText(parser)
                                "link" -> currentLink = safeNextText(parser)
                                "pubdate" -> currentPubDate = safeNextText(parser)
                                "description" -> currentDescription = Jsoup.parse(safeNextText(parser)).text()
                                "media:content", "content" -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (url != null) currentImageUrl = url
                                }
                                "enclosure" -> {
                                    val type = parser.getAttributeValue(null, "type")
                                    if (type?.startsWith("image") == true) {
                                        currentImageUrl = parser.getAttributeValue(null, "url")
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name == "item") {
                            // If no image found in tags, try to extract from description HTML
                            if (currentImageUrl == null && currentDescription.isNotEmpty()) {
                                currentImageUrl = extractImageUrlFromHtml(currentDescription)
                            }

                            items.add(
                                RssItem(
                                    title = currentTitle.trim(),
                                    link = currentLink.trim(),
                                    pubDate = currentPubDate.trim(),
                                    description = currentDescription.trim(),
                                    imageUrl = currentImageUrl
                                )
                            )
                            // Reset for next item
                            insideItem = false
                            currentTitle = ""
                            currentLink = ""
                            currentPubDate = ""
                            currentDescription = ""
                            currentImageUrl = null
                        }
                    }
                }
                eventType = try {
                    parser.next()
                } catch (e: Exception) {
                    XmlPullParser.END_DOCUMENT
                }
            }
        } catch (e: Exception) {
            Log.e("RssRepo", "Error parsing XML", e)
        }
        return items
    }

    private fun safeNextText(parser: XmlPullParser): String {
        return try {
            parser.nextText() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractImageUrlFromHtml(html: String): String? {
        val imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>".toRegex(RegexOption.IGNORE_CASE)
        return imgRegex.find(html)?.groupValues?.get(1)
    }
}
