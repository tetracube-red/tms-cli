package red.tetracube.tms.operations.installation

import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InstallationOrchestrator(
    private val namespaceOperations: NamespaceOperations,
    private val databaseOperations: DatabaseOperations,
    private val gatekeeperOperations: GatekeeperOperations,
    private val gatekeeperInitializationOperations: GatekeeperInitializationOperations,
) {

    private val logger = LoggerFactory.getLogger(InstallationOrchestrator::class.java)

    fun doInstallation() {
        logger.info("Creating namespace")
        this.namespaceOperations.publishNamespace()

        logger.info("Deploying database")
        this.databaseOperations.installDatabase()

        logger.info("Deploying gatekeeper")
        this.gatekeeperOperations.installGatekeeper()

        logger.info("Setup gatekeeper")
        this.gatekeeperInitializationOperations.createRealm()
    }
}
