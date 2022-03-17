package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
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
         val projectNames: Map[Int, String] = modelUtils.getProjectNameMap()

        modelUtils.getPipelineJobSchemas()
            .map(
                pipelineJobSchema => GitlabUserEventSchema(
                    hostName = pipelineJobSchema.host_name,
                    eventId = PipelineJobEvent.getId(
                        hostName = pipelineJobSchema.host_name,
                        projectName = projectNames.getOrElse(
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

    def getCommitterUsers(): Map[String, Seq[String]] = {
        ModelHelperUtils.getReverseMapping(
            modelUtils.getUserCommitterMap()
                .map({
                    case ((hostName, userId), committerIds) => (
                        ModelHelperUtils.getAuthorId(hostName, userId),
                        committerIds
                    )
                })
        )
    }

    def getCommitAuthors(): Dataset[CommitAuthorResult] = {
        val committerUsers: Map[String, Seq[String]] = getCommitterUsers()

        modelUtils.loadMongoDataGitlab[CommitAuthorSchema](MongoConstants.CollectionCommits)
            .flatMap(row => CommitAuthorSchema.fromRow(row))
            .groupByKey(authorSchema => (authorSchema.host_name, authorSchema.committer_email))
            .mapValues(
                authorSchema => CommitAuthorProcessedSchema(
                    committerEmail = authorSchema.committer_email,
                    committerName = authorSchema.committer_name,
                    hostName = authorSchema.host_name,
                    commitEventIds = Seq(
                        CommitEvent.getId(authorSchema.host_name, authorSchema.project_name, authorSchema.id)
                    ),
                    gitlabAuthorIds = committerUsers.getOrElse(
                        CommitAuthor.getId(GitlabOrigin.getId(authorSchema.host_name), authorSchema.committer_email),
                        Seq.empty
                    )
                )
            )
            .reduceGroups((first, second) => CommitAuthorProcessedSchema.reduceSchemas(first, second))
            .map({case (_, authorSchema) => ArtifactResult.fromCommitAuthorProcessedSchema(authorSchema)})
    }

    def getGitlabAuthors(): Dataset[GitlabAuthorResult] = {
        val authorCommits: Map[(String, Int), Seq[String]] = modelUtils.getUserCommitMap()
        val authorCommitters: Map[(String, Int), Seq[String]] = modelUtils.getUserCommitterMap()

        getPipelineUserEvents()
            .union(getPipelineJobUserEvents())
            .distinct()
            .groupByKey(schema => (schema.hostName, schema.userSchema.id))
            .mapValues(
                schema => (
                    schema.userSchema,
                    schema.eventType match {
                        case PipelineEvent.PipelineEventType => Seq(schema.eventId)
                        case _ => Seq.empty
                    },
                    schema.eventType match {
                        case PipelineJobEvent.PipelineJobEventType => Seq(schema.eventId)
                        case _ => Seq.empty
                    }
                )
            )
            // combine the event id lists and use the first found schema for each user
            .reduceGroups(
                (first, second) => (
                    first._1,
                    first._2 ++ second._2,
                    first._3 ++ second._3
                )
            )
            .map({
                case ((hostName, _), (userSchema, pipelineEventIds, pipelineJobEventIds)) =>
                    ArtifactResult.fromUserData(
                        pipelineUserSchema = userSchema,
                        hostName = hostName,
                        committerIds = authorCommitters.getOrElse((hostName, userSchema.id), Seq.empty),
                        commitEventIds = authorCommits.getOrElse((hostName, userSchema.id), Seq.empty),
                        pipelineEventIds = pipelineEventIds,
                        pipelineJobEventIds = pipelineJobEventIds
                    )
            })
    }
}
