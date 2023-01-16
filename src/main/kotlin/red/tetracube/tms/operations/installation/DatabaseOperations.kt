package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import red.tetracube.tms.properties.TMSConfigProperties
import java.io.File
import java.io.FileInputStream
import javax.enterprise.context.ApplicationScoped
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


@ApplicationScoped
class DatabaseOperations(
    private val kubernetesClient: KubernetesClient,
    private val genericKubernetesOperations: GenericKubernetesOperations,
    private val tmsConfigProperties: TMSConfigProperties
) {

    private val logger = LoggerFactory.getLogger(DatabaseOperations::class.java)

    fun installDatabase() {
        logger.info("Creating database secrets")
        this.createSecrets()
        logger.info("Creating database config map")
        this.createConfigMap()
        logger.info("Creating database volumes")
        this.createVolumes()
        logger.info("Creating database config map for db-init")
        this.createInitDBConfigMap()
        logger.info("Deploying database service")
        this.createDeployment()
        logger.info("Exposing network services")
        this.createNetworkServices()
    }

    private fun createSecrets() {
        val secretsData = mapOf(
            Pair("postgres-user", tmsConfigProperties.dbUsername),
            Pair("postgres-password", tmsConfigProperties.dbPassword)
        )
            .toMutableMap()
        this.genericKubernetesOperations.createSecret(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.dbSecretName(),
            tmsConfigProperties.dbApplicationName,
            tmsConfigProperties.namespaceName(),
            secretsData
        )
    }

    private fun createConfigMap() {
        val configMapData = mapOf(
            Pair("postgres_db", tmsConfigProperties.dbName),
            Pair("pgdata", tmsConfigProperties.dbPgDataPath)
        )
            .toMutableMap()
        this.genericKubernetesOperations.createConfigMap(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.dbConfigMaps(),
            tmsConfigProperties.dbApplicationName,
            tmsConfigProperties.namespaceName(),
            configMapData
        )
    }

    private fun createVolumes() {
        val storageCapacity = QuantityBuilder()
            .withFormat("Gi")
            .withAmount("5")
            .build()
        this.genericKubernetesOperations.createPersistentVolume(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.dbApplicationName,
            storageCapacity,
            tmsConfigProperties.dbDataPath
        )
        this.genericKubernetesOperations.createPersistentVolumeClaim(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.dbApplicationName,
            storageCapacity,
            tmsConfigProperties.dbPVCName()
        )
    }

    private fun createInitDBConfigMap() {
        val configMapData = mapOf<String, String>().toMutableMap()

        val sqlResources = DatabaseOperations::class.java.getResource("${File.separator}sql")
        val resourcesPath = if (sqlResources != null) {
            Path(sqlResources.path).listDirectoryEntries("*.sql")
        } else {
            return
        }
        resourcesPath.map {
            val fileStream = FileInputStream(it.toFile())
            configMapData[it.name] = String(fileStream.readAllBytes())
        }
        this.genericKubernetesOperations.createConfigMap(
            tmsConfigProperties.namespaceName(),
            tmsConfigProperties.dbInitConfigMapName(),
            tmsConfigProperties.dbApplicationName,
            tmsConfigProperties.namespaceName(),
            configMapData
        )
    }

    private fun createDeployment() {
        val deployment = DeploymentBuilder()
            .withNewMetadata()
            .withName(tmsConfigProperties.dbApplicationName)
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
                    Pair("name", tmsConfigProperties.dbApplicationName),
                    Pair("part-of", tmsConfigProperties.namespaceName()),
                )
            )
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withName(tmsConfigProperties.dbApplicationName)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", tmsConfigProperties.dbApplicationName),
                    Pair("part-of", tmsConfigProperties.namespaceName())
                )
            )
            .endMetadata()
            .withNewSpec()
            .withContainers(
                listOf(
                    ContainerBuilder()
                        .withName(tmsConfigProperties.dbApplicationName)
                        .withImage("postgres:alpine")
                        .withImagePullPolicy("IfNotPresent")
                        .withPorts(
                            ContainerPortBuilder()
                                .withName("tcp-port")
                                .withContainerPort(5432)
                                .build()
                        )
                        .withEnv(
                            EnvVarBuilder()
                                .withName("POSTGRES_PASSWORD")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withSecretKeyRef(
                                            SecretKeySelectorBuilder()
                                                .withName(tmsConfigProperties.dbSecretName())
                                                .withKey("postgres-password")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("POSTGRES_USER")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withSecretKeyRef(
                                            SecretKeySelectorBuilder()
                                                .withName(tmsConfigProperties.dbSecretName())
                                                .withKey("postgres-user")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("POSTGRES_DB")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.dbConfigMaps())
                                                .withKey("postgres_db")
                                                .withOptional(false)
                                                .build()
                                        )
                                        .build()
                                )
                                .build(),
                            EnvVarBuilder()
                                .withName("PGDATA")
                                .withValueFrom(
                                    EnvVarSourceBuilder()
                                        .withConfigMapKeyRef(
                                            ConfigMapKeySelectorBuilder()
                                                .withName(tmsConfigProperties.dbConfigMaps())
                                                .withKey("pgdata")
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
                                    .withMountPath("/var/lib/postgresql/data")
                                    .withName("tetracube-db-data-volume")
                                    .build(),
                                VolumeMountBuilder()
                                    .withMountPath("/docker-entrypoint-initdb.d")
                                    .withName("db-init-scripts")
                                    .build()
                            )
                        )
                        .build()
                )
            )
            .withVolumes(
                listOf(
                    VolumeBuilder()
                        .withName("tetracube-db-data-volume")
                        .withPersistentVolumeClaim(
                            PersistentVolumeClaimVolumeSourceBuilder()
                                .withClaimName(tmsConfigProperties.dbPVCName())
                                .build()
                        )
                        .build(),
                    VolumeBuilder()
                        .withName("db-init-scripts")
                        .withNewConfigMap()
                        .withName(tmsConfigProperties.dbInitConfigMapName())
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
            .inNamespace(tmsConfigProperties.namespaceName())
            .resource(deployment)
            .createOrReplace()
    }

    private fun createNetworkServices() {
        val ports = listOf(
            ServicePortBuilder()
                .withName("tcp")
                .withPort(5432)
                .withTargetPort(IntOrString(5432))
                .withProtocol("TCP")
                .build()
        )
        this.genericKubernetesOperations
            .createService(
                tmsConfigProperties.dbInternalNetworkName(),
                "ClusterIP",
                tmsConfigProperties.namespaceName(),
                tmsConfigProperties.namespaceName(),
                tmsConfigProperties.dbApplicationName,
                ports
            )
        if (tmsConfigProperties.installationExposeServices) {
            this.genericKubernetesOperations
                .createService(
                    tmsConfigProperties.dbExternalNetworkName(),
                    "LoadBalancer",
                    tmsConfigProperties.namespaceName(),
                    tmsConfigProperties.namespaceName(),
                    tmsConfigProperties.dbApplicationName,
                    ports
                )
        }
    }

}