// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.results.ProjectArtifactResult
import visdom.adapters.dataset.results.ProjectArtifactResult.UserAuthorResult
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.database.mongodb.MongoConstants


class DatasetModelAuthorUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelAuthorUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder
    import sparkSession.implicits.newStringEncoder

    private def getCommitUsers(): Dataset[(String, Seq[String], Seq[String])] = {
        modelUtils.getCommitSchemas()
            .flatMap(
                commit => Seq(
                    (commit.committer, commit.project_id, commit.commit_hash),
                    (commit.author, commit.project_id, commit.commit_hash)
                )
            )
            .groupByKey({case (username, projectId, commitId) => username})
            .mapValues({
                case (_, projectId, commitId) =>
                    Seq(ProjectCommitEvent.getId(AdapterValues.datasetName, projectId, commitId))
            })
            .reduceGroups((first, second) => first ++ second)
            .map({case (username, commitEventIds) => (username, commitEventIds.distinct, Seq.empty)})
    }

    private def getIssueUsers(): Dataset[(String, Seq[String], Seq[String])] =
        modelUtils.loadMongoDataDataset[JiraIssueSchema](MongoConstants.CollectionJiraIssues)
            .flatMap(row => JiraIssueSchema.fromRow(row))
            .flatMap(
                issue => (
                    issue.assignee match {
                        case Some(assignee: String) => Seq((assignee, issue.project_id, issue.key))
                        case None => Seq.empty
                    }
                ) ++ Seq(
                    (issue.creator_name, issue.project_id, issue.key),
                    (issue.reporter, issue.project_id, issue.key)
                )
            )
            .groupByKey({case (username, projectId, issueKey) => username})
            .mapValues({
                case (_, projectId, issueKey) =>
                    Seq(JiraIssueArtifact.getId(AdapterValues.datasetName, projectId, issueKey))
            })
            .reduceGroups((first, second) => first ++ second)
            .map({case (username, issueIds) => (username, Seq.empty, issueIds.distinct)})

    def getUserAuthors(): Dataset[UserAuthorResult] = {
        // collect the usernames and the commit event ids related to them (and empty issue id list)
        val commitUsers: Dataset[(String, Seq[String], Seq[String])] = getCommitUsers()

        // collect the usernames and (empty list of commit ids and) the Jira issue artifact ids related to them
        val issueUsers: Dataset[(String, Seq[String], Seq[String])] = getIssueUsers()

        commitUsers
            .union(issueUsers)
            .groupByKey({case (username, _, _) => username})
            .mapValues({case (_, commitEventIds, issueIds) => (commitEventIds, issueIds)})
            .reduceGroups((first, second) => (first._1 ++ second._1, first._2 ++ second._2))
            .map({
                case (username, (commitEventIds, issueIds)) =>
                    ProjectArtifactResult.fromUsername(
                        username = username,
                        datasetName = AdapterValues.datasetName,
                        relatedIssueIds = issueIds,
                        relatedCommitEventIds = commitEventIds
                    )
            })
    }
}
