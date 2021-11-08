package visdom.adapters.general

import visdom.adapters.AdapterApp


object Adapter extends AdapterApp {
    val adapterMetadata: visdom.adapters.Metadata = Metadata
    val adapterRoutes: visdom.adapters.AdapterRoutes = AdapterRoutes
    val adapterValues: visdom.adapters.AdapterValues = AdapterValues
}
