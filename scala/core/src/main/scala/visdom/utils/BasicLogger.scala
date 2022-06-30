// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import visdom.utils.CommonConstants.Dollar


trait BasicLogger {
    val log: Logger = LoggerFactory.getLogger(this.getClass().getName().stripSuffix(Dollar))
}
