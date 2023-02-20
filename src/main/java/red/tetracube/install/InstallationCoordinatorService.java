package red.tetracube.install;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.tetracube.core.kuberentes.ConfigurationOperations;
import red.tetracube.core.kuberentes.NamespaceOperations;
import red.tetracube.core.preferences.CliProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;

@ApplicationScoped
public class InstallationCoordinatorService {

    @Inject
    InstallOptions installOptions;

    @Inject
    KubernetesClient kubernetesClient;

    @Inject
    NamespaceOperations namespaceOperations;

    @Inject
    ConfigurationOperations configurationOperations;

    @Inject
    CliProperties cliProperties;

    private final Logger LOGGER = LoggerFactory.getLogger(InstallationCoordinatorService.class);

    public void startInstallation() {
        LOGGER.info("Starting installation for {}", installOptions.getInstallationName());
        LOGGER.info("Kubernetes version {}.{}", kubernetesClient.getKubernetesVersion().getMajor(), kubernetesClient.getKubernetesVersion().getMinor());
        createNamespace();
        createSecrets();
        createConfigMaps();
    }

    public void createNamespace() {
        LOGGER.info("Creating namespace");
        namespaceOperations.createNamespace(installOptions.installationNameSlug(), installOptions.installationNameSlug());
    }

    public void createSecrets() {
        LOGGER.info("Creating database secrets");
        var secretsData = new HashMap<String, String>() {{
           put ("postgres-user", cliProperties.database().username());
           put ("postgres-password", installOptions.getDbPassword());
        }};
        configurationOperations.createSecret(
                installOptions.installationNameSlug(),
                installOptions.installationNameSlug(),
                cliProperties.database().applicationName(),
                cliProperties.database().secretName(),
                secretsData
        );
    }

    public void createConfigMaps() {
        LOGGER.info("Creating database config maps");
        var configMapData = new HashMap<String, String>() {{
            put ("postgres-db", cliProperties.database().dbName());
            put ("pgdata", cliProperties.database().pgDataPath());
        }};
        configurationOperations.createConfigMap(
                installOptions.installationNameSlug(),
                installOptions.installationNameSlug(),
                cliProperties.database().applicationName(),
                cliProperties.database().configurationName(),
                configMapData
        );
    }

}
