// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.fetcher.aplus

import spray.json.JsObject
import visdom.http.server.QueryOptionsBase
import visdom.http.server.services.constants.APlusServerConstants
import visdom.json.JsonUtils


final case class ExerciseDataQueryOptions(
    courseId: String,
    moduleId: Option[String],
    exerciseId: Option[String],
    parseNames: String,
    includeSubmissions: String,
    includeGitlabData: String,
    useAnonymization: String,
    gdprExerciseId: Option[String],
    gdprFieldName: String,
    gdprAcceptedAnswer: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                APlusServerConstants.CourseId -> JsonUtils.toJsonValue(courseId),
                APlusServerConstants.ModuleId -> JsonUtils.toJsonValue(moduleId),
                APlusServerConstants.ExerciseId -> JsonUtils.toJsonValue(exerciseId),
                APlusServerConstants.ParseNames -> JsonUtils.toJsonValue(parseNames),
                APlusServerConstants.IncludeSubmissions -> JsonUtils.toJsonValue(includeSubmissions),
                APlusServerConstants.IncludeGitlabData -> JsonUtils.toJsonValue(includeGitlabData),
                APlusServerConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization),
                APlusServerConstants.GDPRExerciseId -> JsonUtils.toJsonValue((gdprExerciseId)),
                APlusServerConstants.GDPRFieldName -> JsonUtils.toJsonValue((gdprFieldName)),
                APlusServerConstants.GDPRAcceptedAnswer -> JsonUtils.toJsonValue((gdprAcceptedAnswer))
            )
        )
    }
}
