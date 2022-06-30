// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab.results

import spray.json.JsArray
import spray.json.JsString
import spray.json.JsValue
import visdom.adapter.gitlab.utils.TimeUtils.toUtcString


final case class TimestampResult(
    project_name: String,
    path: String,
    timestamps: Array[String]
) {
    def toJsonTuple(): (String, String, JsValue) = {
        (
            project_name,
            path,
            JsArray(
                timestamps
                    .map(timestamp => {
                        toUtcString(timestamp) match {
                            case Some(utcDateTimeString: String) => Some(JsString(utcDateTimeString))
                            case None => None
                        }
                    })
                    .flatten
                    .toVector
            )
        )
    }
}
