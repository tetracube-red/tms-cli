package red.tetracube.tms.operations.installation

import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InstallationOrchestrator(
    private val namespaceOperations: NamespaceOperations,
    private val databaseOperations: DatabaseOperations,
    private val houseFabricOperations: HouseFabricOperations,
    private val kafkaOperations: KafkaOperations
) {

    private val logger = LoggerFactory.getLogger(InstallationOrchestrator::class.java)

    fun doInstallation() {
        logger.info("Creating namespace")
        this.namespaceOperations.publishNamespace()

        logger.info("Deploying database")
        this.databaseOperations.installDatabase()

        logger.info("Deploying message broker")
        this.kafkaOperations.installBroker()

        logger.info("Deploying hose fabric service")
        this.houseFabricOperations.installHouseFabric()
    }
}
