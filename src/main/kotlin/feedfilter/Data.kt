package com.eddsteel.feedfilter

import java.io.StringWriter
import java.net.URI

data class RSS(val data: String) {
    constructor(writer: StringWriter): this(writer.toString())
}

data class EpisodeMetadata(
    val url: URI,
    val title: String,
    val description: String) {
    constructor(url: String, title: String, desc: String):
        this(URI(url), escape(title), escape(desc))

    val stl =
        """dict(url="$url", description="$description", title="$title")"""
}

data class FeedConfiguration(
    val name: String,
    val href: URI,
    val rule: String
)

private fun escape(s: String) = s.replace("\"", "\\\"").replace("\n", "\\\n")
