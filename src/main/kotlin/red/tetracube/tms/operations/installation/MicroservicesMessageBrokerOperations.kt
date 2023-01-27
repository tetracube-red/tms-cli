package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import red.tetracube.tms.properties.TMSConfigProperties
import red.tetracube.tms.properties.TMSConfiguration
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class MicroservicesMessageBrokerOperations(
    private val kubernetesClient: KubernetesClient,
    private val genericKubernetesOperations: GenericKubernetesOperations,
    private val tmsConfigProperties: TMSConfigProperties,
    private val tmsCliConfiguration: TMSConfiguration
) {

    private val logger = LoggerFactory.getLogger(MicroservicesMessageBrokerOperations::class.java)

    fun installBroker() {
        logger.info("Deploying message broker deployment")
        this.createDeployment()
        logger.info("Exposing network services")
        this.createNetworkServices()
    }

    private fun createDeployment() {
        val deployment = DeploymentBuilder()
            .withNewMetadata()
            .withName(tmsConfigProperties.redisApplicationName)
            .withLabels<String, String>(
                mapOf(
                    Pair("part-of", tmsCliConfiguration.namespaceName()),
                )
            )
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .withNewSelector()
            .withMatchLabels<String, String>(
                mapOf(
                    Pair("name", tmsConfigProperties.redisApplicationName),
                    Pair("part-of", tmsCliConfiguration.namespaceName()),
                )
            )
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withName(tmsConfigProperties.redisApplicationName)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", tmsConfigProperties.redisApplicationName),
                    Pair("part-of", tmsCliConfiguration.namespaceName())
                )
            )
            .endMetadata()
            .withNewSpec()
            .withContainers(
                listOf(
                    ContainerBuilder()
                        .withName(tmsConfigProperties.redisApplicationName)
                        .withImage("redis:7.0.8-bullseye")
                        .withImagePullPolicy("IfNotPresent")
                        .withPorts(
                            ContainerPortBuilder()
                                .withName("tcp")
                                .withContainerPort(6379)
                                .build()
                        )
                        .build()
                )
            )
            .endSpec()
            .endTemplate()
            .endSpec()
            .build()

        this.kubernetesClient
            .apps()
            .deployments()
            .inNamespace(tmsCliConfiguration.namespaceName())
            .resource(deployment)
            .createOrReplace()
    }

    private fun createNetworkServices() {
        val ports = listOf(
            ServicePortBuilder()
                .withName("tcp")
                .withPort(6379)
                .withTargetPort(IntOrString(6379))
                .withProtocol("TCP")
                .build()
        )
        this.genericKubernetesOperations
            .createService(
                tmsConfigProperties.redisInternalNetworkName(),
                null,
                tmsCliConfiguration.namespaceName(),
                tmsCliConfiguration.namespaceName(),
                tmsConfigProperties.redisApplicationName,
                ports
            )
        if (tmsCliConfiguration.exposeServices!!) {
            val extPort = listOf(
                ServicePortBuilder()
                    .withName("tcp")
                    .withPort(6379)
                    .withTargetPort(IntOrString(6379))
                    .withProtocol("TCP")
                    .build()
            )
            this.genericKubernetesOperations
                .createService(
                    tmsConfigProperties.redisExternalNetworkName(),
                    "LoadBalancer",
                    tmsCliConfiguration.namespaceName(),
                    tmsCliConfiguration.namespaceName(),
                    tmsConfigProperties.redisApplicationName,
                    extPort
                )
        }
    }

}