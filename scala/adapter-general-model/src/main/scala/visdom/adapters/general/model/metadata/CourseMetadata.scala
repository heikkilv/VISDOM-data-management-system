package visdom.adapters.general.model.metadata

import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.Metadata
import visdom.adapters.general.model.metadata.data.CourseData
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.NameSchema
import visdom.utils.GeneralUtils


class CourseMetadata(
    courseSchema: CourseSchema
)
extends Metadata {
    def getType: String = CourseMetadata.CourseMetadataType

    val origin: ItemLink =
        new AplusOrigin(
            courseSchema.host_name,
            courseSchema.id,
            Some(courseSchema.code)
        ).link

    val name: String = courseSchema.name.en match {
        case Some(englishName: NameSchema) => englishName.name
        case None => courseSchema.name.fi match {
            case Some(finnishName: NameSchema) => finnishName.name
            case None => courseSchema.name.raw
        }
    }
    val description: String = courseSchema.name.raw

    val data: CourseData = CourseData.fromCourseSchema(courseSchema)

    val id: String = CourseMetadata.getId(origin.id, data.course_id)

    // add links to the related module metadata
    addRelatedConstructs(
        data.modules.map(
            moduleId => ItemLink(
                ModuleMetadata.getId(origin.id, moduleId),
                ModuleMetadata.ModuleMetadataType
            )
        )
    )
}

object CourseMetadata {
    final val CourseMetadataType: String = "course"

    def getId(originId: String, courseId: Int): String = {
        GeneralUtils.getUuid(originId, CourseMetadataType, courseId.toString())
    }

    def fromCourseSchema(courseSchema: CourseSchema): CourseMetadata = {
        new CourseMetadata(courseSchema)
    }
}
