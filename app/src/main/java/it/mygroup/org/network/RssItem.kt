package it.mygroup.org.network

enum class RssCategory {
    CACCIA, PESCA, ALL
}

data class RssItem(
    val title: String,
    val link: String,
    val pubDate: String,
    val description: String,
    val imageUrl: String? = null,
    val category: RssCategory = RssCategory.ALL
)
