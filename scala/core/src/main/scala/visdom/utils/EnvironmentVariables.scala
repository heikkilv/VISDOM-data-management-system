// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils


object EnvironmentVariables {
    // the environmental variable names
    val EnvironmentApplicationName: String = "APPLICATION_NAME"
    val EnvironmentDataBrokerAddress: String = "DATA_BROKER_ADDRESS"
    val EnvironmentHostName: String = "HOST_NAME"
    val EnvironmentHostPort: String = "HOST_PORT"
    val EnvironmentMetadataDatabase: String = "MONGODB_METADATA_DATABASE"
    val EnvironmentSecretWord: String = "SECRET_WORD"

    // the default values for the environment variables
    val DefaultApplicationName: String = "component-name"
    val DefaultDataBrokerAddress: String = "http://visdom-broker:8080"
    val DefaultHostName: String = "localhost"
    val DefaultHostPort: String = "8765"
    val DefaultMetadataDatabase: String = "metadata"
    val DefaultSecretWord: String = CommonConstants.EmptyString

    val DefaultEnvironmentValue: String = CommonConstants.EmptyString

    val VariableMap: Map[String, String] = Map(
        EnvironmentApplicationName -> DefaultApplicationName,
        EnvironmentDataBrokerAddress -> DefaultDataBrokerAddress,
        EnvironmentHostName -> DefaultHostName,
        EnvironmentHostPort -> DefaultHostPort,
        EnvironmentMetadataDatabase -> DefaultMetadataDatabase,
        EnvironmentSecretWord -> DefaultSecretWord
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
