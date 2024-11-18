package com.example.p.Response

data class YouTubeResponse(
    val items: List<Item>?
)

data class Item(
    val snippet: Snippet?
)

data class Snippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails? // Thumbnails 객체를 포함
)

data class Thumbnails(
    val high: ThumbnailDetails? // high 품질 썸네일
)

data class ThumbnailDetails(
    val url: String? // 썸네일 URL
)