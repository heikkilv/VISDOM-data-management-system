package visdom.utils

import org.apache.commons.lang.StringUtils.EMPTY


object EnvironmentVariables {
    // the environmental variable names
    val EnvironmentApplicationName: String = "APPLICATION_NAME"
    val EnvironmentDataBrokerAddress: String = "DATA_BROKER_ADDRESS"
    val EnvironmentHostName: String = "HOST_NAME"
    val EnvironmentHostPort: String = "HOST_PORT"
    val EnvironmentMetadataDatabase: String = "MONGODB_METADATA_DATABASE"

    // the default values for the environment variables
    val DefaultApplicationName: String = "component-name"
    val DefaultDataBrokerAddress: String = "http://visdom-broker:8080"
    val DefaultHostName: String = "localhost"
    val DefaultHostPort: String = "8765"
    val DefaultMetadataDatabase: String = "metadata"

    val DefaultEnvironmentValue: String = EMPTY

    val VariableMap: Map[String, String] = Map(
        EnvironmentApplicationName -> DefaultApplicationName,
        EnvironmentDataBrokerAddress -> DefaultDataBrokerAddress,
        EnvironmentHostName -> DefaultHostName,
        EnvironmentHostPort -> DefaultHostPort,
        EnvironmentMetadataDatabase -> DefaultMetadataDatabase
    )

    def getEnvironmentVariable(variableName: String): String = {
        sys.env.getOrElse(
            variableName,
            VariableMap.getOrElse(variableName, DefaultEnvironmentValue)
        )
    }

    def getEnvironmentVariable(variableName: String, customVariableMap: Map[String, String]): String = {
        sys.env.getOrElse(
            variableName,
            customVariableMap.getOrElse(variableName, DefaultEnvironmentValue)
        )
    }
}
