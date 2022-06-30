// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.gitlab

import com.github.swagger.akka.model.Info
import visdom.fetchers.gitlab.queries.all.AllDataService
import visdom.fetchers.gitlab.queries.commits.CommitService
import visdom.fetchers.gitlab.queries.events.EventService
import visdom.fetchers.gitlab.queries.files.FileService
import visdom.fetchers.gitlab.queries.info.InfoService
import visdom.fetchers.gitlab.queries.multi.MultiService
import visdom.fetchers.gitlab.queries.pipelines.PipelinesService
import visdom.fetchers.gitlab.queries.project.ProjectService
import visdom.http.server.swagger.SwaggerDocService


object SwaggerFetcherDocService extends SwaggerDocService {
    private val hostServerName: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentHostName,
        GitlabConstants.DefaultHostName
    )
    private val hostServerPort: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentHostPort,
        GitlabConstants.DefaultHostPort
    )

    override val host = List(hostServerName, hostServerPort).mkString(":")
    override val info: Info = Info(version = GitlabConstants.FetcherVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CommitService],
        classOf[FileService],
        classOf[PipelinesService],
        classOf[ProjectService],
        classOf[EventService],
        classOf[AllDataService],
        classOf[MultiService],
        classOf[InfoService]
    )
}
