package visdom.http.server.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info


abstract trait SwaggerDocService extends SwaggerHttpService {
    override val info: Info = Info(version = SwaggerConstants.SwaggerJsonVersionDefault)
    override val unwantedDefinitions: Seq[String] = SwaggerConstants.SwaggerJsonUnwantedDefinitions
}
