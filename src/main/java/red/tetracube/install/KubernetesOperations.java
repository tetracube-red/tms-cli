package red.tetracube.install;

import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@ApplicationScoped
public class KubernetesOperations {

    @Inject
    KubernetesClient kubernetesClient;

    public void createSecret(
            String namespace,
            String secretName,
            String applicationName,
            String partOfApplication,
            HashMap<String, String> secretsData
    ) {
        for (var key : secretsData.keySet()) {
            secretsData.replace(key, Base64.getEncoder().encodeToString(secretsData.get(key).getBytes(StandardCharsets.UTF_8)));
        }
        var secret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .withLabels(
                        new HashMap<>() {{
                            put("name", applicationName);
                            put("part-of", partOfApplication);
                        }}
                )
                .endMetadata()
                .withData(secretsData)
                .build();
        this.kubernetesClient
                .secrets()
                .inNamespace(namespace)
                .resource(secret)
                .createOrReplace();
    }

    fun createConfigMap(
            namespace:String,
            configMapName:String,
            applicationName:String,
            partOfApplication:String,
            configMapData:MutableMap<String, String>
    ) {
        val configMap = ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapName)
            .withLabels<String, String> (
                mapOf(
                        Pair("name", applicationName),
                        Pair("part-of", partOfApplication),
                        )
        )
                .endMetadata()
                .withData < String, String > (configMapData)
                .build()
        this.kubernetesClient
                .configMaps()
                .inNamespace(namespace)
                .resource(configMap)
                .createOrReplace()
    }

    fun createPersistentVolume(
            partOfApplication:String,
            applicationName:String,
            capacity:Quantity,
            dbDataPath:String,
            affinityNode:String
    ) {
        val persistentVolume = PersistentVolumeBuilder()
                .withNewMetadata()
                .withName("pv-$partOfApplication-$applicationName")
                .endMetadata()
                .withNewSpec()
                .withAccessModes(listOf("ReadWriteOnce"))
            .withCapacity<String, Quantity> (
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
                                                .withValues(affinityNode)
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
            namespace:String,
            partOfApplication:String,
            applicationName:String,
            capacity:Quantity,
            pvcName:String
    ) {
        val persistentVolumeClaim = PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(pvcName)
                .endMetadata()
                .withNewSpec()
                .withStorageClassName("local-storage")
                .withAccessModes(listOf("ReadWriteOnce"))
                .withNewResources()
            .withRequests<String, Quantity> (
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
            serviceName:String,
            serviceType:String?,
            namespace:String,
            partOfApplication:String,
            applicationName:String,
            servicePorts:List<ServicePort>
    ) {
        val serviceBuilder = ServiceBuilder()
                .withNewMetadata()
                .withName(serviceName)
                .withNamespace(namespace)
            .withLabels<String, String> (
                mapOf(
                        Pair("name", applicationName),
                        Pair("part-of", partOfApplication),
                        )
        )
                .endMetadata()
                .withNewSpec()
                .withPorts(servicePorts)
                .withSelector < String, String > (
                mapOf(
                        Pair("name", applicationName),
                        Pair("part-of", partOfApplication),
                        )
        )

        serviceType ?.let {
            serviceBuilder.withType(it)
        }

        val service = serviceBuilder.endSpec()
                .build()

        this.kubernetesClient
                .services()
                .inNamespace(namespace)
                .resource(service)
                .createOrReplace()
    }

    fun createIngress(
            namespaceName:String,
            ingressName:String,
            ingressHost:String,
            linkedServiceName:String,
            linkedPort:Int
    ) {
        val ingress = IngressBuilder()
                .withNewMetadata()
                .withName(ingressName)
            .withAnnotations<String, String> (
                mapOf(
                        Pair("kubernetes.io/ingress.class", "nginx"),
                        Pair("ingress.kubernetes.io/ssl-redirect", "false")
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
}
