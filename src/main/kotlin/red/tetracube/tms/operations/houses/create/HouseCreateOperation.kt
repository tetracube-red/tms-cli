package red.tetracube.tms.operations.houses.create

import org.eclipse.microprofile.rest.client.inject.RestClient
import org.slf4j.LoggerFactory
import red.tetracube.tms.operations.houses.create.payloads.CreateHouseAPIReply
import red.tetracube.tms.operations.houses.create.payloads.CreateHouseAPIRequest
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.WebApplicationException

@ApplicationScoped
class HouseCreateOperation {

    private val logger = LoggerFactory.getLogger(HouseCreateOperation::class.java)

    @Inject
    @RestClient
    lateinit var houseFabricClient: HouseFabricClient

    fun createHouse(houseName: String) {
        val createHouseAPIRequest = CreateHouseAPIRequest(houseName)
        val houseCreateAPICall = try {
            this.houseFabricClient.createHouse(createHouseAPIRequest)
        } catch (ex: WebApplicationException) {
            logger.warn("Cannot create house due {}", ex.response)
            null
        } ?: return
        logger.info(
            "House {} created with id {} and slug {}",
            houseCreateAPICall.name,
            houseCreateAPICall.id,
            houseCreateAPICall.slug
        )
    }

}