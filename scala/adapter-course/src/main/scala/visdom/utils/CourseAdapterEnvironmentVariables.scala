// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils


object CourseAdapterEnvironmentVariables {
    val EnvironmentAPlusDatabase: String = "APLUS_DATABASE"
    val EnvironmentGitlabDatabase: String = "GITLAB_DATABASE"

    // the default values for the MongoDB database name related environmental variables
    val DefaultAPlusDatabase: String = "aplus"
    val DefaultGitlabDatabase: String = "gitlab"

    val CourseAdapterVariableMap: Map[String, String] =
        EnvironmentVariables.VariableMap ++
        Map(
            EnvironmentAPlusDatabase -> DefaultAPlusDatabase,
            EnvironmentGitlabDatabase -> DefaultGitlabDatabase
        )
}
