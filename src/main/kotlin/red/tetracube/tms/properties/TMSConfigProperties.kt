package red.tetracube.tms.properties

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.inject.Singleton

@Singleton
data class TMSConfigProperties(
    @ConfigProperty(name = "tms-cli.db.application-name")
    val dbApplicationName: String,

    @ConfigProperty(name = "tms-cli.db.name")
    val dbName: String,

    @ConfigProperty(name = "tms-cli.db.username")
    val dbUsername: String,

    @ConfigProperty(name = "tms-cli.db.pg-data")
    val dbPgDataPath: String,

    @ConfigProperty(name = "tms-cli.mobile.client-id")
    val mobileClientId: String
) {

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

    fun gatekeeperDbConnectionString(): String {
        return "jdbc:postgresql://${dbInternalNetworkName()}:5432/$dbName?currentSchema=gatekeeper"
    }

}