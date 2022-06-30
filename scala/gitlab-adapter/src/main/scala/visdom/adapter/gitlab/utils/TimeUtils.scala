// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
