package visdom.adapter.gitlab.utils

import java.time.DateTimeException
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import visdom.adapter.gitlab.GitlabConstants.UtcTimeZone


object TimeUtils {
    def toUtcString(zonedDatetime: String): Option[String] = {
        try {
            Some(ZonedDateTime
                .parse(zonedDatetime)
                .withZoneSameInstant(ZoneId.of(UtcTimeZone))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            )
        }
        catch {
            case error: DateTimeException => None
        }
    }
}
