package visdom.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import visdom.utils.CommonConstants.Dollar


trait BasicLogger {
    val log: Logger = LoggerFactory.getLogger(this.getClass().getName().stripSuffix(Dollar))
}
