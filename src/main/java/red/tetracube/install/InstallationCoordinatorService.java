package red.tetracube.install;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InstallationCoordinatorService {

    @Inject
    InstallOptions installOptions;

    @Inject
    KubernetesClient kubernetesClient;

    private final Logger LOGGER = LoggerFactory.getLogger(InstallationCoordinatorService.class);

    public void startInstallation() {
        LOGGER.info("Starting installation for {}", installOptions.getInstallationName());
        LOGGER.info("Kubernetes version {}.{}", kubernetesClient.getKubernetesVersion().getMajor(), kubernetesClient.getKubernetesVersion().getMinor());
    }

}
