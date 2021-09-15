package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ModulePointSchema(
    id: Int,
    name: NameSchema,
    exercises: Seq[ExercisePointsSchema]
)
extends BaseSchema

object ModulePointSchema extends BaseSchemaTrait[ModulePointSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.Name, false),
        FieldDataType(SnakeCaseConstants.Exercises, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ModulePointSchema] = {
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
                    ModulePointSchema(
                        id,
                        name,
                        exercises
                    )
                )
            case _ => None
        }
    }
}
