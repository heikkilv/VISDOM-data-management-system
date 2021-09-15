package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ModuleSchema(
    id: Int,
    display_name: NameSchema,
    is_open: Boolean,
    course_id: Int,
    metadata: MetadataSchema,
    _links: Option[ModuleLinksSchema]
)
extends BaseSchema

object ModuleSchema extends BaseSchemaTrait[ModuleSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.DisplayName, false),
        FieldDataType(SnakeCaseConstants.IsOpen, false),
        FieldDataType(SnakeCaseConstants.CourseId, false),
        FieldDataType(SnakeCaseConstants.Metadata, false),
        FieldDataType(SnakeCaseConstants.Links, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ModuleSchema] = {
        val (
            idOption,
            displayPathOption,
            isOpenOption,
            courseIdOption,
            metadataOption,
            linksOption
        ) = valueOptions.toTuple6
        toOption(
            (
                idOption,
                displayPathOption,
                isOpenOption,
                courseIdOption,
                metadataOption
            ),
            (
                (value: Any) => toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => toBooleanOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => MetadataSchema.fromAny(value)
                )
        ) match {
            case Some(
                (
                    id: Int,
                    displayName: NameSchema,
                    isOpen: Boolean,
                    courseId: Int,
                    metadata: MetadataSchema
                )
            ) =>
                Some(
                    ModuleSchema(
                        id,
                        displayName,
                        isOpen,
                        courseId,
                        metadata,
                        ModuleLinksSchema.fromAny(linksOption)
                    )
                )
            case _ => None
        }
    }
}
