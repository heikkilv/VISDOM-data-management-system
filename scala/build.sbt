scalaVersion := "2.12.13"

val coreDirectory: String = "core"
val brokerDirectory: String = "broker"
val gitlabFetcherDirectory: String = "gitlab-fetcher"
val gitlabAdapterDirectory: String = "gitlab-adapter"

lazy val core = project
    .in(file(coreDirectory))

lazy val dataBroker = project
    .in(file(brokerDirectory))
    .dependsOn(core)

lazy val gitlabFetcher = project
    .in(file(gitlabFetcherDirectory))
    .dependsOn(core)

lazy val gitlabAdapter = project
    .in(file(gitlabAdapterDirectory))
    .dependsOn(core)
