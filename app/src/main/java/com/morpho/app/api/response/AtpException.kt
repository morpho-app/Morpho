package morpho.app.api.response

import java.io.IOException

class AtpException(
  val statusCode: StatusCode,
) : IOException("XRPC request failed: ${statusCode::class.simpleName}")
