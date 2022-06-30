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
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ExerciseSchema(
    id: Int,
    url: String,
    html_url: String,
    display_name: ModuleNameSchema,
    course: IntIdSchema,
    max_points: Int,
    max_submissions: Int,
    is_submittable: Boolean,
    host_name: String,
    metadata: ExerciseMetadataSchema,
    _links: Option[ExerciseLinksSchema]
)
extends BaseSchema

object ExerciseSchema extends BaseSchemaTrait2[ExerciseSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HtmlUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.DisplayName, false, ModuleNameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Course, false, IntIdSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.MaxPoints, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.MaxSubmissions, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.IsSubmittable, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Metadata, false, ExerciseMetadataSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Links, true, ExerciseLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ExerciseSchema] = {
        TupleUtils.toTuple[Int, String, String, ModuleNameSchema, IntIdSchema, Int, Int, Boolean, String,
                           ExerciseMetadataSchema, Option[ExerciseLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (ExerciseSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
