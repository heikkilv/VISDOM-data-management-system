package visdom.adapters.dataset

import visdom.adapters.AdapterApp


object Adapter extends AdapterApp {
    val adapterValues: visdom.adapters.AdapterValues = AdapterValues
    val adapterRoutes: visdom.adapters.AdapterRoutes = AdapterRoutes
    val adapterMetadata: visdom.adapters.Metadata = Metadata

    start()
}
