package com.rogr.swishpayoutclient.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val fmt = DateTimeFormatter.ISO_INSTANT

    fun getInstructionDate(): String =
        Instant.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .let(fmt::format) // e.g. 2025-08-22T17:21:12Z
}