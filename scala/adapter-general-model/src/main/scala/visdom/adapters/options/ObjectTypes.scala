package visdom.adapters.options

import visdom.adapters.general.model.artifacts.CoursePointsArtifact
import visdom.adapters.general.model.artifacts.ExercisePointsArtifact
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.ModuleAverageArtifact
import visdom.adapters.general.model.artifacts.ModulePointsArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.events.SubmissionEvent
import visdom.adapters.general.model.metadata.CourseMetadata
import visdom.adapters.general.model.metadata.ExerciseMetadata
import visdom.adapters.general.model.metadata.ModuleMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.utils.SnakeCaseConstants


object ObjectTypes extends ObjectTypesTrait {
    val OriginTypes: Set[String] = Set(
        GitlabOrigin.GitlabOriginType,
        AplusOrigin.AplusOriginType
    )
    val EventTypes: Set[String] = Set(
        CommitEvent.CommitEventType,
        PipelineEvent.PipelineEventType,
        PipelineJobEvent.PipelineJobEventType,
        SubmissionEvent.SubmissionEventType
    )
    val AuthorTypes: Set[String] = Set(
        CommitAuthor.CommitAuthorType,
        GitlabAuthor.GitlabAuthorType,
        AplusAuthor.AplusAuthorType
    )
    val ArtifactTypes: Set[String] = Set(
        FileArtifact.FileArtifactType,
        PipelineReportArtifact.PipelineReportArtifactType,
        CoursePointsArtifact.CoursePointsArtifactType,
        ModulePointsArtifact.ModulePointsArtifactType,
        ExercisePointsArtifact.ExercisePointsArtifactType,
        ModuleAverageArtifact.ModuleAverageArtifactType
    )
    val MetadataTypes: Set[String] = Set(
        CourseMetadata.CourseMetadataType,
        ModuleMetadata.ModuleMetadataType,
        ExerciseMetadata.ExerciseMetadataType
    )

    val objectTypes: Map[String, Set[String]] = Map(
        TargetTypeOrigin -> OriginTypes,
        TargetTypeEvent -> EventTypes,
        TargetTypeAuthor -> AuthorTypes,
        TargetTypeArtifact -> ArtifactTypes,
        TargetTypeMetadata -> MetadataTypes
    )

    // The default attribute type is String => only non-string attributes should be listed here
    val attributeTypes: Map[String, Map[String, String]] = Map(
        GitlabOrigin.GitlabOriginType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProjectId) -> IntType
        ),
        AplusOrigin.AplusOriginType -> Map.empty,
        CommitEvent.CommitEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Additions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Deletions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Total) -> IntType
        ),
        PipelineEvent.PipelineEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.PipelineId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProjectId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.QueuedDuration) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Tag) -> BooleanType
        ),
        PipelineJobEvent.PipelineJobEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.JobId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProjectId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Tag) -> BooleanType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.QueuedDuration) -> DoubleType
        ),
        SubmissionEvent.SubmissionEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SubmissionId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ExerciseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Grade) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.LatePenaltyApplied) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Grader) -> IntType
        ),
        FileArtifact.FileArtifactType -> Map.empty,
        PipelineReportArtifact.PipelineReportArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TotalTime) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TotalCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SuccessCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.FailedCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SkippedCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ErrorCount) -> IntType
        ),
        CoursePointsArtifact.CoursePointsArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CourseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Points) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SubmissionCount) -> IntType
        ),
        ModulePointsArtifact.ModulePointsArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ModuleId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Points) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.MaxPoints) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ExerciseCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SubmissionCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CommitCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Passed) -> BooleanType
        ),
        ExercisePointsArtifact.ExercisePointsArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ExerciseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Points) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.MaxPoints) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SubmissionCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CommitCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Passed) -> BooleanType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Official) -> BooleanType
        ),
        ModuleAverageArtifact.ModuleAverageArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ModuleNumber) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CourseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Grade) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Total) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.AvgPoints) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.AvgExercises) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.AvgSubmissions) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.AvgCommits) -> DoubleType
        ),
        CourseMetadata.CourseMetadataType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CourseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.LateSubmissionCoefficient) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.VisibleToStudents) -> BooleanType
        ),
        ModuleMetadata.ModuleMetadataType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ModuleId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ModuleNumber) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CourseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.MaxPoints) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.PointsToPass) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.IsOpen) -> BooleanType
        ),
        ExerciseMetadata.ExerciseMetadataType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ExerciseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ModuleId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CourseId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.MaxPoints) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.MaxSubmissions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.PointsToPass) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.IsSubmittable) -> BooleanType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.GitIsFolder) -> BooleanType
        ),
        CommitAuthor.CommitAuthorType -> Map.empty,
        GitlabAuthor.GitlabAuthorType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType
        ),
        AplusAuthor.AplusAuthorType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.IsExternal) -> BooleanType
        )
    )
}
