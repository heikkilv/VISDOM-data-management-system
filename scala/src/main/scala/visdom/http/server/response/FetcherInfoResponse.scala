package visdom.http.server.response


abstract class FetcherInfoResponse extends ComponentInfoResponse {
    def fetcherType: String
    def mongoDatabase: String
}
