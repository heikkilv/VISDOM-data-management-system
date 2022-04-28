package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.results.EventResult
import visdom.adapters.general.model.results.EventResult.CommitEventResult
import visdom.adapters.general.model.results.EventResult.PipelineEventResult
import visdom.adapters.general.model.results.EventResult.PipelineJobEventResult
import visdom.adapters.general.model.results.EventResult.SubmissionEventResult
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.adapters.general.schemas.SubmissionGitDataSchema
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants


class ModelEventUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder

    def getCommitSchemas(): Dataset[CommitSchema] = {
        modelUtils.loadMongoDataGitlab[CommitSchema](MongoConstants.CollectionCommits)
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

    def getCommitUsers(): Map[String, Seq[String]] = {
        ModelHelperUtils.getReverseMapping(
            modelUtils.getUserCommitMap()
                .map({
                    case ((hostName, userId), commitEventIds) => (
                        ModelHelperUtils.getAuthorId(hostName, userId),
                        commitEventIds
                    )
                })
        )
    }

    def getCommits(): Dataset[CommitEventResult] = {
        val commitJobs: Map[String, Seq[Int]] = getCommitJobs()
        val commitUsers: Map[String, Seq[String]] = getCommitUsers()

        getCommitSchemas()
            .map(
                commitSchema =>
                    EventResult.fromCommitSchema(
                        commitSchema,
                        commitJobs.getOrElse(commitSchema.id, Seq.empty),
                        commitUsers.getOrElse(
                            CommitEvent.getId(commitSchema.host_name, commitSchema.project_name, commitSchema.id),
                            Seq.empty
                        )
                    )
            )
    }

    def getPipelines(): Dataset[PipelineEventResult] = {
        modelUtils.getPipelineSchemas()
            .map(pipelineSchema => EventResult.fromPipelineSchema(pipelineSchema))
    }

    def getPipelineToJobMap(): Map[(String, Int), Seq[(Int, String)]] = {
        // returns mapping from (host name, pipeline id) pair to a list of (pipeline job id, job name) pairs
        modelUtils.getPipelineJobSchemas()
            .map(
                pipelineJob => (
                    pipelineJob.host_name,
                    pipelineJob.pipeline.id,
                    pipelineJob.id,
                    pipelineJob.name
                )
            )
            .groupByKey({case (hostName, pipelineId, _, _) => (hostName, pipelineId)})
            .mapValues({case (_, _, jobId, jobName) => Seq((jobId, jobName))})
            .reduceGroups((first, second) => first ++ second)
            .collect()
            .toMap
    }

    def getTestSuiteNameMap(): Map[(String, Int), Seq[String]] = {
        // returns a mapping from (host name, pipeline job id) to a list of test suite names
        val pipelineToJobs: Map[(String, Int), Seq[(Int, String)]] = getPipelineToJobMap()

        modelUtils.loadMongoDataGitlab[PipelineReportSchema](MongoConstants.CollectionPipelineReports)
            .flatMap(row => PipelineReportSchema.fromRow(row))
            .map(
                report => report.test_suites.map(
                    suite => pipelineToJobs.get((report.host_name, report.pipeline_id))
                        .map(
                            jobElement => jobElement.map({
                                case (jobId, jobName) => (report.host_name, jobId, jobName, suite.name)
                            })
                        )
                )
            )
            .flatMap(sequence => sequence)
            .flatMap(optional => optional)
            .flatMap(sequence => sequence)
            .filter(element => element._3 == element._4)
            .map({case (hostName, jobId, _, suiteName) => (hostName, jobId, suiteName)})
            .distinct()
            .groupByKey({case (hostName, jobId, _) => (hostName, jobId)})
            .mapValues({case (_, _, suiteName) => Seq(suiteName)})
            .reduceGroups((first, second) => first ++ second)
            .collect()
            .toMap
    }

    def getPipelineJobs(): Dataset[PipelineJobEventResult] = {
        val projectNames: Map[(String, Int), String] = modelUtils.getPipelineProjectNameMap()
        val testSuiteNames: Map[(String, Int),Seq[String]] = getTestSuiteNameMap()

        modelUtils.getPipelineJobSchemas()
            // include only the jobs that have a known project name
            .filter(pipelineJob => projectNames.keySet.contains((pipelineJob.host_name, pipelineJob.pipeline.id)))
            .map(
                pipelineJobSchema =>
                    EventResult.fromPipelineJobSchema(
                        pipelineJobSchema,
                        projectNames.getOrElse(
                            (pipelineJobSchema.host_name, pipelineJobSchema.pipeline.id),
                            CommonConstants.EmptyString
                        ),
                        testSuiteNames.getOrElse((pipelineJobSchema.host_name, pipelineJobSchema.id), Seq.empty)
                    )
            )
    }

    def getSubmissions(): Dataset[SubmissionEventResult] = {
        val exerciseGitMap: Map[Int, String] = modelUtils.getExerciseGitMap()

        modelUtils.getSubmissionSchemas()
            .map(
                submission => (
                    submission,
                    submission.submission_data.map(submissionData => submissionData.git).flatten match {
                        case Some(gitData: SubmissionGitDataSchema) =>
                            exerciseGitMap.get(submission.exercise.id) match {
                                case Some(gitPath: String) => Seq(
                                    ItemLink(
                                        FileArtifact.getId(
                                            GitlabOrigin.getId(gitData.host_name, gitData.project_name),
                                            gitPath
                                        ),
                                        FileArtifact.FileArtifactType
                                    )
                                )
                                case None => Seq.empty
                            }
                        case None => Seq.empty
                }
                )
            )
            .map({case (submission, fileLinks) => EventResult.fromSubmissionSchema(submission, fileLinks, Seq.empty)})
    }
}
