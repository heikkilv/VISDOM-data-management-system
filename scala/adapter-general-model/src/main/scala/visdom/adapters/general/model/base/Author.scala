package visdom.adapters.general.model.base

import visdom.utils.CommonConstants


abstract class Author
extends Artifact {
    def getType: String = Author.AuthorType
}

object Author {
    final val AuthorType: String = "author"
    final val DefaultDescription: String = CommonConstants.EmptyString
}
