package com.morpho.butterfly.xrpc

import com.morpho.butterfly.response.AtpError

class XrpcSubscriptionParseException(
  val error: AtpError?,
) : RuntimeException("Subscription result could not be parsed")
