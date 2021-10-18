package com.eddsteel.feedfilter

import org.slf4j.LoggerFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.XMLEvent
import java.io.InputStream
import java.io.Writer
import java.io.StringWriter

internal val inputFactory = XMLInputFactory.newInstance()
internal val outputFactory = XMLOutputFactory.newInstance()

private val logger = LoggerFactory.getLogger("feed-filter")

class FeedFilter(val predicate: (EpisodeMetadata) -> Boolean) {
    fun filter(input: InputStream): RSS {
        val reader = inputFactory.createXMLEventReader(input)
        val output = StringWriter()
        val writer = outputFactory.createXMLEventWriter(output)
        var partialItem = PartialItem()
        var filtering = false

        try {
            while (reader.hasNext()) {
                val nextEvent = reader.nextEvent()
                if (nextEvent.startElementName() == "item") {
                    partialItem = PartialItem(nextEvent)
                    filtering = true
                    continue
                }
                if (nextEvent.endElementName() == "item") {
                    partialItem = partialItem.add(nextEvent)
                    filtering = false
                    if (partialItem.meta?.let{ predicate(it) } ?: false) {
                        partialItem.buffer.forEach(writer::add)
                    } else {
                        logger.info("DROP {}", partialItem.title)
                    }
                    continue
                }
                if (filtering) {
                    partialItem = when (nextEvent.startElementName()) {
                        "title" -> reader.peekText()?.let { partialItem.copy(title = it) } ?: partialItem
                        "description" -> reader.peekText()?.let { partialItem.copy(description = it) } ?: partialItem
                        "link" -> reader.peekText()?.let { partialItem.copy(link = it) } ?: partialItem
                        else -> partialItem
                    }.add(nextEvent)
                    continue
                }
                writer.add(nextEvent)
            }
        } finally {
            reader.close()
            writer.close()
        }

        return RSS(output)
    }
}

fun XMLEventReader.peekText(): String? =
    peek().takeIf { it.isCharacters() }?.asCharacters()?.getData()

fun XMLEvent.startElementName(): String? =
    takeIf { it.isStartElement() }?.asStartElement()?.getName()?.getLocalPart()?.lowercase()

fun XMLEvent.endElementName(): String? =
    takeIf { it.isEndElement() }?.asEndElement()?.getName()?.getLocalPart()?.lowercase()

data class PartialItem(
    val title: String? = null,
    val description: String? = null,
    val link: String? = null,
    val buffer: List<XMLEvent> = emptyList()) {

    constructor(event: XMLEvent): this(null, null, null, listOf(event))

    fun add(event: XMLEvent): PartialItem = copy(buffer = this.buffer + event)
    val meta: EpisodeMetadata? =
        if (title != null && description != null && link != null) {
            EpisodeMetadata(link, title, description)
        } else null
}
