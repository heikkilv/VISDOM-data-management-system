package visdom.adapters.general.model.authors

import visdom.adapters.general.model.authors.data.AplusAuthorData
import visdom.adapters.general.model.authors.states.AuthorState
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.LinkTrait
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.AplusUserSchema
import visdom.utils.GeneralUtils


class AplusAuthor(
    aplusUserSchema: AplusUserSchema,
    relatedConstructs: Seq[LinkTrait],
    relatedEvents: Seq[LinkTrait]
)
extends Author {
    override def getType: String = AplusAuthor.AplusAuthorType

    val name: String = aplusUserSchema.full_name
    val description: String = Author.DefaultDescription
    // use the active author state for all APlusAuthor objects
    val state: String = AuthorState.ActiveAuthorStateString
    val origin: ItemLink = AplusOrigin.getAplusOriginFromHost(aplusUserSchema.host_name).link
    val data: AplusAuthorData = AplusAuthorData.fromAplusUserSchema(aplusUserSchema)

    val id: String = AplusAuthor.getId(origin.id, data.user_id)

    addRelatedConstructs(relatedConstructs)
    addRelatedEvents(relatedEvents)
}

object AplusAuthor {
    final val AplusAuthorType: String = "aplus_user"

    def getId(originId: String, userId: Int): String = {
        GeneralUtils.getUuid(originId, AplusAuthorType, userId.toString())
    }
}
