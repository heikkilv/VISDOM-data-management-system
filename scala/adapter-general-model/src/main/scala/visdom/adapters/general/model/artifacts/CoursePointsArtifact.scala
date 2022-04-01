package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.CoursePointsData
import visdom.adapters.general.model.artifacts.states.PointsState
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.metadata.CourseMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.PointsSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


class CoursePointsArtifact(
    pointsSchema: PointsSchema,
    courseSchema: Option[CourseSchema]
)
extends Artifact {
    def getType: String = CoursePointsArtifact.CoursePointsArtifactType

    val origin: ItemLink =
        new AplusOrigin(
            pointsSchema.host_name,
            pointsSchema.course_id,
            None
        ).link

    val name: String = s"Course ${pointsSchema.course_id} points for ${pointsSchema.id}"
    val description: String = s"Course ${pointsSchema.course_id} points for student ${pointsSchema.full_name}"

    val state: String = courseSchema match {
        case Some(course: CourseSchema) => pointsSchema.metadata.last_modified match {
            case dateString: String if dateString < course.starting_time => PointsState.NotStarted
            case dateString: String if dateString > course.ending_time => PointsState.Finished
            case _ => PointsState.Active
        }
        case None => PointsState.Unknown
    }

    val data: CoursePointsData = CoursePointsData.fromPointsSchema(pointsSchema)

    val id: String = CoursePointsArtifact.getId(origin.id, data.course_id, data.user_id)

    // add the user and related course metadata and module points artifacts as related constructs
    addRelatedConstructs(
        Seq(
            ItemLink(
                AplusAuthor.getId(AplusOrigin.getId(pointsSchema.host_name), data.user_id),
                AplusAuthor.AplusAuthorType
            ),
            ItemLink(CourseMetadata.getId(origin.id, data.course_id), CourseMetadata.CourseMetadataType)
        ) ++
        pointsSchema.modules.map(
            modulePointsSchema =>
                ItemLink(
                    ModulePointsArtifact.getId(origin.id, modulePointsSchema.id, data.user_id),
                    ModulePointsArtifact.ModulePointsArtifactType
                )
        )
    )
}

object CoursePointsArtifact {
    final val CoursePointsArtifactType: String = "course_points"

    def getId(originId: String, courseId: Int, userId: Int): String = {
        GeneralUtils.getUuid(originId, CoursePointsArtifactType, courseId.toString(), userId.toString())
    }

    def fromPointsSchema(pointsSchema: PointsSchema, courseSchema: Option[CourseSchema]): CoursePointsArtifact = {
        new CoursePointsArtifact(pointsSchema, courseSchema)
    }
}
