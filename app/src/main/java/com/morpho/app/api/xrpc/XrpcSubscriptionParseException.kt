package morpho.app.api.xrpc

import morpho.app.api.response.AtpErrorDescription

class XrpcSubscriptionParseException(
  val error: AtpErrorDescription?,
) : RuntimeException("Subscription result could not be parsed")
