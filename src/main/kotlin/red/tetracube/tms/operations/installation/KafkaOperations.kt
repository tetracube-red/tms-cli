package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import red.tetracube.tms.properties.TMSConfigProperties
import red.tetracube.tms.properties.TMSConfiguration
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class KafkaOperations(
    private val kubernetesClient: KubernetesClient,
    private val genericKubernetesOperations: GenericKubernetesOperations,
    private val tmsConfigProperties: TMSConfigProperties,
    private val tmsCliConfiguration: TMSConfiguration
) {

    private val logger = LoggerFactory.getLogger(KafkaOperations::class.java)

    fun installBroker() {
        logger.info("Deploying kafka deployment")
        this.createDeployment()
        logger.info("Exposing network services")
        this.createNetworkServices()
    }

    private fun createSecrets() {
        /* val secretsData = mapOf(
             Pair("postgres-user", tmsConfigProperties.dbUsername),
             Pair("postgres-password", tmsCliConfiguration.database!!.password!!)
         )
             .toMutableMap()
         this.genericKubernetesOperations.createSecret(
             tmsCliConfiguration.namespaceName(),
             tmsConfigProperties.dbSecretName(),
             tmsConfigProperties.dbApplicationName,
             tmsCliConfiguration.namespaceName(),
             secretsData
         )*/
    }

    private fun createConfigMap() {
        /*  val configMapData = mapOf(
              Pair("postgres_db", tmsConfigProperties.dbName),
              Pair("pgdata", tmsConfigProperties.dbPgDataPath)
          )
              .toMutableMap()
          this.genericKubernetesOperations.createConfigMap(
              tmsCliConfiguration.namespaceName(),
              tmsConfigProperties.dbConfigMaps(),
              tmsConfigProperties.dbApplicationName,
              tmsCliConfiguration.namespaceName(),
              configMapData
          )*/
    }

    private fun createDeployment() {
        val deployment = DeploymentBuilder()
            .withNewMetadata()
            .withName(tmsConfigProperties.kafkaApplicationName)
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
                    Pair("name", tmsConfigProperties.kafkaApplicationName),
                    Pair("part-of", tmsCliConfiguration.namespaceName()),
                )
            )
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withName(tmsConfigProperties.kafkaApplicationName)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", tmsConfigProperties.kafkaApplicationName),
                    Pair("part-of", tmsCliConfiguration.namespaceName())
                )
            )
            .endMetadata()
            .withNewSpec()
            .withContainers(
                listOf(
                    ContainerBuilder()
                        .withName(tmsConfigProperties.kafkaApplicationName)
                        .withImage("bitnami/kafka:3.3.2-debian-11-r1")
                        .withImagePullPolicy("IfNotPresent")
                        .withPorts(
                            ContainerPortBuilder()
                                .withName("internal-tcp")
                                .withContainerPort(9092)
                                .build(),
                            ContainerPortBuilder()
                                .withName("external-tcp")
                                .withContainerPort(9093)
                                .build()
                        )
                        .withEnv(
                            EnvVarBuilder()
                                .withName("KAFKA_ENABLE_KRAFT")
                                .withValue("yes")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_PROCESS_ROLES")
                                .withValue("broker,controller")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_CONTROLLER_LISTENER_NAMES")
                                .withValue("CONTROLLER")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_LISTENERS")
                                // .withValue("CLIENT://:9092,CONTROLLER://:9094,EXTERNAL://:9093")
                                .withValue("SASL_SSL://:9092")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP")
                                .withValue("CONTROLLER:PLAINTEXT,CLIENT:PLAINTEXT,EXTERNAL:PLAINTEXT")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_ADVERTISED_LISTENERS")
                                .withValue("CLIENT://kafka:9092,EXTERNAL://127.0.0.1:9093")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE")
                                .withValue("true")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_BROKER_ID")
                                .withValue("1")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS")
                                .withValue("1@127.0.0.1:9094")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_BROKER_ID")
                                .withValue("1")
                                .build(),
                            EnvVarBuilder()
                                .withName("ALLOW_PLAINTEXT_LISTENER")
                                //.withValue("yes")
                                .withValue("no")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_CFG_INTER_BROKER_LISTENER_NAME")
                                .withValue("CLIENT")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_KRAFT_CLUSTER_ID")
                                .withValue("1")
                                .build(),
                            EnvVarBuilder()
                                .withName("KAFKA_TLS_TYPE")
                                .withValue("PEM")
                                .build()
                        )
                        .withVolumeMounts(
                            listOf(
                                VolumeMountBuilder()
                                    .withMountPath("/opt/bitnami/kafka/config/certs/")
                                    .withName("tetracube-certs")
                                    .build()
                            )
                        )
                        .build()
                )
            )
            .withVolumes(
                listOf(
                    VolumeBuilder()
                        .withName("tetracube-certs")
                        .withNewConfigMap()
                        //.withName(tmsConfigProperties.dbInitConfigMapName())
                        .withName("keystore-secret-tetracube-gatekeeper")
                        .endConfigMap()
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
                .withPort(9092)
                .withTargetPort(IntOrString(9092))
                .withProtocol("TCP")
                .build()
        )
        this.genericKubernetesOperations
            .createService(
                tmsConfigProperties.kafkaInternalNetworkName(),
                null,
                tmsCliConfiguration.namespaceName(),
                tmsCliConfiguration.namespaceName(),
                tmsConfigProperties.kafkaApplicationName,
                ports
            )
        if (tmsCliConfiguration.exposeServices!!) {
            val extPort = listOf(
                ServicePortBuilder()
                    .withName("tcp")
                    .withPort(9093)
                    .withTargetPort(IntOrString(9093))
                    .withProtocol("TCP")
                    .build()
            )
            this.genericKubernetesOperations
                .createService(
                    tmsConfigProperties.kafkaExternalNetworkName(),
                    "LoadBalancer",
                    tmsCliConfiguration.namespaceName(),
                    tmsCliConfiguration.namespaceName(),
                    tmsConfigProperties.kafkaApplicationName,
                    extPort
                )
        }
    }

}