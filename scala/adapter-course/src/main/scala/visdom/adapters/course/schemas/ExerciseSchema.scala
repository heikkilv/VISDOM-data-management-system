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
    max_points: Int,
    max_submissions: Int,
    metadata: MetadataSchema
)
extends BaseSchema

object ExerciseSchema extends BaseSchemaTrait[ExerciseSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.DisplayName, false),
        FieldDataType(SnakeCaseConstants.IsSubmittable, false),
        FieldDataType(SnakeCaseConstants.MaxPoints, false),
        FieldDataType(SnakeCaseConstants.MaxSubmissions, false),
        FieldDataType(SnakeCaseConstants.Metadata, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ExerciseSchema] = {
        toOption(
            valueOptions.toTuple6,
            (
                (value: Any) => GeneralUtils.toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => GeneralUtils.toBooleanOption(value),
                (value: Any) => GeneralUtils.toIntOption(value),
                (value: Any) => GeneralUtils.toIntOption(value),
                (value: Any) => MetadataSchema.fromAny(value)
            )
        ) match {
            case Some((
                id: Int,
                displayName: NameSchema,
                isSubmittable: Boolean,
                max_points: Int,
                max_submissions: Int,
                metadata: MetadataSchema
            )) =>
                Some(ExerciseSchema(id, displayName, isSubmittable, max_points, max_submissions, metadata))
            case _ => None
        }
    }
}
