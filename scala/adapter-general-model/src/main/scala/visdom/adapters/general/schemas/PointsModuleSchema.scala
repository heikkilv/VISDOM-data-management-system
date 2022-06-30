// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PointsModuleSchema(
    id: Int,
    name: ModuleNameSchema,
    max_points: Int,
    points_to_pass: Int,
    submission_count: Int,
    points: Int,
    points_by_difficulty: PointsDifficultySchema,
    passed: Boolean,
    exercises: Seq[PointsExerciseSchema]
)
extends BaseSchema

object PointsModuleSchema extends BaseSchemaTrait2[PointsModuleSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Name, false, ModuleNameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.MaxPoints, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.PointsToPass, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SubmissionCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Points, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.PointsByDifficulty, false, PointsDifficultySchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Passed, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.Exercises, false, (value: Any) => toSeqOption(value, PointsExerciseSchema.fromAny))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PointsModuleSchema] = {
        TupleUtils.toTuple[Int, ModuleNameSchema, Int, Int, Int, Int, PointsDifficultySchema,
                           Boolean, Seq[PointsExerciseSchema]](values) match {
            case Some(inputValues) => Some(
                (PointsModuleSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
