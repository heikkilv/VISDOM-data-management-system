package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.PascalCaseConstants
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class ModulePointDifficultySchema(
    empty: Option[Int],
    G: Option[Int],
    P: Option[Int]
)
extends BaseSchema

object ModulePointDifficultySchema extends BaseSchemaTrait[ModulePointDifficultySchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Empty, true),
        FieldDataType(PascalCaseConstants.G, true),
        FieldDataType(PascalCaseConstants.P, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ModulePointDifficultySchema] = {
        val (emptyOption, gOption, pOption) =
            valueOptions
                .map(valueOption => toIntOption(valueOption))
                .toTuple3
        Some(ModulePointDifficultySchema(emptyOption, gOption, pOption))
    }
}
