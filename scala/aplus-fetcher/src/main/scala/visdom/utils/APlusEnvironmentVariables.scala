package visdom.utils


object APlusEnvironmentVariables {
    val EnvironmentAPlusHost: String = "APLUS_HOST"
    val EnvironmentAPlusToken: String = "APLUS_TOKEN"
    val EnvironmentAPlusInsecureConnection: String = "APLUS_INSECURE_CONNECTION"
    val EnvironmentDataDatabase: String = "MONGODB_DATA_DATABASE"

    // the default values for the A+ environment variables
    val DefaultAPlusHost: String = ""
    val DefaultAPlusToken: String = ""
    val DefaultAPlusInsecureConnection: String = "false"
    val DefaultDataDatabase: String = "aplus"

    val APlusVariableMap: Map[String, String] =
        EnvironmentVariables.VariableMap ++
        Map(
            EnvironmentAPlusHost -> DefaultAPlusHost,
            EnvironmentAPlusToken -> DefaultAPlusToken,
            EnvironmentAPlusInsecureConnection -> DefaultAPlusInsecureConnection,
            EnvironmentDataDatabase -> DefaultDataDatabase
        )
}
