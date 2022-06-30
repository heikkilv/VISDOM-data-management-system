// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.artifacts.CoursePointsArtifact
import visdom.adapters.general.model.artifacts.ExercisePointsArtifact
import visdom.adapters.general.model.artifacts.ModulePointsArtifact
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.SubmissionEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.AplusAuthorResult
import visdom.adapters.general.model.results.ArtifactResult.CommitAuthorResult
import visdom.adapters.general.model.results.ArtifactResult.GitlabAuthorResult
import visdom.adapters.general.schemas.AplusUserSchema
import visdom.adapters.general.schemas.CommitAuthorProcessedSchema
import visdom.adapters.general.schemas.CommitAuthorSchema
import visdom.adapters.general.schemas.GitlabUserEventSchema
import visdom.adapters.general.schemas.SubmissionUserSchema
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants


class ModelAuthorUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder

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

    // scalastyle:off method.length
    private def getAuthorsFromPoints(): Dataset[(AplusUserSchema, Seq[ItemLink], Seq[ItemLink])] = {
        val courseModuleExerciseMap: Map[Int,Map[Int,Seq[Int]]] = modelUtils.getCourseIdMap()

        modelUtils.getPointsSchemas()
            .map(
                points => (
                    AplusUserSchema(
                        id = points.id,
                        username = points.username,
                        student_id = points.student_id,
                        email = points.email,
                        full_name = points.full_name,
                        is_external = points.is_external,
                        host_name = points.host_name
                    ),
                    AplusOrigin.getId(points.host_name, points.course_id),
                    points.course_id,
                    courseModuleExerciseMap.getOrElse(points.course_id, Map.empty).keySet.toSeq,
                    courseModuleExerciseMap.getOrElse(points.course_id, Map.empty)
                        .map({case (_, exerciseIds) => exerciseIds})
                        .flatten
                        .toSeq
                )
            )
            .map({
                case (user, originId, courseId, moduleIds, exerciseIds) => (
                    user,
                    (
                        CoursePointsArtifact.getId(originId, courseId, user.id),
                        CoursePointsArtifact.CoursePointsArtifactType
                    ),
                    moduleIds.map(
                        moduleId => (
                            ModulePointsArtifact.getId(originId, moduleId, user.id),
                            ModulePointsArtifact.ModulePointsArtifactType
                        )
                    ),
                    exerciseIds.map(
                        exerciseId => (
                            ExercisePointsArtifact.getId(originId, exerciseId, user.id),
                            ExercisePointsArtifact.ExercisePointsArtifactType
                        )
                    )
                )
            })
            .map({
                case (user, courseLink, moduleLinks, exerciseLinks) => (
                    user,
                    Seq(courseLink) ++ moduleLinks ++ exerciseLinks
                )
            })
            .map({
                case (user, pointLinks) => (
                    user,
                    pointLinks.map({case (objectId, objectType) => ItemLink(objectId, objectType)}),
                    Seq.empty
                )
            })
    }
    // scalastyle:on method.length

    private def getAuthorsFromSubmissions(): Dataset[(AplusUserSchema, Seq[ItemLink], Seq[ItemLink])] = {
        modelUtils.getSubmissionSchemas()
            .map(submission => (
                submission.id,
                submission._links.map(links => links.courses.getOrElse(0)).getOrElse(0),
                submission.host_name,
                submission.submitters,
                submission.grader
            ))
            .map({
                case (submissionId, courseId, hostName, submitters, graderOption) => (
                    ItemLink(
                        SubmissionEvent.getId(hostName, courseId, submissionId),
                        SubmissionEvent.SubmissionEventType
                    ),
                    hostName,
                    graderOption match {
                        case Some(grader: SubmissionUserSchema) => submitters :+ grader
                        case None => submitters
                    }
                )
            })
            .flatMap({
                case (submissionEvent, hostName, users) => users.map(
                    user => (
                        AplusUserSchema(
                            id = user.id,
                            username = user.username,
                            student_id = user.student_id,
                            email = user.email,
                            full_name = user.full_name,
                            is_external = user.is_external,
                            host_name = hostName
                        ),
                        Seq.empty,
                        Seq(submissionEvent)
                    )
                )
            })
    }

    def getAplusAuthors(): Dataset[AplusAuthorResult] = {
        getAuthorsFromPoints()
            .union(getAuthorsFromSubmissions())
            .groupByKey({case (user, _, _) => user})
            .reduceGroups(
                (first, second) => (
                    first._1,
                    (first._2 ++ second._2).distinct,
                    (first._3 ++ second._3).distinct
                )
            )
            .map({case (_, row) => row})
            .map({
                case (user, relatedConstructs, relatedEvents) =>
                    ArtifactResult.fromUserData(user, relatedConstructs, relatedEvents)
            })
    }
}
