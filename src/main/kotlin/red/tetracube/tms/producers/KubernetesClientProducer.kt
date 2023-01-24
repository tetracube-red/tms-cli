package red.tetracube.tms.producers

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import org.slf4j.LoggerFactory
import red.tetracube.tms.properties.TMSConfiguration
import java.io.File
import java.io.FileInputStream
import javax.enterprise.inject.Produces
import javax.inject.Singleton


@Singleton
class KubernetesClientProducer() {

    private val logger = LoggerFactory.getLogger(KubernetesClientProducer::class.java)

    @Produces
    fun kubernetesClient(tmsConfiguration: TMSConfiguration): KubernetesClient {
        val k8sFile = File(tmsConfiguration.kubernetes!!.kubernetesConfig!!)
        logger.info("Loading config file from {}", k8sFile.name)
        val k8sFileInStream = FileInputStream(k8sFile)
        val k8sFileContent = String(k8sFileInStream.readAllBytes())
        logger.info("Building Kubernetes client")
        val k8sClientConfig = Config.fromKubeconfig(k8sFileContent)
        return KubernetesClientBuilder()
            .withConfig(k8sClientConfig)
            .build()
    }
}
