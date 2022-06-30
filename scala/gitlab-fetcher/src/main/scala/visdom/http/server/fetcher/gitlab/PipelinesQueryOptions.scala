// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class PipelinesQueryOptions(
    projectName: String,
    reference: String,
    startDate: Option[String],
    endDate: Option[String],
    includeReports: String,
    includeJobs: String,
    includeJobLogs: String,
    useAnonymization: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.ProjectName -> JsonUtils.toJsonValue(projectName),
                AttributeConstants.Reference -> JsonUtils.toJsonValue(reference),
                AttributeConstants.StartDate -> JsonUtils.toJsonValue(startDate),
                AttributeConstants.EndDate -> JsonUtils.toJsonValue(endDate),
                AttributeConstants.IncludeJobs -> JsonUtils.toJsonValue(includeJobs),
                AttributeConstants.IncludeJobLogs -> JsonUtils.toJsonValue(includeJobLogs),
                AttributeConstants.IncludeReports -> JsonUtils.toJsonValue(includeReports),
                AttributeConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization)
            )
        )
    }
}
