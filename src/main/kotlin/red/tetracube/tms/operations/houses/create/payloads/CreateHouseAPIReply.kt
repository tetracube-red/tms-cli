package red.tetracube.tms.operations.houses.create.payloads

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class CreateHouseAPIReply @JsonCreator constructor(
    @JsonProperty
    var name: String,

    @JsonProperty
    val id: UUID,

    @JsonProperty
    val slug: String
)