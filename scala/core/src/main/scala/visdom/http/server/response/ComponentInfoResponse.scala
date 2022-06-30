// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.response


abstract class ComponentInfoResponse extends BaseResponse {
    def componentName: String
    def componentType: String
    def version: String
    def startTime: String
    def apiAddress: String
    def swaggerDefinition: String
}
