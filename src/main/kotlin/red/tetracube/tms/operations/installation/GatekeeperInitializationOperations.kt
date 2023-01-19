package red.tetracube.tms.operations.installation

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import red.tetracube.tms.clients.TetraResteasyReactiveClientProvider
import red.tetracube.tms.properties.TMSConfigProperties
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class GatekeeperInitializationOperations(
        private val tmsConfigProperties: TMSConfigProperties
) {

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

    fun createRealm() {
        val clientRepresentation = ClientRepresentation()
        clientRepresentation.clientId = "tetracube-mobile-app"
        clientRepresentation.isPublicClient = false
        clientRepresentation.isEnabled = true
        clientRepresentation.isDirectAccessGrantsEnabled = true
        clientRepresentation.isImplicitFlowEnabled = false
        clientRepresentation.isServiceAccountsEnabled = false
        clientRepresentation.isStandardFlowEnabled = false

        val realm = RealmRepresentation()
        realm.realm = tmsConfigProperties.namespaceName()
        realm.clients = listOf(clientRepresentation)

        keycloak.realms().create(realm)
    }
}
