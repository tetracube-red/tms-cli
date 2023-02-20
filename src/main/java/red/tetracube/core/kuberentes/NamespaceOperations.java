package red.tetracube.core.kuberentes;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;

@ApplicationScoped
public class NamespaceOperations {

    private final KubernetesClient kubernetesClient;

    private final static Logger LOGGER = LoggerFactory.getLogger(NamespaceOperations.class);

    public NamespaceOperations(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void createNamespace(String installationName, String name) {
        var metadata = new ObjectMetaBuilder()
                .withName(name)
                .withLabels(
                        new HashMap<>() {{
                            put("installation-name", installationName);
                        }}
                )
                .build();
        var k8sNamespace = new NamespaceBuilder()
                .withMetadata(metadata)
                .build();
        try {
            LOGGER.info("🏗 Creating namespace");
            this.kubernetesClient.namespaces()
                    .resource(k8sNamespace)
                    .create();
            LOGGER.info("✔ Namespace created");
        } catch (KubernetesClientException kubernetesClientException) {
            if (kubernetesClientException.getCode() == 409) {
                LOGGER.info("⚠ Namespace already exists, skipping creation");
            }
        }
    }
}
