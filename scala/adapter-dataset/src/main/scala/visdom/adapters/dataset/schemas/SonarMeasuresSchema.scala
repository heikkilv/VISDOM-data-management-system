// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SonarMeasuresSchema(
    project_id: String,
    analysis_key: String,
    duplicated_lines: Int,
    duplicated_blocks: Int,
    duplicated_files: Int,
    violations: Int,
    blocker_violations: Int,
    critical_violations: Int,
    major_violations: Int,
    minor_violations: Int,
    info_violations: Int,
    alert_status: String,
    open_issues: Int,
    code_smells: Int,
    bugs: Int,
    vulnerabilities: Int,
    reliability_rating: Int,
    security_rating: Int,
    lines: Option[Int],
    files: Option[Int],
    comment_lines: Option[Int],
    last_commit_date: Option[String]
)
extends BaseSchema

object SonarMeasuresSchema extends BaseSchemaTrait2[SonarMeasuresSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AnalysisKey, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.DuplicatedLines, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.DuplicatedBlocks, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.DuplicatedFiles, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Violations, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.BlockerViolations, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.CriticalViolations, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.MajorViolations, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.MinorViolations, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.InfoViolations, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.AlertStatus, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.OpenIssues, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.CodeSmells, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Bugs, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Vulnerabilities, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.ReliabilityRating, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SecurityRating, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Lines, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.Files, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.CommentLines, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.LastCommitDate, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SonarMeasuresSchema] = {
        TupleUtils.toTuple[String, String, Int, Int, Int, Int, Int, Int, Int, Int, Int, String, Int, Int, Int, Int,
                           Int, Int, Option[Int], Option[Int], Option[Int], Option[String]](values) match {
            case Some(inputValues) => Some(
                (SonarMeasuresSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
