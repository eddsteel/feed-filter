package com.eddsteel.feedfilter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

fun feedFetcher(configPath: String): FeedFetcher {
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return mapper.readValue(File(configPath), FeedFetcher::class.java)
}

data class FeedFetcher(val feeds: List<FeedConfiguration>) {
    fun fetch(name: String?): RSS? {
        if (name == null) return null
        val config = feeds.find { it.name == name }
        if (config == null) return null

        val pred = StarlarkPredicate(config.rule)
        val stream = config.href.toURL().openStream()
        return FeedFilter(pred::test).filter(stream)
    }
}
