package visdom.http.server.services.constants


object BrokerDescriptions {
    final val BrokerInfoEndpointDescription = "Returns information about the data broker."
    final val BrokerInfoEndpointSummary = "Returns data broker info."

    final val AdaptersEndpointDescription = "Returns information about the active data adapters."
    final val AdaptersEndpointSummary = "Returns active data adapter info."

    final val FetchersEndpointDescription = "Returns information about the active data fetchers."
    final val FetchersEndpointSummary = "Returns active data fetcher info."

    final val BrokerQueryLogEntry = "Received query for active components of type: "
}
