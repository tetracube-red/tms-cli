package red.tetracube.tms.operations.houses.create.payloads

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CreateHouseAPIRequest @JsonCreator constructor(
    @JsonProperty("name")
    val name: String
)