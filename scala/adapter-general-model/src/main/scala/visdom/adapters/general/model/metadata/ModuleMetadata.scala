package visdom.adapters.general.model.metadata

import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.Metadata
import visdom.adapters.general.model.metadata.data.ModuleData
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.ModuleSchema
import visdom.utils.GeneralUtils


class ModuleMetadata(
    moduleSchema: ModuleSchema
)
extends Metadata {
    def getType: String = CourseMetadata.CourseMetadataType

    val origin: ItemLink =
        new AplusOrigin(
            moduleSchema.host_name,
            moduleSchema.course_id,
            None
        ).link

    val name: String = moduleSchema.display_name.en match {
        case Some(englishName: String) => englishName
        case None => moduleSchema.display_name.fi match {
            case Some(finnishName: String) => finnishName
            case None => moduleSchema.display_name.raw
        }
    }
    val description: String = moduleSchema.display_name.raw

    val data: ModuleData = ModuleData.fromModuleSchema(moduleSchema)

    val id: String = ModuleMetadata.getId(origin.id, data.module_id)

    // add links to the related exercise and course metadata
    addRelatedConstructs(
        data.exercises.map(
            exerciseId => ItemLink(
                ExerciseMetadata.getId(origin.id, exerciseId),
                ExerciseMetadata.ExerciseMetadataType
            )
        ) :+
        ItemLink(
            CourseMetadata.getId(origin.id, data.course_id),
            CourseMetadata.CourseMetadataType
        )
    )

    // TODO: add links to aplus user, module points artifact
}

object ModuleMetadata {
    final val ModuleMetadataType: String = "module"

    def getId(originId: String, moduleId: Int): String = {
        GeneralUtils.getUuid(originId, ModuleMetadataType, moduleId.toString())
    }

    def fromModuleSchema(moduleSchema: ModuleSchema): ModuleMetadata = {
        new ModuleMetadata(moduleSchema)
    }
}
