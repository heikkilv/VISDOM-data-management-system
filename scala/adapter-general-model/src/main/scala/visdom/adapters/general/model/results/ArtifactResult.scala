package visdom.adapters.general.model.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.artifacts.CoursePointsArtifact
import visdom.adapters.general.model.artifacts.ExercisePointsArtifact
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.ModuleAverageArtifact
import visdom.adapters.general.model.artifacts.ModulePointsArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.artifacts.data.CoursePointsData
import visdom.adapters.general.model.artifacts.data.ExercisePointsData
import visdom.adapters.general.model.artifacts.data.FileData
import visdom.adapters.general.model.artifacts.data.ModuleAverageData
import visdom.adapters.general.model.artifacts.data.ModulePointsData
import visdom.adapters.general.model.artifacts.data.PipelineReportData
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.authors.data.AplusAuthorData
import visdom.adapters.general.model.authors.data.CommitAuthorData
import visdom.adapters.general.model.authors.data.GitlabAuthorData
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.LinkTrait
import visdom.adapters.general.schemas.AplusUserSchema
import visdom.adapters.general.schemas.CommitAuthorProcessedSchema
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.GitlabAuthorSchema
import visdom.adapters.general.schemas.ModuleAverageSchema
import visdom.adapters.general.schemas.ModuleNumbersSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.adapters.general.schemas.PipelineUserSchema
import visdom.adapters.general.schemas.PointsExerciseSchema
import visdom.adapters.general.schemas.PointsModuleSchema
import visdom.adapters.general.schemas.PointsSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.IdValue
import visdom.json.JsonUtils
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ArtifactResult[ArtifactData <: Data](
    _id: String,
    id: String,
    `type`: String,
    name: String,
    description: String,
    state: String,
    origin: ItemLink,
    data: ArtifactData,
    related_constructs: Seq[ItemLink],
    related_events: Seq[ItemLink]
)
extends BaseResultValue
with IdValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(`type`),
                SnakeCaseConstants.Name -> JsonUtils.toBsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toBsonValue(description),
                SnakeCaseConstants.State -> JsonUtils.toBsonValue(state),
                SnakeCaseConstants.Origin -> origin.toBsonValue(),
                SnakeCaseConstants.Data -> data.toBsonValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toBsonValue(related_constructs.map(link => link.toBsonValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toBsonValue(related_events.map(link => link.toBsonValue()))
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(`type`),
                SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toJsonValue(description),
                SnakeCaseConstants.State -> JsonUtils.toJsonValue(state),
                SnakeCaseConstants.Origin -> origin.toJsValue(),
                SnakeCaseConstants.Data -> data.toJsValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toJsonValue(related_constructs.map(link => link.toJsValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toJsonValue(related_events.map(link => link.toJsValue()))
            )
        )
    }
}

object ArtifactResult {
    type FileArtifactResult = ArtifactResult[FileData]
    type PipelineReportArtifactResult = ArtifactResult[PipelineReportData]

    type CoursePointsArtifactResult = ArtifactResult[CoursePointsData]
    type ModulePointsArtifactResult = ArtifactResult[ModulePointsData]
    type ExercisePointsArtifactResult = ArtifactResult[ExercisePointsData]
    type ModuleAverageArtifactResult = ArtifactResult[ModuleAverageData]

    type CommitAuthorResult = ArtifactResult[CommitAuthorData]
    type GitlabAuthorResult = ArtifactResult[GitlabAuthorData]
    type AplusAuthorResult = ArtifactResult[AplusAuthorData]

    def fromArtifact[ArtifactData <: Data](
        artifact: Artifact,
        artifactData: ArtifactData
    ): ArtifactResult[ArtifactData] = {
        ArtifactResult(
            _id = artifact.id,
            id = artifact.id,
            `type` = artifact.getType,
            name = artifact.name,
            description = artifact.description,
            state = artifact.state,
            origin = artifact.origin,
            data = artifactData,
            related_constructs = artifact.relatedConstructs.map(link => ItemLink.fromLinkTrait(link)),
            related_events = artifact.relatedEvents.map(link => ItemLink.fromLinkTrait(link))
        )
    }

    def fromCommitAuthorProcessedSchema(commitAuthorSchema: CommitAuthorProcessedSchema): CommitAuthorResult = {
        val commitAuthor: CommitAuthor = new CommitAuthor(
            authorName = commitAuthorSchema.committerName,
            authorEmail = commitAuthorSchema.committerEmail,
            hostName = commitAuthorSchema.hostName,
            relatedCommitEventIds = commitAuthorSchema.commitEventIds,
            relatedAuthorIds = commitAuthorSchema.gitlabAuthorIds
        )
        fromArtifact(commitAuthor, commitAuthor.data)
    }

