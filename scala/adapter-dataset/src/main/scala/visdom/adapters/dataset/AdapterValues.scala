package visdom.adapters.dataset

import visdom.utils.AdapterEnvironmentVariables.AdapterVariableMap
import visdom.utils.AdapterEnvironmentVariables.EnvironMentOnlyDataset
import visdom.utils.EnvironmentVariables.getEnvironmentVariable


object AdapterValues
extends visdom.adapters.AdapterValues {
    override val AdapterType: String = "Dataset"
    override val Version: String = "0.1"

    val datasetName: String = "https://github.com/clowee/The-Technical-Debt-Dataset/releases/download/2.0.1/td_V2.db"
    val onlyDataset: Boolean = getEnvironmentVariable(EnvironMentOnlyDataset, AdapterVariableMap) == "true"
}
