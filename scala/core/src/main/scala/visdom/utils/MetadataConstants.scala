package visdom.utils


object MetadataConstants {
    val AttributeAdapterType: String = "adapter_type"
    val AttributeApiAddress: String = "api_address"
    val AttributeComponentName: String = "application_name"
    val AttributeComponentType: String = "component_type"
    val AttributeDatabase: String = "database"
    val AttributeFetcherType: String = "fetcher_type"
    val AttributeId: String = "id"
    val AttributeSourceServer: String = "source_server"
    val AttributeStartTime: String = "start_time"
    val AttributeSwaggerDefinition: String = "swagger_definition"
    val AttributeTimestamp: String = "timestamp"
    val AttributeVersion: String = "version"

    val MetadataInitialDelay: Long = 0
    val MetadataUpdateInterval: Long = 300000
    val ComponentActiveInterval: Long = MetadataUpdateInterval * 11 / 10
}
