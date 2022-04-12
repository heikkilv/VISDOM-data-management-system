package visdom.adapters.general

import visdom.adapters.utils.GeneralQueryUtils


object AdapterValues
extends visdom.adapters.AdapterValues {
    override val AdapterType: String = "GeneralModel"
    override val Version: String = "0.1"

    val generalQueryUtils: GeneralQueryUtils = new GeneralQueryUtils(cacheDatabaseName, supportedDatabases)
}
