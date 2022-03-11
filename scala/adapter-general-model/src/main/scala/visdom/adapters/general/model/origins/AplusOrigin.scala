package visdom.adapters.general.model.origins

import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.origins.data.AplusOriginData
import visdom.utils.GeneralUtils
import visdom.utils.CommonConstants


class AplusOrigin(hostName: String, courseId: Int, code: Option[String])
extends Origin {
    def getType: String = AplusOrigin.AplusOriginType
    val source: String = hostName
    val context: String = courseId.toString()

    override val id: String = AplusOrigin.getId(hostName, courseId)

    val data: AplusOriginData = AplusOriginData(code)
}

object AplusOrigin {
    final val AplusOriginType: String = "aplus"

    def getAplusOriginFromHost(hostName: String): AplusOrigin = {
        new AplusOrigin(hostName, CommonConstants.MinusOne, None)
    }

    def getId(hostName: String): String = {
        getId(hostName, CommonConstants.MinusOne)
    }

    def getId(hostName: String, courseId: Int): String = {
        GeneralUtils.getUuid(AplusOriginType, hostName, courseId.toString())
    }
}
