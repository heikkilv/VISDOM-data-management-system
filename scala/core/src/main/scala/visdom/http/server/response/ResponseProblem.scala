package visdom.http.server.response


final case class ResponseProblem(
    status: String,
    description: String
) extends StatusResponse
