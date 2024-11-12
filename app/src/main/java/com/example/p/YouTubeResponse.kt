package com.example.p

data class YouTubeResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val snippet: Snippet
)

data class Snippet(
    val title: String,
    val channelTitle: String
)
