package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ExerciseSchema(
    id: Int,
    display_name: NameSchema,
    metadata: MetadataSchema
)
extends BaseSchema

object ExerciseSchema extends BaseSchemaTrait[ExerciseSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.DisplayName, false),
        FieldDataType(SnakeCaseConstants.Metadata, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ExerciseSchema] = {
        GeneralUtils.toOption(
            valueOptions.toTuple3,
            (
                (value: Any) => GeneralUtils.toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => MetadataSchema.fromAny(value)
            )
        ) match {
            case Some((id: Int, displayName: NameSchema, metadata: MetadataSchema)) =>
                Some(ExerciseSchema(id, displayName, metadata))
            case _ => None
        }
    }
}
