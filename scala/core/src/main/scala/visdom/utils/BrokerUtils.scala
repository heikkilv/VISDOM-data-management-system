// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import visdom.fetchers.FetcherType
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.EnrichedBsonDocument


object BrokerUtils {
    def getFetcherRequestUrl(): String = {
        Seq(
            EnvironmentVariables.getEnvironmentVariable(EnvironmentVariables.EnvironmentDataBrokerAddress),
            PathConstants.Fetchers
        ).mkString(CommonConstants.Slash)
    }

    def getFetcherResponse(): Option[String] = {
        HttpUtils.getRequestBody(
            HttpUtils.getSimpleRequest(getFetcherRequestUrl()),
            HttpConstants.StatusCodeOk
        )
    }

    def getFetchers(): Seq[FetcherType] = {
        getFetcherResponse() match {
            case Some(responseBody: String) => {
                HttpUtils.responseToDocumentArrayCaseArray(responseBody)
                    .flatMap(responseDocument => FetcherType.fromBsonDocument(responseDocument))
                    .toSeq
            }
            case None => Seq.empty
        }
    }

    def getFetcherAddress(fetcherType: String, sourceServer: String): Option[String] = {
        getFetchers()
            .filter(fetcher => fetcher.isMatchingFetcher(fetcherType, sourceServer))
            .map(fetcher => fetcher.apiAddress)
            .headOption  // NOTE: assumption that only one active fetcher per source server
    }
}