    def fromUserData(
        pipelineUserSchema: PipelineUserSchema,
        hostName: String,
        committerIds: Seq[String],
        commitEventIds: Seq[String],
        pipelineEventIds: Seq[String],
        pipelineJobEventIds: Seq[String]
    ): GitlabAuthorResult = {
        val gitlabAuthor: GitlabAuthor = new GitlabAuthor(
            userId = pipelineUserSchema.id,
            username = pipelineUserSchema.username,
            authorName = pipelineUserSchema.name,
            authorState = pipelineUserSchema.state,
            hostName = hostName,
            relatedCommitterIds = committerIds,
            relatedCommitEventIds = commitEventIds,
            relatedPipelineEventIds = pipelineEventIds,
            relatedPipelineJobEventIds = pipelineJobEventIds
        )
        fromArtifact(gitlabAuthor, gitlabAuthor.data)
    }

    def fromUserData(
        aplusUserSchema: AplusUserSchema,
        relatedConstructs: Seq[LinkTrait],
        relatedEvents: Seq[LinkTrait]
    ): AplusAuthorResult = {
        val aplusAuthor: AplusAuthor = new AplusAuthor(aplusUserSchema, relatedConstructs, relatedEvents)
        fromArtifact(aplusAuthor, aplusAuthor.data)
    }

    def fromFileSchema(fileSchema: FileSchema, relatedFilePaths: Seq[String]): FileArtifactResult = {
        val fileArtifact: FileArtifact = new FileArtifact(fileSchema, relatedFilePaths)
        fromArtifact(fileArtifact, fileArtifact.data)
    }

    def fromPipelineReportSchema(
        pipelineReportSchema: PipelineReportSchema,
        projectName: String
    ): PipelineReportArtifactResult = {
        val reportArtifact: PipelineReportArtifact = new PipelineReportArtifact(pipelineReportSchema, projectName)
        fromArtifact(reportArtifact, reportArtifact.data)
    }

    def fromCoursePointsSchema(
        coursePointsSchema: PointsSchema,
        courseSchema: Option[CourseSchema]
    ): CoursePointsArtifactResult = {
        val coursePointsArtifact: CoursePointsArtifact = new CoursePointsArtifact(coursePointsSchema, courseSchema)
        fromArtifact(coursePointsArtifact, coursePointsArtifact.data)
    }

    def fromModulePointsSchema(
        modulePointsSchema: PointsModuleSchema,
        moduleSchema: ModuleSchema,
        userId: Int,
        exerciseCount: Int,
        commitCount: Int,
        cumulativeValues: ModuleNumbersSchema,
        updateTime: String
    ): ModulePointsArtifactResult = {
        val modulePointsArtifact: ModulePointsArtifact =
            new ModulePointsArtifact(
                modulePointsSchema,
                moduleSchema,
                userId,
                exerciseCount,
                commitCount,
                cumulativeValues,
                updateTime
            )
        fromArtifact(modulePointsArtifact, modulePointsArtifact.data)
    }

    def fromExercisePointsSchema(
        exercisePointsSchema: PointsExerciseSchema,
        exerciseSchema: ExerciseSchema,
        additionalSchema: ExerciseAdditionalSchema,
        moduleId: Int,
        userId: Int,
        relatedCommitEventLinks: Seq[LinkTrait],
        updateTime: String
    ): ExercisePointsArtifactResult = {
        val modulePointsArtifact: ExercisePointsArtifact = new ExercisePointsArtifact(
            exercisePointsSchema,
            exerciseSchema,
            additionalSchema,
            moduleId,
            userId,
            relatedCommitEventLinks,
            updateTime
        )
        fromArtifact(modulePointsArtifact, modulePointsArtifact.data)
    }

    def fromModuleAverageSchema(
        moduleAverageSchema: ModuleAverageSchema,
        courseSchema: CourseSchema,
        moduleIds: Seq[Int],
        updateTime: String
    ): ModuleAverageArtifactResult = {
        val moduleAverageArtifact: ModuleAverageArtifact = new ModuleAverageArtifact(
            moduleAverageSchema,
            courseSchema,
            moduleIds,
            updateTime
        )
        fromArtifact(moduleAverageArtifact, moduleAverageArtifact.data)
    }
}
