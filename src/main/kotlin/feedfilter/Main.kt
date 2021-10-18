package com.eddsteel.feedfilter

fun main() {
    val configPath = envOr("FEEDS", "./src/dist/etc/feeds.yaml")
    val feeds = feedFetcher(configPath)
    serve(feeds)
}

private fun envOr(name: String, default: String) = System.getenv(name) ?: default

