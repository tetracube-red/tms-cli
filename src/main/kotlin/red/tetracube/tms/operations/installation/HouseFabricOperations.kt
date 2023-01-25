package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import red.tetracube.tms.properties.TMSConfigProperties
import red.tetracube.tms.properties.TMSConfiguration
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class HouseFabricOperations(
    private val kubernetesClient: KubernetesClient,
    private val genericKubernetesOperations: GenericKubernetesOperations,
    private val tmsConfigProperties: TMSConfigProperties,
    private val tmsCliConfiguration: TMSConfiguration
)  {

    private val logger = LoggerFactory.getLogger(DatabaseOperations::class.java)

    fun installHouseFabric() {
        logger.info("Creating house fabric secrets")
        this.createSecrets()
        logger.info("Creating house fabric config map")
        this.createConfigMap()
       /* logger.info("Creating database volumes")
        this.createVolumes()
        logger.info("Creating database config map for db-init")
        this.createInitDBConfigMap()
        logger.info("Deploying database service")
        this.createDeployment()
        logger.info("Exposing network services")
        this.createNetworkServices()*/
    }

    private fun createSecrets() {
        val secretsData = mapOf(
            Pair("db-user", tmsConfigProperties.dbUsername),
            Pair("db-password", tmsCliConfiguration.database!!.password!!)
        )
            .toMutableMap()
        this.genericKubernetesOperations.createSecret(
            tmsCliConfiguration.namespaceName(),
            tmsConfigProperties.houseFabricSecretName(),
            tmsConfigProperties.houseFabricApplicationName,
            tmsCliConfiguration.namespaceName(),
            secretsData
        )
    }

    private fun createConfigMap() {
        val configMapData = mapOf(
            Pair("db-name", tmsConfigProperties.dbName)
        )
            .toMutableMap()
        this.genericKubernetesOperations.createConfigMap(
            tmsCliConfiguration.namespaceName(),
            tmsConfigProperties.houseFabricConfigMaps(),
            tmsConfigProperties.houseFabricApplicationName,
            tmsCliConfiguration.namespaceName(),
            configMapData
        )
    }

}