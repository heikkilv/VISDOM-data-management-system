// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.dataset.schemas.SonarMeasuresSchema
import visdom.adapters.general.model.base.Data
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class SonarMeasuresData(
    analysis_key: String,
    lines: Option[Int],
    comment_lines: Option[Int],
    files: Option[Int],
    bugs: Int,
    vulnerabilities: Int,
    code_smells: Int,
    open_issues: Int,
    duplications: SonarDuplicationsData,
    violations: SonarViolationsData,
    ratings: SonarRatingsData,
    last_commit_date: Option[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.AnalysisKey -> JsonUtils.toBsonValue(analysis_key),
                SnakeCaseConstants.Lines -> JsonUtils.toBsonValue(lines),
                SnakeCaseConstants.CommentLines -> JsonUtils.toBsonValue(comment_lines),
                SnakeCaseConstants.Files -> JsonUtils.toBsonValue(files),
                SnakeCaseConstants.Bugs -> JsonUtils.toBsonValue(bugs),
                SnakeCaseConstants.Vulnerabilities -> JsonUtils.toBsonValue(vulnerabilities),
                SnakeCaseConstants.CodeSmells -> JsonUtils.toBsonValue(code_smells),
                SnakeCaseConstants.OpenIssues -> JsonUtils.toBsonValue(open_issues),
                SnakeCaseConstants.Duplications -> duplications.toBsonValue(),
                SnakeCaseConstants.Violations -> violations.toBsonValue(),
                SnakeCaseConstants.Ratings -> ratings.toBsonValue(),
                SnakeCaseConstants.LastCommitDate -> JsonUtils.toBsonValue(last_commit_date)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.AnalysisKey -> JsonUtils.toJsonValue(analysis_key),
                SnakeCaseConstants.Lines -> JsonUtils.toJsonValue(lines),
                SnakeCaseConstants.CommentLines -> JsonUtils.toJsonValue(comment_lines),
                SnakeCaseConstants.Files -> JsonUtils.toJsonValue(files),
                SnakeCaseConstants.Bugs -> JsonUtils.toJsonValue(bugs),
                SnakeCaseConstants.Vulnerabilities -> JsonUtils.toJsonValue(vulnerabilities),
                SnakeCaseConstants.CodeSmells -> JsonUtils.toJsonValue(code_smells),
                SnakeCaseConstants.OpenIssues -> JsonUtils.toJsonValue(open_issues),
                SnakeCaseConstants.Duplications -> duplications.toJsValue(),
                SnakeCaseConstants.Violations -> violations.toJsValue(),
                SnakeCaseConstants.Ratings -> ratings.toJsValue(),
                SnakeCaseConstants.LastCommitDate -> JsonUtils.toJsonValue(last_commit_date)
            )
        )
    }
}

object SonarMeasuresData {
    def fromMeasuresSchema(sonarMeasuresSchema: SonarMeasuresSchema): SonarMeasuresData = {
        SonarMeasuresData(
            analysis_key = sonarMeasuresSchema.analysis_key,
            lines = sonarMeasuresSchema.lines,
            comment_lines = sonarMeasuresSchema.comment_lines,
            files = sonarMeasuresSchema.files,
            bugs = sonarMeasuresSchema.bugs,
            vulnerabilities = sonarMeasuresSchema.vulnerabilities,
            code_smells = sonarMeasuresSchema.code_smells,
            open_issues = sonarMeasuresSchema.open_issues,
            duplications = SonarDuplicationsData(
                lines = sonarMeasuresSchema.duplicated_lines,
                blocks = sonarMeasuresSchema.duplicated_blocks,
                files = sonarMeasuresSchema.duplicated_files
            ),
            violations = SonarViolationsData(
                blocker = sonarMeasuresSchema.blocker_violations,
                critical = sonarMeasuresSchema.critical_violations,
                major = sonarMeasuresSchema.major_violations,
                minor = sonarMeasuresSchema.minor_violations,
                info = sonarMeasuresSchema.info_violations,
                total = sonarMeasuresSchema.violations
            ),
            ratings = SonarRatingsData(
                reliability = sonarMeasuresSchema.reliability_rating,
                security = sonarMeasuresSchema.security_rating
            ),
            last_commit_date = sonarMeasuresSchema.last_commit_date
        )
    }
}
