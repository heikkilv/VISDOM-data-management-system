package visdom.utils


object APlusEnvironmentVariables {
    val EnvironmentAPlusHost: String = "APLUS_HOST"
    val EnvironmentDataDatabase: String = "MONGODB_DATA_DATABASE"

    // the default values for the A+ environment variables
    val DefaultAPlusHost: String = ""
    val DefaultDataDatabase: String = "aplus"

    val APlusVariableMap: Map[String, String] =
        EnvironmentVariables.VariableMap ++
        Map(
            EnvironmentAPlusHost -> DefaultAPlusHost,
            EnvironmentDataDatabase -> DefaultDataDatabase
        )
}
