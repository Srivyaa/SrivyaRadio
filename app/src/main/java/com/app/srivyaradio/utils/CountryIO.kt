package com.app.srivyaradio.utils

import com.app.srivyaradio.data.models.CountryEntry
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.opencsv.CSVWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringReader
import java.nio.charset.StandardCharsets

object CountryIO {
    private const val HEADER_NAME = "name"
    private const val HEADER_CODE = "code"
    private const val HEADER_ACTIVE = "active"

    fun readCsv(input: InputStream): List<CountryEntry> {
        // Read the whole content to support separator auto-detection and re-parsing
        val text = input.readBytes().toString(StandardCharsets.UTF_8)
        if (text.isBlank()) return emptyList()

        // Normalize newlines and strip BOM if present
        var normalized = text.replace("\r\n", "\n").replace("\r", "\n")
        if (normalized.startsWith("\uFEFF")) normalized = normalized.removePrefix("\uFEFF")

        val rawLines = normalized.split('\n')
        var start = 0
        var sep: Char? = null
        if (rawLines.isNotEmpty()) {
            val first = rawLines[0].trim()
            if (first.lowercase().startsWith("sep=") && first.length >= 5) {
                sep = first[4]
                start = 1
            }
        }

        val content = rawLines.drop(start).joinToString("\n")
        val headerLine = rawLines.drop(start).firstOrNull()?.trim() ?: ""
        if (sep == null) {
            sep = when {
                '\t' in headerLine -> '\t'
                headerLine.count { it == ';' } > headerLine.count { it == ',' } -> ';'
                headerLine.contains(',') -> ','
                else -> ','
            }
        }

        val parser = CSVParserBuilder().withSeparator(sep!!).build()
        val csv = CSVReaderBuilder(StringReader(content)).withCSVParser(parser).build()
        val all = csv.readAll()?.toMutableList() ?: return emptyList()
        if (all.isEmpty()) return emptyList()

        // Filter out completely blank rows
        val rows = all.filter { row -> row.any { it?.trim()?.isNotEmpty() == true } }
        if (rows.isEmpty()) return emptyList()

        // Normalize potential header row
        val headerRaw = rows.first()
        val headerNorm = headerRaw.map { it.trim().lowercase().replace("\uFEFF", "") }

        fun findIndex(names: List<String>): Int {
            for (n in names) {
                val idx = headerNorm.indexOf(n)
                if (idx >= 0) return idx
            }
            return -1
        }

        var idxName = findIndex(listOf(HEADER_NAME))
        var idxCode = findIndex(listOf(HEADER_CODE, "countrycode", "country_code"))
        var idxActive = findIndex(listOf(HEADER_ACTIVE, "enabled", "is_active"))

        val dataStart = if (idxName >= 0 && idxCode >= 0) 1 else 0
        if (dataStart == 0) {
            // No header detected: assume first columns are name, code, [active]
            idxName = 0
            idxCode = 1
            idxActive = if (headerRaw.size > 2) 2 else -1
        }

        return rows.drop(dataStart).mapNotNull { r ->
            val name = r.getOrNull(idxName)?.trim().orEmpty()
            val code = r.getOrNull(idxCode)?.trim()?.uppercase().orEmpty()
            val active = r.getOrNull(idxActive)?.trim()?.let { toBool(it) } ?: true
            if (name.isNotBlank() && code.isNotBlank()) CountryEntry(name, code, active) else null
        }
    }

    fun writeCsv(out: OutputStream, entries: List<CountryEntry>, includeBom: Boolean = true) {
        if (includeBom) {
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        }
        OutputStreamWriter(out, StandardCharsets.UTF_8).use { osw ->
            val writer = CSVWriter(osw)
            try {
                // Excel-friendly separator hint for locales using ';' by default
                writer.writeNext(arrayOf("sep=,"), false)
                writer.writeNext(arrayOf(HEADER_NAME, HEADER_CODE, HEADER_ACTIVE))
                entries.forEach { e ->
                    writer.writeNext(arrayOf(e.name, e.code.uppercase(), if (e.active) "true" else "false"))
                }
                writer.flush()
            } finally {
                try { writer.close() } catch (_: Exception) { }
            }
        }
    }


    fun writeTemplateCsv(out: OutputStream) {
        writeCsv(out, sample(), includeBom = true)
    }

    private fun toBool(v: String): Boolean {
        return when (v.trim().lowercase()) {
            "true", "1", "y", "yes" -> true
            "false", "0", "n", "no" -> false
            else -> true
        }
    }

    private fun sample(): List<CountryEntry> = listOf(
        CountryEntry("Community_Radios \uD83C\uDFE1", "COMMUNITY", true),
        CountryEntry("Tamil_FM_Radios \uD83C\uDDEE\uD83C\uDDF3", "TAMILFM", true),
    )
}
