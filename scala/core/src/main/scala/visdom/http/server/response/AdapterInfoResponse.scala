package visdom.http.server.response


abstract class AdapterInfoResponse extends ComponentInfoResponse {
    def adapterType: String
}
