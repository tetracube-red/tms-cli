package red.tetracube.core.kuberentes;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ConfigurationOperations {

    private final KubernetesClient kubernetesClient;

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationOperations.class);

    public ConfigurationOperations(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void createSecret(String namespace,
                             String installationName,
                             String applicationName,
                             String secretName,
                             Map<String, String> secretsData) {
        secretsData.keySet()
                .forEach(key ->
                        secretsData.replace(
                                key,
                                Base64.getEncoder().encodeToString(secretsData.get(key).getBytes(StandardCharsets.UTF_8)
                                )
                        )
                );
        var metadata = new ObjectMetaBuilder()
                .withName(secretName)
                .withLabels(
                        new HashMap<>() {{
                            put("name", applicationName);
                            put("part-of", installationName);
                        }}
                )
                .build();
        var k8sSecret = new SecretBuilder()
                .withMetadata(metadata)
                .withData(secretsData)
                .build();
        try {
            LOGGER.info("🚀 Creating {} secret", secretName);
            kubernetesClient.secrets()
                    .inNamespace(namespace)
                    .resource(k8sSecret)
                    .create();
            LOGGER.info("✔ Secret {} created", secretName);
        } catch (KubernetesClientException kubernetesClientException) {
            if (kubernetesClientException.getCode() == 409) {
                LOGGER.info("⚠ Secret {} already exists", secretName);
            }
        }
    }

    public void createConfigMap(String namespace,
                                String installationName,
                                String applicationName,
                                String configMapName,
                                Map<String, String> configMapData) {
        var metadata = new ObjectMetaBuilder()
                .withName(configMapName)
                .withLabels(
                        new HashMap<>() {{
                            put("name", applicationName);
                            put("part-of", installationName);
                        }}
                )
                .build();
        var k8sConfigMap = new ConfigMapBuilder()
                .withMetadata(metadata)
                .withData(configMapData)
                .build();
        try {
            LOGGER.info("🚀 Creating {} config map", configMapName);
            kubernetesClient.configMaps()
                    .inNamespace(namespace)
                    .resource(k8sConfigMap)
                    .create();
            LOGGER.info("✔ Config map {} created", configMapName);
        } catch (KubernetesClientException kubernetesClientException) {
            if (kubernetesClientException.getCode() == 409) {
                LOGGER.info("⚠ Config map {} already exists", configMapName);
            }
        }
    }

}
