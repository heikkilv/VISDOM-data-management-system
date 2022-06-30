// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils


object AdapterEnvironmentVariables {
    val EnvironmentAPlusDatabase: String = "APLUS_DATABASE"
    val EnvironmentCacheDatabase: String = "CACHE_DATABASE"
    val EnvironmentGitlabDatabase: String = "GITLAB_DATABASE"
    val EnvironmentDatasetDatabase: String = "DATASET_DATABASE"
    val EnvironMentOnlyDataset: String = "ONLY_DATASET"

    // the default values for the MongoDB database name related environmental variables
    val DefaultAPlusDatabase: String = "aplus"
    val DefaultCacheDatabase: String = "cache"
    val DefaultGitlabDatabase: String = "gitlab"
    val DefaultDatasetDatabase: String = "dataset"
    val DefaultOnlyDataset: String = "true"

    val AdapterVariableMap: Map[String, String] =
        EnvironmentVariables.VariableMap ++
        Map(
            EnvironmentAPlusDatabase -> DefaultAPlusDatabase,
            EnvironmentCacheDatabase -> DefaultCacheDatabase,
            EnvironmentGitlabDatabase -> DefaultGitlabDatabase,
            EnvironmentDatasetDatabase -> DefaultDatasetDatabase,
            EnvironMentOnlyDataset -> DefaultOnlyDataset
        )
}
