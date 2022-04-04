package visdom.adapters.dataset


object AdapterValues
extends visdom.adapters.AdapterValues {
    override val AdapterType: String = "Dataset"
    override val Version: String = "0.1"

    val datasetName: String = "https://github.com/clowee/The-Technical-Debt-Dataset/releases/download/2.0.1/td_V2.db"
}
