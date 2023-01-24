package red.tetracube.tms.properties

class TMSConfiguration {

    var installationName: String? = null
    var exposeServices: Boolean? = null
    var hostname: String? = null
    var kubernetes: TMSConfigurationKubernetes? = null
    var certs: TMSConfigurationCertificates? = null
    var database: TMSConfigurationDatabase? = null
    var gatekeeper: TMSConfigurationGatekeeper? = null

    fun namespaceName(): String {
        return this.installationName!!
            .lowercase()
            .replace(" ", "-")
            .filter { it.isLetterOrDigit() || it == '-' }
    }

    fun gatekeeperHostname(): String {
        return "gk.$hostname"
    }

    fun gatekeeperBasePath(): String {
        return "https://${gatekeeperHostname()}:8443"
    }

}