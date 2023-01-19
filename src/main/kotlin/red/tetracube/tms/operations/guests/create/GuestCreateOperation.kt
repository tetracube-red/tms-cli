package red.tetracube.tms.operations.guests.create

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import red.tetracube.tms.clients.TetraResteasyReactiveClientProvider
import red.tetracube.tms.properties.TMSConfigProperties
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class GuestCreateOperation(
    private val tmsConfigProperties: TMSConfigProperties
) {

    private val logger: Logger = LoggerFactory.getLogger(GuestCreateOperation::class.java)

    private lateinit var keycloak: Keycloak

    @PostConstruct
    fun initKeycloak() {
        var resteasyClient = TetraResteasyReactiveClientProvider()
            .newRestEasyClient(null, null, true)
        keycloak = KeycloakBuilder.builder()
            .serverUrl(tmsConfigProperties.gatekeeperBasePath())
            .realm("master")
            .username(tmsConfigProperties.gatekeeperAdminUsername)
            .password(tmsConfigProperties.gatekeeperPassword)
            .clientId("admin-cli")
            .resteasyClient(resteasyClient)
            .build()
    }

    fun createUser() {
        val userCredential = CredentialRepresentation()
        userCredential.isTemporary = false
        userCredential.type = CredentialRepresentation.PASSWORD
        userCredential.credentialData = "testing_data"
        userCredential.secretData = "testing_data_password"

        val user = UserRepresentation()
        user.username = "dave"
        user.isEnabled = true
        user.isEmailVerified = true

        logger.info("Creating guest into keycloak")
        val response = keycloak.realm(tmsConfigProperties.namespaceName())
            .users()
            .create(user)
        if (response.status != 201) {
            logger.warn("Operation failed: {} -> {}", response.status, response.entity)
            return;
        }

        val userId = response.readEntity(UserRepresentation::class.java).id

        logger.info("Setting guest's password")
        keycloak.realm(tmsConfigProperties.namespaceName())
            .users()[userId]
            .resetPassword(userCredential)
    }
}