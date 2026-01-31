package com.kidsenglishsongs.player.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LrcParserTest {

    private lateinit var lrcParser: LrcParser

    @Before
    fun setup() {
        lrcParser = LrcParser()
    }

    @Test
    fun `parse extracts metadata correctly`() {
        val lrcContent = """
            [ti:Test Song]
            [ar:Test Artist]
            [al:Test Album]
            [00:00.00]First line
            [00:05.00]Second line
        """.trimIndent()

        val result = lrcParser.parse(lrcContent)

        assertEquals("Test Song", result.title)
        assertEquals("Test Artist", result.artist)
        assertEquals("Test Album", result.album)
    }

    @Test
    fun `parse extracts lyrics with correct timestamps`() {
        val lrcContent = """
            [00:00.00]First line
            [00:05.50]Second line
            [00:10.00]Third line
        """.trimIndent()

        val result = lrcParser.parse(lrcContent)

        assertEquals(3, result.lines.size)
        assertEquals(0L, result.lines[0].timeMs)
        assertEquals("First line", result.lines[0].text)
        assertEquals(5500L, result.lines[1].timeMs)
        assertEquals("Second line", result.lines[1].text)
        assertEquals(10000L, result.lines[2].timeMs)
    }

    @Test
    fun `parse handles three digit milliseconds`() {
        val lrcContent = "[00:05.500]Lyric line"

        val result = lrcParser.parse(lrcContent)

        assertEquals(1, result.lines.size)
        assertEquals(5500L, result.lines[0].timeMs)
    }

    @Test
    fun `parse handles two digit milliseconds`() {
        val lrcContent = "[00:05.50]Lyric line"

        val result = lrcParser.parse(lrcContent)

        assertEquals(1, result.lines.size)
        assertEquals(5500L, result.lines[0].timeMs)
    }

    @Test
    fun `parse skips empty lyric lines`() {
        val lrcContent = """
            [00:00.00]First line
            [00:05.00]
            [00:10.00]Third line
        """.trimIndent()

        val result = lrcParser.parse(lrcContent)

        assertEquals(2, result.lines.size)
        assertEquals("First line", result.lines[0].text)
        assertEquals("Third line", result.lines[1].text)
    }

    @Test
    fun `parse sorts lyrics by timestamp`() {
        val lrcContent = """
            [00:10.00]Third line
            [00:00.00]First line
            [00:05.00]Second line
        """.trimIndent()

        val result = lrcParser.parse(lrcContent)

        assertEquals("First line", result.lines[0].text)
        assertEquals("Second line", result.lines[1].text)
        assertEquals("Third line", result.lines[2].text)
    }

    @Test
    fun `parse handles minutes correctly`() {
        val lrcContent = "[02:30.00]Two and half minutes"

        val result = lrcParser.parse(lrcContent)

        assertEquals(1, result.lines.size)
        assertEquals(150000L, result.lines[0].timeMs) // 2*60*1000 + 30*1000
    }

    @Test
    fun `getCurrentLineIndex returns correct index`() {
        val lines = listOf(
            LrcParser.LyricLine(0L, "First"),
            LrcParser.LyricLine(5000L, "Second"),
            LrcParser.LyricLine(10000L, "Third")
        )

        assertEquals(-1, lrcParser.getCurrentLineIndex(lines, -100L))
        assertEquals(0, lrcParser.getCurrentLineIndex(lines, 0L))
        assertEquals(0, lrcParser.getCurrentLineIndex(lines, 3000L))
        assertEquals(1, lrcParser.getCurrentLineIndex(lines, 5000L))
        assertEquals(1, lrcParser.getCurrentLineIndex(lines, 7000L))
        assertEquals(2, lrcParser.getCurrentLineIndex(lines, 10000L))
        assertEquals(2, lrcParser.getCurrentLineIndex(lines, 15000L))
    }

    @Test
    fun `getCurrentLineIndex returns -1 for empty list`() {
        val result = lrcParser.getCurrentLineIndex(emptyList(), 5000L)

        assertEquals(-1, result)
    }

    @Test
    fun `parse returns empty lines for invalid content`() {
        val lrcContent = "This is not valid LRC content"

        val result = lrcParser.parse(lrcContent)

        assertTrue(result.lines.isEmpty())
        assertNull(result.title)
    }

    @Test
    fun `parse handles mixed valid and invalid lines`() {
        val lrcContent = """
            [ti:Test Song]
            Invalid line without timestamp
            [00:05.00]Valid lyric line
            Another invalid line
        """.trimIndent()

        val result = lrcParser.parse(lrcContent)

        assertEquals("Test Song", result.title)
        assertEquals(1, result.lines.size)
        assertEquals("Valid lyric line", result.lines[0].text)
    }
}
