// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class FileQueryOptions(
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: String,
    includeCommitLinks: String,
    useAnonymization: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.ProjectName -> JsonUtils.toJsonValue(projectName),
                AttributeConstants.Reference -> JsonUtils.toJsonValue(reference),
                AttributeConstants.FilePath -> JsonUtils.toJsonValue(filePath),
                AttributeConstants.Recursive -> JsonUtils.toJsonValue(recursive),
                AttributeConstants.IncludeCommitLinks -> JsonUtils.toJsonValue(includeCommitLinks),
                AttributeConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization)
            )
        )
    }
}
