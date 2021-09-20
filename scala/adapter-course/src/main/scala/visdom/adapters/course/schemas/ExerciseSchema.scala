package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class ExerciseSchema(
    id: Int,
    display_name: NameSchema,
    is_submittable: Boolean,
    metadata: MetadataSchema
)
extends BaseSchema

object ExerciseSchema extends BaseSchemaTrait[ExerciseSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.DisplayName, false),
        FieldDataType(SnakeCaseConstants.IsSubmittable, false),
        FieldDataType(SnakeCaseConstants.Metadata, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ExerciseSchema] = {
        toOption(
            valueOptions.toTuple4,
            (
                (value: Any) => GeneralUtils.toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => GeneralUtils.toBooleanOption(value),
                (value: Any) => MetadataSchema.fromAny(value)
            )
        ) match {
            case Some((id: Int, displayName: NameSchema, isSubmittable: Boolean, metadata: MetadataSchema)) =>
                Some(ExerciseSchema(id, displayName, isSubmittable, metadata))
            case _ => None
        }
    }
}
