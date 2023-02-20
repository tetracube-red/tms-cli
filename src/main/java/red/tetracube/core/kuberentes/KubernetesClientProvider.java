package red.tetracube.core.kuberentes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.tetracube.install.InstallOptions;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;

@Singleton
public class KubernetesClientProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(KubernetesClientProvider.class);

    @Singleton
    @Produces
    public KubernetesClient kubernetesClient(InstallOptions installOptions) throws IOException {
        LOGGER.info("Loading config file from {}", installOptions.getK8sFile().getName());
        try (var k8sFileInStream = new FileInputStream(installOptions.getK8sFile())) {
            var k8sFileContent = new String(k8sFileInStream.readAllBytes());

            LOGGER.info("Building Kubernetes client");
            var k8sClientConfig = Config.fromKubeconfig(k8sFileContent);
            return new KubernetesClientBuilder()
                    .withConfig(k8sClientConfig)
                    .build();
        }
    }
}
