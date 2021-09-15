package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CourseSchema(
    id: Int,
    name: CourseNameSchema,
    instance_name: String,
    starting_time: String,
    ending_time: String,
    _links: Option[CourseLinksSchema]
)
extends BaseSchema

object CourseSchema extends BaseSchemaTrait[CourseSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.Name, false),
        FieldDataType(SnakeCaseConstants.InstanceName, false),
        FieldDataType(SnakeCaseConstants.StartingTime, false),
        FieldDataType(SnakeCaseConstants.EndingTime, false),
        FieldDataType(SnakeCaseConstants.Links, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CourseSchema] = {
        val (
            idOption,
            nameOption,
            instanceNameOption,
            startingTimeOption,
            endingTimeOption,
            linksOption
        ) = valueOptions.toTuple6
        toOption(
            (
                idOption,
                nameOption,
                instanceNameOption,
                startingTimeOption,
                endingTimeOption
            ),
            (
                (value: Any) => toIntOption(value),
                (value: Any) => CourseNameSchema.fromAny(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value)
            )
        ) match {
            case Some(
                (
                    id: Int,
                    name: CourseNameSchema,
                    instance_name: String,
                    starting_time: String,
                    ending_time: String
                )
            ) =>
                Some(
                    CourseSchema(
                        id,
                        name,
                        instance_name,
                        starting_time,
                        ending_time,
                        CourseLinksSchema.fromAny(linksOption) match {
                            case Some(links: CourseLinksSchema) => Some(links)
                            case _ => None
                        }
                    )
                )
            case _ => None
        }
    }
}
