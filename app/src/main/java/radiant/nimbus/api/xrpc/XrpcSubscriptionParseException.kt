package radiant.nimbus.api.xrpc

import radiant.nimbus.api.response.AtpErrorDescription

class XrpcSubscriptionParseException(
  val error: AtpErrorDescription?,
) : RuntimeException("Subscription result could not be parsed")
