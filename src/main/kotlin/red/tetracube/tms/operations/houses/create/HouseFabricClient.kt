package red.tetracube.tms.operations.houses.create

import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import red.tetracube.tms.operations.houses.create.payloads.CreateHouseAPIReply
import red.tetracube.tms.operations.houses.create.payloads.CreateHouseAPIRequest
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@RegisterRestClient()
@Path("/houses")
interface HouseFabricClient {

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createHouse(request: @Valid CreateHouseAPIRequest): CreateHouseAPIReply

}