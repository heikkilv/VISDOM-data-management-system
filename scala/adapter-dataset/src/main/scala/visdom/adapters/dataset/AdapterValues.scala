// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset

import visdom.adapters.QueryCache
import visdom.adapters.utils.GeneralQueryUtils
import visdom.utils.AdapterEnvironmentVariables.AdapterVariableMap
import visdom.utils.AdapterEnvironmentVariables.EnvironMentOnlyDataset
import visdom.utils.EnvironmentVariables.getEnvironmentVariable
import visdom.utils.QueryUtils


object AdapterValues
extends visdom.adapters.AdapterValues {
    override val AdapterType: String = "Dataset"
    override val Version: String = "0.1"

    val datasetName: String = "https://github.com/clowee/The-Technical-Debt-Dataset/releases/download/2.0.1/td_V2.db"
    val onlyDataset: Boolean = getEnvironmentVariable(EnvironMentOnlyDataset, AdapterVariableMap) == "true"

    override val supportedDatabases: Seq[String] = Seq(datasetDatabaseName) ++ (
        onlyDataset match {
            case true => Seq(aPlusDatabaseName, gitlabDatabaseName)
            case false => Seq.empty
        }
    )

    override val cache: QueryCache = new QueryCache(supportedDatabases)
    override val queryUtils: QueryUtils = new QueryUtils(cache)

    val generalQueryUtils: GeneralQueryUtils = new GeneralQueryUtils(cacheDatabaseName, supportedDatabases)
}
