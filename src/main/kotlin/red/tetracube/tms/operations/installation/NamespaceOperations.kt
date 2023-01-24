package red.tetracube.tms.operations.installation

import io.fabric8.kubernetes.api.model.NamespaceBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import red.tetracube.tms.properties.TMSConfiguration
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class NamespaceOperations(
    private val kubernetesClient: KubernetesClient,
    private val tmsCliConfiguration: TMSConfiguration
) {

    fun publishNamespace() {
        val namespace = NamespaceBuilder()
            .withNewMetadata()
            .withName(tmsCliConfiguration.namespaceName())
            .withLabels<String, String>(
                mapOf(
                    Pair<String, String>("installation-name", tmsCliConfiguration.installationName!!)
                )
            )
            .and()
            .build()

        this.kubernetesClient.namespaces()
            .resource(namespace)
            .createOrReplace()
    }

}
