package visdom.utils


object AdapterEnvironmentVariables {
    val EnvironmentAPlusDatabase: String = "APLUS_DATABASE"
    val EnvironmentGitlabDatabase: String = "GITLAB_DATABASE"

    // the default values for the MongoDB database name related environmental variables
    val DefaultAPlusDatabase: String = "aplus"
    val DefaultGitlabDatabase: String = "gitlab"

    val AdapterVariableMap: Map[String, String] =
        EnvironmentVariables.VariableMap ++
        Map(
            EnvironmentAPlusDatabase -> DefaultAPlusDatabase,
            EnvironmentGitlabDatabase -> DefaultGitlabDatabase
        )
}
