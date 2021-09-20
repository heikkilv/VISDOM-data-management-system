package visdom.http.server.response


final case class CourseAdapterInfoResponse(
    componentType: String,
    componentName: String,
    adapterType: String,
    version: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends ComponentInfoResponse
