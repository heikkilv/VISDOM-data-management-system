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


final case class CourseSchema(
    id: Int,
    url: String,
    html_url: String,
    code: String,
    instance_name: String,
    language: String,
    starting_time: String,
    ending_time: String,
    visible_to_students: Boolean,
    name: CourseNameSchema,
    host_name: String,
    metadata: CourseMetadataSchema,
    _links: Option[CourseLinksSchema]
)
extends BaseSchema

object CourseSchema extends BaseSchemaTrait2[CourseSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HtmlUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Code, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.InstanceName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Language, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.StartingTime, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.EndingTime, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.VisibleToStudents, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.Name, false, CourseNameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Metadata, false, CourseMetadataSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Links, true, CourseLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CourseSchema] = {
        TupleUtils.toTuple[Int, String, String, String, String, String, String, String, Boolean, CourseNameSchema,
                           String, CourseMetadataSchema, Option[CourseLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (CourseSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
