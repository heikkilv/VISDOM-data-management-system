package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.model.results.EventResult
import visdom.adapters.general.model.results.EventResult.CommitEventResult
import visdom.adapters.general.model.results.EventResult.PipelineEventResult
import visdom.adapters.general.model.results.EventResult.PipelineJobEventResult
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants


class ModelEventUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getCommitSchemas(): Dataset[CommitSchema] = {
        modelUtils.loadMongoData[CommitSchema](MongoConstants.CollectionCommits)
            .flatMap(row => CommitSchema.fromRow(row))
    }

    def getCommitJobs(): Map[String, Seq[Int]] = {
        modelUtils.getPipelineJobSchemas()
            .map(jobSchema => (jobSchema.id, jobSchema.commit.id))
            .collect()
            .groupBy({case (_, commitId) => commitId})
            .map({
                case (commitId, jobIdArray) => (
                    commitId,
                    jobIdArray.map({case (jobId, _) => jobId}).toSeq
                )
            })
    }

    def getCommits(): Dataset[CommitEventResult] = {
        val commitJobs: Map[String, Seq[Int]] = getCommitJobs()

        getCommitSchemas()
            .map(
                commitSchema =>
                    EventResult.fromCommitSchema(
                        commitSchema,
                        commitJobs.getOrElse(commitSchema.id, Seq.empty)
                    )
            )
    }

    def getPipelines(): Dataset[PipelineEventResult] = {
        modelUtils.getPipelineSchemas()
            .map(pipelineSchema => EventResult.fromPipelineSchema(pipelineSchema))
    }

    def getPipelineJobs(): Dataset[PipelineJobEventResult] = {
        val projectNames: Map[Int, String] = modelUtils.getProjectNameMap()

        modelUtils.getPipelineJobSchemas()
            // include only the jobs that have a known project name
            .filter(pipelineJob => projectNames.keySet.contains(pipelineJob.pipeline.id))
            .map(
                pipelineJobSchema =>
                    EventResult.fromPipelineJobSchema(
                        pipelineJobSchema,
                        projectNames.getOrElse(pipelineJobSchema.pipeline.id, CommonConstants.EmptyString)
                    )
            )
    }

}
