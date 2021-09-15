package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ModuleSchema(
    id: Int,
    name: NameSchema,
    exercises: Seq[ExercisePointsSchema]
)
extends BaseSchema

object ModuleSchema extends BaseSchemaTrait[ModuleSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.Name, false),
        FieldDataType(SnakeCaseConstants.Exercises, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ModuleSchema] = {
        toOption(
            valueOptions.toTuple3,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => toSeqOption(value, ExercisePointsSchema.fromAny)
            )
        ) match {
            case Some((id: Int, name: NameSchema, exercises: Seq[ExercisePointsSchema])) =>
                Some(
                    ModuleSchema(
                        id,
                        name,
                        exercises
                    )
                )
            case _ => None
        }
    }
}
