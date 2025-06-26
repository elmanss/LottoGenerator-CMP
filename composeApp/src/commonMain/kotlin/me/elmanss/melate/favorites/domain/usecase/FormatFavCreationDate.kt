package me.elmanss.melate.favorites.domain.usecase

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.toLocalDateTime

/*
// import kotlinx.datetime.format.*

println(DateTimeFormat.formatAsKotlinBuilderDsl(DateTimeComponents.Format {
    byUnicodePattern("uuuu-MM-dd'T'HH:mm:ss[.SSS]Z")
}))

// will print:
/*
date(LocalDate.Formats.ISO)
char('T')
hour()
char(':')
minute()
char(':')
second()
alternativeParsing({
}) {
    char('.')
    secondFraction(3)
}
offset(UtcOffset.Formats.FOUR_DIGITS)
 */
 */

class FormatFavCreationDate(val dateTimeFormat: DateTimeFormat<LocalDateTime>) {
  @OptIn(FormatStringsInDatetimeFormats::class)
  operator fun invoke(createdAt: Long): String {
    val datetime = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.UTC)
    return dateTimeFormat.format(datetime)
  }
}
