package red.tetracube.install;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.tetracube.core.kuberentes.ConfigurationOperations;
import red.tetracube.core.kuberentes.NamespaceOperations;
import red.tetracube.core.kuberentes.VolumeOperations;
import red.tetracube.core.preferences.CliProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

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
    VolumeOperations volumeOperations;

    @Inject
    CliProperties cliProperties;

    private final Logger LOGGER = LoggerFactory.getLogger(InstallationCoordinatorService.class);

    public void startInstallation() {
        LOGGER.info("Starting installation for {}", installOptions.getInstallationName());
        LOGGER.info("Kubernetes version {}.{}", kubernetesClient.getKubernetesVersion().getMajor(), kubernetesClient.getKubernetesVersion().getMinor());
        createNamespace();
        createSecrets();
        createConfigMaps();
        createPersistentVolume();
    }

    public void createNamespace() {
        LOGGER.info("Creating namespace");
        namespaceOperations.createNamespace(installOptions.installationNameSlug(), installOptions.installationNameSlug());
    }

    public void createSecrets() {
        LOGGER.info("Creating database secrets");
        var secretsData = new HashMap<String, String>() {{
            put("postgres-user", cliProperties.database().username());
            put("postgres-password", installOptions.getDbPassword());
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
            put("postgres-db", cliProperties.database().dbName());
            put("pgdata", cliProperties.database().pgDataPath());
        }};
        configurationOperations.createConfigMap(
                installOptions.installationNameSlug(),
                installOptions.installationNameSlug(),
                cliProperties.database().applicationName(),
                cliProperties.database().configurationName(),
                configMapData
        );

        var initDbConfigMapData = new HashMap<String, String>();
        var sqlResources = InstallationCoordinatorService.class.getResource("/sql");
        var resourcesPath = Optional.ofNullable(sqlResources)
                .map(sqlResourcesPath -> {
                    try (var walk = Files.walk(Path.of(sqlResourcesPath.getPath()))) {
                        return walk
                                .filter(p -> !Files.isDirectory(p))
                                .map(p -> p.toString().toLowerCase())
                                .filter(f -> f.endsWith("sql"))
                                .collect(Collectors.toList());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow();

        resourcesPath.forEach(sqlScript -> {
            var sqlScriptFile = new File(sqlScript);
            try (FileInputStream fileStream = new FileInputStream(sqlScriptFile)) {
                initDbConfigMapData.put(sqlScriptFile.getName(), new String(fileStream.readAllBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        configurationOperations.createConfigMap(
                installOptions.installationNameSlug(),
                installOptions.installationNameSlug(),
                cliProperties.database().applicationName(),
                cliProperties.database().initDbScripts(),
                initDbConfigMapData
        );
    }

    public void createPersistentVolume() {
        LOGGER.info("Creating database persistent volumes");
        volumeOperations.createPersistentVolume(
                installOptions.installationNameSlug(),
                cliProperties.database().applicationName(),
                cliProperties.database().persistentVolumeName(),
                "5Gi",
                installOptions.getDbPersistentPath(),
                installOptions.getDbPersistentPathNodeName()
        );
        volumeOperations.createPersistentVolumeClaim(
                installOptions.installationNameSlug(),
                installOptions.installationNameSlug(),
                cliProperties.database().applicationName(),
                "5Gi",
                cliProperties.database().persistentVolumeClaimName()
        );
    }

}
