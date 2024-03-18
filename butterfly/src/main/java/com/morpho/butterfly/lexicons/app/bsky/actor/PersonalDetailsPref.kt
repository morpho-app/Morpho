package app.bsky.actor

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class PersonalDetailsPref(
  /**
   * The birth date of the owner of the account.
   */
  public val birthDate: Timestamp? = null,
)

