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
}