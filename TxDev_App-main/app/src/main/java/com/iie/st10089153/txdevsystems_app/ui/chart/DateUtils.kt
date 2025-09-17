package com.iie.st10089153.txdevsystems_app.ui.chart

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ISO_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
private val PRETTY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

fun isoStartOfDay(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay()
        .format(ISO_FMT)

fun isoEndOfDay(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atTime(LocalTime.MAX)
        .withNano(0)
        .format(ISO_FMT)

/** Convert ISO (with/without Z/offset) to dd-MM-yyyy for labels */
fun prettyIsoDate(iso: String): String {
    val ld: LocalDate = runCatching {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate()
    }.recoverCatching {
        OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    }.getOrElse {
        // Fallback: take yyyy-MM-dd substring
        LocalDate.parse(iso.substring(0, 10))
    }
    return ld.format(PRETTY_DATE)
}
