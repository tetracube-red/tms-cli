package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import red.tetracube.tms.properties.TMSConfigProperties
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class GenericKubernetesOperations(
    private val kubernetesClient: KubernetesClient,
    private val tmsConfigProperties: TMSConfigProperties
) {

    fun createSecret(
        namespace: String,
        secretName: String,
        applicationName: String,
        partOfApplication: String,
        secretsData: MutableMap<String, String>
    ) {
        for (key in secretsData.keys) {
            secretsData[key] = Base64.getEncoder().encodeToString(secretsData[key]?.encodeToByteArray())
        }
        val secret = SecretBuilder()
            .withNewMetadata()
            .withName(secretName)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", applicationName),
                    Pair("part-of", partOfApplication),
                )
            )
            .endMetadata()
            .withData<String, String>(secretsData)
            .build()
        this.kubernetesClient
            .secrets()
            .inNamespace(namespace)
            .resource(secret)
            .createOrReplace()
    }

    fun createConfigMap(
        namespace: String,
        configMapName: String,
        applicationName: String,
        partOfApplication: String,
        configMapData: MutableMap<String, String>
    ) {
        val configMap = ConfigMapBuilder()
            .withNewMetadata()
            .withName(configMapName)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", applicationName),
                    Pair("part-of", partOfApplication),
                )
            )
            .endMetadata()
            .withData<String, String>(configMapData)
            .build()
        this.kubernetesClient
            .configMaps()
            .inNamespace(namespace)
            .resource(configMap)
            .createOrReplace()
    }

    fun createPersistentVolume(
        partOfApplication: String,
        applicationName: String,
        capacity: Quantity,
        dbDataPath: String
    ) {
        val persistentVolume = PersistentVolumeBuilder()
            .withNewMetadata()
            .withName("pv-$partOfApplication-$applicationName")
            .endMetadata()
            .withNewSpec()
            .withAccessModes(listOf("ReadWriteOnce"))
            .withCapacity<String, Quantity>(
                mapOf(
                    Pair("storage", capacity)
                )
            )
            .withStorageClassName("local-storage")
            .withVolumeMode("Filesystem")
            .withPersistentVolumeReclaimPolicy("Retain")
            .withNewLocal()
            .withPath(dbDataPath)
            .endLocal()
            .withNewNodeAffinity()
            .withNewRequired()
            .withNodeSelectorTerms(
                NodeSelectorTermBuilder()
                    .withMatchExpressions(
                        NodeSelectorRequirementBuilder()
                            .withKey("kubernetes.io/hostname")
                            .withOperator("In")
                            .withValues(tmsConfigProperties.installationAffinityNodeName)
                            .build()
                    )
                    .build()
            )
            .endRequired()
            .endNodeAffinity()
            .endSpec()
            .build()
        this.kubernetesClient
            .persistentVolumes()
            .resource(persistentVolume)
            .createOrReplace()
    }

    fun createPersistentVolumeClaim(
        namespace: String,
        partOfApplication: String,
        applicationName: String,
        capacity: Quantity,
        pvcName: String
    ) {
        val persistentVolumeClaim = PersistentVolumeClaimBuilder()
            .withNewMetadata()
            .withName(pvcName)
            .endMetadata()
            .withNewSpec()
            .withStorageClassName("local-storage")
            .withAccessModes(listOf("ReadWriteOnce"))
            .withNewResources()
            .withRequests<String, Quantity>(
                mapOf(
                    Pair("storage", capacity)
                )
            )
            .endResources()
            .endSpec()
            .build()
        val persistentVolumeClaimsExists = this.kubernetesClient
            .persistentVolumeClaims()
            .inNamespace(namespace)
            .resource(persistentVolumeClaim)
            .isReady
        if (persistentVolumeClaimsExists) {
            return
        }
        this.kubernetesClient
            .persistentVolumeClaims()
            .inNamespace(namespace)
            .resource(persistentVolumeClaim)
            .create()
    }

    fun createService(
        serviceName: String,
        serviceType: String,
        namespace: String,
        partOfApplication: String,
        applicationName: String,
        servicePorts: List<ServicePort>
    ) {
        val service = ServiceBuilder()
            .withNewMetadata()
            .withName(serviceName)
            .withNamespace(namespace)
            .withLabels<String, String>(
                mapOf(
                    Pair("name", applicationName),
                    Pair("part-of", partOfApplication),
                )
            )
            .endMetadata()
            .withNewSpec()
            .withType(serviceType)
            .withPorts(servicePorts)
            .withSelector<String, String>(
                mapOf(
                    Pair("name", applicationName),
                    Pair("part-of", partOfApplication),
                )
            )
            .endSpec()
            .build()

        this.kubernetesClient
            .services()
            .inNamespace(namespace)
            .resource(service)
            .createOrReplace()
    }

    fun createIngress(
        namespaceName: String,
        ingressName: String,
        ingressHost: String,
        linkedServiceName: String,
        linkedPort: Int
    ) {
        val ingress = IngressBuilder()
            .withNewMetadata()
            .withName(ingressName)
            .withAnnotations<String, String>(
                mapOf(
                    Pair("kubernetes.io/ingress.class", "nginx"),
                    Pair("ingress.kubernetes.io/ssl-redirect" , "false")
                )
            )
            .endMetadata()
            .withNewSpec()
            .withRules(
                listOf(
                    IngressRuleBuilder()
                        .withHost(ingressHost)
                        .withNewHttp()
                        .withPaths(
                            listOf(
                                HTTPIngressPathBuilder()
                                    .withPath("/")
                                    .withPathType("Prefix")
                                    .withNewBackend()
                                    .withNewService()
                                    .withName(linkedServiceName)
                                    .withNewPort()
                                    .withNumber(linkedPort)
                                    .endPort()
                                    .endService()
                                    .endBackend()
                                    .build()
                            )
                        )
                        .endHttp()
                        .build()
                )
            )
            .endSpec()
            .build()
        this.kubernetesClient
            .network()
            .v1()
            .ingresses()
            .inNamespace(namespaceName)
            .resource(ingress)
            .createOrReplace()
    }
}