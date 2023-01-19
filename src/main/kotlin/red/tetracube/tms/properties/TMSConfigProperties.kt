package red.tetracube.tms.properties

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.inject.Singleton

@Singleton
data class TMSConfigProperties(
    @ConfigProperty(name = "kubernetes-client.config-file")
    val kubernetesClientConfigFile: String,

    @ConfigProperty(name = "tms-cli.operation")
    val operationType: String,

    @ConfigProperty(name = "tms-cli.installation-name")
    val installationName: String,

    @ConfigProperty(name = "tms-cli.db.application-name")
    val dbApplicationName: String,

    @ConfigProperty(name = "tms-cli.db.name")
    val dbName: String,

    @ConfigProperty(name = "tms-cli.db.username")
    val dbUsername: String,

    @ConfigProperty(name = "tms-cli.db.password")
    val dbPassword: String,

    @ConfigProperty(name = "tms-cli.db.pg-data")
    val dbPgDataPath: String,

    @ConfigProperty(name = "tms-cli.db.data-path")
    val dbDataPath: String,

    @ConfigProperty(name = "tms-cli.installation.affinity-node-name")
    val installationAffinityNodeName: String,

    @ConfigProperty(name = "tms-cli.installation.expose-services")
    val installationExposeServices: Boolean,

    @ConfigProperty(name = "tms-cli.gatekeeper.application-name")
    val gatekeeperApplicationName: String,

    @ConfigProperty(name = "tms-cli.gatekeeper.admin-username")
    val gatekeeperAdminUsername: String,

    @ConfigProperty(name = "tms-cli.gatekeeper.admin-password")
    val gatekeeperPassword: String,

    @ConfigProperty(name = "tms-cli.solution.hostname")
    val solutionHostname: String,

    @ConfigProperty(name = "tms-cli.guest.name")
    val guestName: String,

    @ConfigProperty(name = "tms-cli.solution.certificates")
    val solutionCertificates: List<String>
) {

    fun namespaceName(): String {
        return this.installationName
            .lowercase()
            .replace(" ", "-")
            .filter { it.isLetterOrDigit() || it == '-' }
    }

    fun dbSecretName(): String {
        return "secrets-$dbApplicationName"
    }

    fun dbConfigMaps(): String {
        return "configs-$dbApplicationName"
    }

    fun dbPVCName(): String {
        return "pvc-$dbApplicationName"
    }

    fun dbInitConfigMapName(): String {
        return "init-db-configs-$dbApplicationName"
    }

    fun dbInternalNetworkName(): String {
        return "srv-$dbApplicationName-net"
    }

    fun dbExternalNetworkName(): String {
        return "lb-$dbApplicationName-net"
    }

    fun gatekeeperSecretName(): String {
        return "secrets-$gatekeeperApplicationName"
    }

    fun gatekeeperConfigMapName(): String {
        return "configs-$gatekeeperApplicationName"
    }

    fun gatekeeperDbConnectionString(): String {
        return "jdbc:postgresql://${dbInternalNetworkName()}:5432/$dbName?currentSchema=gatekeeper"
    }

    fun gatekeeperHostname(): String {
        return "gk.$solutionHostname"
    }
    fun gatekeeperBasePath(): String {
        return "https://${gatekeeperHostname()}:8443"
    }

    fun gatekeeperCertificatesSecret(): String {
        return "keystore-secret-$gatekeeperApplicationName"
    }

    fun gatekeeperLoadBalancer(): String {
        return "lb-$gatekeeperApplicationName"
    }

    fun gatekeeperIngress(): String {
        return "in-$gatekeeperApplicationName"
    }
}