package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import red.tetracube.tms.properties.TMSConfigProperties
import java.io.File
import java.io.FileInputStream
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class GatekeeperOperations(
    private val kubernetesClient: KubernetesClient,
    private val genericKubernetesOperations: GenericKubernetesOperations,
    private val tmsConfigProperties: TMSConfigProperties
) {

    private val logger = LoggerFactory.getLogger(GatekeeperOperations::class.java)

    fun installGatekeeper() {
        logger.info("Creating gatekeeper secrets")
        this.createSecrets()
        logger.info("Creating gatekeeper config map")
        this.createConfigMap()
        logger.info("Creating gatekeeper keystore")
        this.storeCertificates()
        logger.info("Deploying gatekeeper service")
        this.createDeployment()
        logger.info("Exposing network services")
        this.createNetworkServices()
    }

    private fun createSecrets() {
        val secretsData = mapOf(
            Pair("kc_db_username", tmsConfigProperties.dbUsername),
            Pair("kc_db_password", tmsConfigProperties.dbPassword),
            Pair("keycloak_admin", tmsConfigProperties.gatekeeperAdminUsername),
            Pair("keycloak_admin_password", tmsConfigProperties.gatekeeperPassword)
        )
            .toMutableMap()
        this.genericKubernetesOperations.createSecret(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.gatekeeperSecretName(),
            tmsConfigProperties.gatekeeperApplicationName,
            tmsConfigProperties.namespaceName(),
            secretsData
        )
    }

    private fun createConfigMap() {
        val configMapData = mapOf(
            Pair("kc_health_enabled", "false"),
            Pair("kc_metrics_enabled", "false"),
            Pair("kc_db", "postgres"),
            Pair("kc_db_url", tmsConfigProperties.gatekeeperDbConnectionString()),
            Pair("kc_hostname", tmsConfigProperties.gatekeeperHostname()),
        )
            .toMutableMap()
        this.genericKubernetesOperations.createConfigMap(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.gatekeeperConfigMapName(),
            tmsConfigProperties.gatekeeperApplicationName,
            tmsConfigProperties.namespaceName(),
            configMapData
        )
    }

    private fun storeCertificates() {
        val secretKeys = mapOf<String, String>().toMutableMap()
        tmsConfigProperties.solutionCertificates
            .map { c -> File(c) }
            .forEach {
                val certificateFileStream = FileInputStream(it)
                val certificateBinaryContent = certificateFileStream.readAllBytes()
                val certificateEncoded = String(certificateBinaryContent)
                secretKeys[it.name] = certificateEncoded
            }

        this.genericKubernetesOperations.createSecret(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.gatekeeperCertificatesSecret(),
            tmsConfigProperties.gatekeeperApplicationName,
            tmsConfigProperties.namespaceName(),
            secretKeys
        )
    }

    private fun createDeployment() {
        val deployment = DeploymentBuilder()
            .withNewMetadata()
            .withName(tmsConfigProperties.gatekeeperApplicationName)
            .withLabels<String, String>(
                mapOf(
                    Pair("part-of", tmsConfigProperties.namespaceName()),
                )
            )
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .withNewSelector()
            .withMatchLabels<String, String>(
                mapOf(
                    Pair("name", tmsConfigProperties.gatekeeperApplicationName),
                    Pair("part-of", tmsConfigProperties.namespaceName()),
                )
            )
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withName(tmsConfigProperties.gatekeeperApplicationName)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", tmsConfigProperties.gatekeeperApplicationName),
                    Pair("part-of", tmsConfigProperties.namespaceName())
                )
            )
            .endMetadata()
            .withNewSpec()
            .withContainers(
                listOf(
                    ContainerBuilder()
                        .withName(tmsConfigProperties.gatekeeperApplicationName)
                        .withImage("quay.io/keycloak/keycloak:latest")
                        .withImagePullPolicy("IfNotPresent")
                        .withPorts(
                            ContainerPortBuilder()
                                .withName("http-port")
                                .withContainerPort(8443)
                                .build()
                        )
                        .withEnv(
                            EnvVarBuilder()
                                .withName("KC_HEALTH_ENABLED")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperConfigMapName())
                                                .withKey("kc_health_enabled")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KC_METRICS_ENABLED")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperConfigMapName())
                                                .withKey("kc_metrics_enabled")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KC_DB")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperConfigMapName())
                                                .withKey("kc_db")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KC_DB_URL")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperConfigMapName())
                                                .withKey("kc_db_url")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KC_DB_USERNAME")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withSecretKeyRef(
                                            SecretKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperSecretName())
                                                .withKey("kc_db_username")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KC_DB_PASSWORD")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withSecretKeyRef(
                                            SecretKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperSecretName())
                                                .withKey("kc_db_password")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KEYCLOAK_ADMIN")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withSecretKeyRef(
                                            SecretKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperSecretName())
                                                .withKey("keycloak_admin")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KEYCLOAK_ADMIN_PASSWORD")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withSecretKeyRef(
                                            SecretKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperSecretName())
                                                .withKey("keycloak_admin_password")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("KC_HOSTNAME")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.gatekeeperConfigMapName())
                                                .withKey("kc_hostname")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .withVolumeMounts(
                            listOf(
                                VolumeMountBuilder()
                                    .withMountPath("/opt/keycloak/certs")
                                    .withName("certificate")
                                    .build()
                            )
                        )
                        .withArgs(
                            listOf(
                                "start",
                                "--https-certificate-file=/opt/keycloak/certs/keycloak-server.crt.pem",
                                "--https-certificate-key-file=/opt/keycloak/certs/keycloak-server.key.pem"
                            )
                        )
                        .build()
                )
            )
            .withVolumes(
                listOf(
                    VolumeBuilder()
                        .withName("certificate")
                        .withNewSecret()
                        .withSecretName(tmsConfigProperties.gatekeeperCertificatesSecret())
                        .endSecret()
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
            .inNamespace(tmsConfigProperties.namespaceName())
            .resource(deployment)
            .createOrReplace()
    }

    private fun createNetworkServices() {
        val ports = listOf(
            ServicePortBuilder()
                .withName("https")
                .withPort(8443)
                .withTargetPort(IntOrString(8443))
                .withProtocol("TCP")
                .build()
        )

        this.genericKubernetesOperations
            .createService(
                tmsConfigProperties.gatekeeperLoadBalancer(),
                "LoadBalancer",
                tmsConfigProperties.namespaceName(),
                tmsConfigProperties.namespaceName(),
                tmsConfigProperties.gatekeeperApplicationName,
                ports
            )
        this.genericKubernetesOperations
            .createIngress(
                tmsConfigProperties.namespaceName(),
                tmsConfigProperties.gatekeeperIngress(),
                tmsConfigProperties.gatekeeperHostname(),
                tmsConfigProperties.gatekeeperLoadBalancer(),
                8443
            )
    }

}
