package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.CommitAuthorResult
import visdom.adapters.general.model.results.ArtifactResult.GitlabAuthorResult
import visdom.adapters.general.schemas.CommitAuthorProcessedSchema
import visdom.adapters.general.schemas.CommitAuthorSchema
import visdom.adapters.general.schemas.GitlabUserEventSchema
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants


class ModelAuthorUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder

    private def getPipelineUserEvents(): Dataset[GitlabUserEventSchema] = {
        modelUtils.getPipelineSchemas()
            .map(
                pipelineSchema => GitlabUserEventSchema(
                    hostName = pipelineSchema.host_name,
                    eventId = PipelineEvent.getId(
                        hostName = pipelineSchema.host_name,
                        projectName = pipelineSchema.project_name,
                        pipelineId = pipelineSchema.id
                    ),
                    eventType = PipelineEvent.PipelineEventType,
                    userSchema = pipelineSchema.user
                )
            ).distinct()
    }

    private def getPipelineJobUserEvents(): Dataset[GitlabUserEventSchema] = {
         val pipelineProjectNames: Map[Int, String] = modelUtils.getPipelineProjectNames()

        modelUtils.getPipelineJobSchemas()
            .map(
                pipelineJobSchema => GitlabUserEventSchema(
                    hostName = pipelineJobSchema.host_name,
                    eventId = PipelineJobEvent.getId(
                        hostName = pipelineJobSchema.host_name,
                        projectName = pipelineProjectNames.getOrElse(
                            pipelineJobSchema.pipeline.id,
                            CommonConstants.EmptyString
                        ),
                        jobId = pipelineJobSchema.id
                    ),
                    eventType = PipelineJobEvent.PipelineJobEventType,
                    userSchema = pipelineJobSchema.user
                )
            )
            .distinct()
    }

    def getCommitAuthors(): Dataset[CommitAuthorResult] = {
        modelUtils.loadMongoData[CommitAuthorSchema](MongoConstants.CollectionCommits)
            .flatMap(row => CommitAuthorSchema.fromRow(row))
            .groupByKey(authorSchema => (authorSchema.host_name, authorSchema.committer_email))
            .mapValues(
                authorSchema => CommitAuthorProcessedSchema(
                    committerEmail = authorSchema.committer_email,
                    committerName = authorSchema.committer_name,
                    hostName = authorSchema.host_name,
                    commitEventIds = Seq(
                        CommitEvent.getId(authorSchema.host_name, authorSchema.project_name, authorSchema.id)
                    )
                )
            )
            .reduceGroups((first, second) => CommitAuthorProcessedSchema.reduceSchemas(first, second))
            .map({case (_, authorSchema) => ArtifactResult.fromCommitAuthorProcessedSchema(authorSchema)})
    }

    def getGitlabAuthors(): Dataset[GitlabAuthorResult] = {
        getPipelineUserEvents()
            .union(getPipelineJobUserEvents())
            .distinct()
            .groupByKey(schema => (schema.hostName, schema.userSchema.id))
            .mapValues(
                schema => (
                    schema.eventType match {
                        case PipelineEvent.PipelineEventType => Seq(schema.eventId)
                        case _ => Seq.empty
                    },
                    schema.eventType match {
                        case PipelineJobEvent.PipelineJobEventType => Seq(schema.eventId)
                        case _ => Seq.empty
                    },
                    schema.userSchema
                )
            )
            // combine the event lists and use the first found schema for each user
            .reduceGroups((first, second) => (first._1 ++ second._1, first._2 ++ second._2, first._3))
            .map({
                case ((hostName, _), (pipelineEventIds, pipelineJobEventIds, userSchema)) =>
                    ArtifactResult.fromUserData(
                        pipelineUserSchema = userSchema,
                        hostName = hostName,
                        pipelineEventIds = pipelineEventIds,
                        pipelineJobEventIds = pipelineJobEventIds
                    )
            })
    }
}
