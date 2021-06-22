package visdom.http.server.response


abstract class StatusResponse extends BaseResponse {
    def status: String
    def description: String
}
