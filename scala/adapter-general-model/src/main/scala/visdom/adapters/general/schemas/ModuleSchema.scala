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


final case class ModuleSchema(
    id: Int,
    url: String,
    html_url: String,
    is_open: Boolean,
    display_name: ModuleNameSchema,
    course_id: Int,
    host_name: String,
    metadata: ModuleMetadataSchema,
    _links: Option[ModuleLinksSchema]
)
extends BaseSchema

object ModuleSchema extends BaseSchemaTrait2[ModuleSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HtmlUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.IsOpen, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.DisplayName, false, ModuleNameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.CourseId, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Metadata, false, ModuleMetadataSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Links, true, ModuleLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ModuleSchema] = {
        TupleUtils.toTuple[Int, String, String, Boolean, ModuleNameSchema, Int, String,
                           ModuleMetadataSchema, Option[ModuleLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (ModuleSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
