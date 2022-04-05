package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.results.ProjectEventResult
import visdom.adapters.dataset.results.ProjectEventResult.ProjectCommitEventResult
import visdom.adapters.dataset.schemas.CommitChangeSchema
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.adapters.dataset.schemas.ProjectSchema
import visdom.adapters.dataset.schemas.SonarMeasuresSchema
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


class DatasetModelEventUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelEventUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder
    import sparkSession.implicits.newStringEncoder

    def getCommitChangeMap(projectId: String): Map[String, (Int, Int, Int)] = {
        modelUtils.loadMongoDataDataset[CommitChangeSchema](MongoConstants.CollectionGitCommitsChanges)
            .filter(functions.column(SnakeCaseConstants.ProjectId) === projectId)
            .flatMap(row => CommitChangeSchema.fromRow(row))
            .map(
                commitChange => (
                    commitChange.commit_hash,
                    1,
                    commitChange.lines_added,
                    commitChange.lines_removed
                )
            )
            .groupByKey({case (hash, _, _, _) => hash})
            .mapValues({case (_, count, additions, deletions) => (count, additions, deletions)})
            .reduceGroups((first, second) => (first._1 + second._1, first._2 + second._2, first._3 + second._3))
            .collect()
            .toMap
    }

    def getCommitIssueMap(projectId: String): Map[String, Seq[String]] = {
        modelUtils.loadMongoDataDataset[JiraIssueSchema](MongoConstants.CollectionJiraIssues)
            .filter(functions.column(SnakeCaseConstants.ProjectId) === projectId)
            .flatMap(row => JiraIssueSchema.fromRow(row))
            .filter(issue => issue.hash.isDefined)
            .groupByKey(issue => issue.hash.getOrElse(CommonConstants.EmptyString))
            .mapValues(issue => Seq(JiraIssueArtifact.getId(AdapterValues.datasetName, issue.project_id, issue.key)))
            .reduceGroups((first, second) => first ++ second)
            .collect()
            .toMap
    }

    def getCommitDateMeasuresMap(projectId: String): Map[String, Seq[String]] = {
        modelUtils.loadMongoDataDataset[SonarMeasuresSchema](MongoConstants.CollectionSonarMeasures)
            .filter(functions.column(SnakeCaseConstants.ProjectId) === projectId)
            .flatMap(row => SonarMeasuresSchema.fromRow(row))
            .filter(measures => measures.last_commit_date.isDefined)
            .groupByKey(
                measures => (measures.last_commit_date.getOrElse(CommonConstants.EmptyString))
            )
            .mapValues(
                measures => Seq(
                    SonarMeasuresArtifact.getId(AdapterValues.datasetName, measures.project_id, measures.analysis_key)
                )
            )
            .reduceGroups((first, second) => first ++ second)
            .collect()
            .toMap
    }

    def getSingleProjectCommits(projectId: String): Dataset[ProjectCommitEventResult] = {
        val commitChangeMap: Map[String, (Int, Int, Int)] = getCommitChangeMap(projectId)
        val commitIssueMap: Map[String, Seq[String]] = getCommitIssueMap(projectId)
        val commitDateMeasuresMap: Map[String, Seq[String]] = getCommitDateMeasuresMap(projectId)

        modelUtils.getCommitSchemas()
            .filter(commit => commit.project_id == projectId)
            .map(
                commit => (
                    commit,
                    commitChangeMap.getOrElse(commit.commit_hash, (0, 0, 0))
                )
            )
            .map({
                case (commit, (files, additions, deletions)) => ProjectEventResult.fromCommitSchema(
                    commitSchema = commit,
                    numberOfFiles = files,
                    additions = additions,
                    deletions = deletions,
                    datasetName = AdapterValues.datasetName,
                    relatedConstructs = (
                        commitIssueMap
                            .getOrElse(commit.commit_hash, Seq.empty)
                            .map(issueId => ItemLink(issueId, JiraIssueArtifact.JiraIssueArtifactType))
                    ) ++ (
                        commitDateMeasuresMap
                            .getOrElse(commit.committer_date, Seq.empty)
                            .map(measuresId => ItemLink(measuresId, SonarMeasuresArtifact.SonarMeasuresArtifactType))
                    )
                )
            })
    }

    def storeProjectCommits(): Unit = {
        modelUtils.loadMongoDataDataset[ProjectSchema](MongoConstants.CollectionProjects)
            .flatMap(row => ProjectSchema.fromRow(row))
            .map(project => project.project_id)
            .collect()
            .foreach(
                projectId => modelUtils.storeObjects(
                    getSingleProjectCommits(projectId),
                    ProjectCommitEvent.ProjectCommitEventType
                )
            )
    }
}
