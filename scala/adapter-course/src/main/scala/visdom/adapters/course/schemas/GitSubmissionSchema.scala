package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class GitSubmissionSchema(
    host_name: Option[String],
    project_name: Option[String]
)
extends BaseSchema

object GitSubmissionSchema extends BaseSchemaTrait[GitSubmissionSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.HostName, true),
        FieldDataType(SnakeCaseConstants.ProjectName, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[GitSubmissionSchema] = {
        val (hostNameOption, projectNameOption) =
            valueOptions
                .map(valueOption => toStringOption(valueOption))
                .toTuple2
        Some(GitSubmissionSchema(hostNameOption, projectNameOption))
    }
}
