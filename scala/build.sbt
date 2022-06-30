// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

scalaVersion := "2.12.15"

val coreDirectory: String = "core"
val brokerDirectory: String = "broker"

val gitlabFetcherDirectory: String = "gitlab-fetcher"
val aPlusFetcherDirectory: String = "aplus-fetcher"

val coreAdapterDirectory: String = "core-adapter"
val gitlabAdapterDirectory: String = "gitlab-adapter"
val adapterCourseDirectory: String = "adapter-course"
val adapterGeneralModelDirectory: String = "adapter-general-model"
val adapterDatasetDirectory: String = "adapter-dataset"

lazy val core = project
    .in(file(coreDirectory))

lazy val dataBroker = project
    .in(file(brokerDirectory))
    .dependsOn(core)

lazy val gitlabFetcher = project
    .in(file(gitlabFetcherDirectory))
    .dependsOn(core)

lazy val aPlusFetcher = project
    .in(file(aPlusFetcherDirectory))
    .dependsOn(core)

lazy val coreAdapter = project
    .in(file(coreAdapterDirectory))
    .dependsOn(core)

lazy val gitlabAdapter = project
    .in(file(gitlabAdapterDirectory))
    .dependsOn(core)

lazy val adapterCourse = project
    .in(file(adapterCourseDirectory))
    .dependsOn(core)

lazy val adapterGeneralModel = project
    .in(file(adapterGeneralModelDirectory))
    .dependsOn(core)
    .dependsOn(coreAdapter)

lazy val adapterDataset = project
    .in(file(adapterDatasetDirectory))
    .dependsOn(core)
    .dependsOn(coreAdapter)
    .dependsOn(adapterGeneralModel)

dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang")
