// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.results.ProjectArtifactResult
import visdom.adapters.dataset.results.ProjectArtifactResult.JiraIssueArtifactResult
import visdom.adapters.dataset.results.ProjectArtifactResult.SonarMeasuresArtifactResult
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.adapters.dataset.schemas.SonarMeasuresSchema
import visdom.database.mongodb.MongoConstants


class DatasetModelArtifactUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelArtifactUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder

    def getJiraIssues(): Dataset[JiraIssueArtifactResult] = {
        modelUtils.loadMongoDataDataset[JiraIssueSchema](MongoConstants.CollectionJiraIssues)
            .flatMap(row => JiraIssueSchema.fromRow(row))
            .map(issue => ProjectArtifactResult.fromJiraIssueSchema(issue, AdapterValues.datasetName))
    }

    def getCommitDateIdMap(): Map[(String, String), Seq[String]] = {
        modelUtils.getCommitSchemas()
            .map(commit => (commit.project_id, commit.committer_date, commit.commit_hash))
            .groupByKey({case (projectId, date, _) => (projectId, date)})
            .mapValues({
                case (projectId, _, hash) => Seq(ProjectCommitEvent.getId(AdapterValues.datasetName, projectId, hash))
            })
            .reduceGroups((first, second) => first ++ second)
            .collect()
            .toMap
    }

    def getSonarMeasures(): Dataset[SonarMeasuresArtifactResult] = {
        val commitDateIdMap: Map[(String, String), Seq[String]] = getCommitDateIdMap()

        modelUtils.loadMongoDataDataset[SonarMeasuresSchema](MongoConstants.CollectionSonarMeasures)
            .flatMap(row => SonarMeasuresSchema.fromRow(row))
            .map(
                measures => ProjectArtifactResult.fromSonarMeasuresSchema(
                    measuresSchema = measures,
                    datasetName = AdapterValues.datasetName,
                    relatedCommitEventIds = measures.last_commit_date match {
                        case Some(commitDate: String) => commitDateIdMap.get((measures.project_id, commitDate)) match {
                            case Some(commitIds: Seq[String]) => commitIds
                            case None => Seq.empty
                        }
                        case None => Seq.empty
                    }
                )
            )
    }
}
