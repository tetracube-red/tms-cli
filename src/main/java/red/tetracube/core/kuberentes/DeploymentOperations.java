package red.tetracube.core.kuberentes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class DeploymentOperations {

    private final KubernetesClient kubernetesClient;

    private final static Logger LOGGER = LoggerFactory.getLogger(DeploymentOperations.class);

    public DeploymentOperations(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void createApplicationDeployment(String namespace,
                                            String installationName,
                                            String applicationName,
                                            List<EnvVar> envVarList,
                                            List<VolumeMount> volumeMounts,
                                            List<Volume> volumes) {
        var metadata = new ObjectMetaBuilder()
                .withName(applicationName)
                .withNamespace(namespace)
                .withLabels(
                        new HashMap<>() {{
                            put("part-of", installationName);
                        }}
                )
                .build();
        var labelSelector = new LabelSelectorBuilder()
                .withMatchLabels(
                        new HashMap<>() {{
                            put("name", applicationName);
                            put("part-of", installationName);
                        }}
                )
                .build();
        var dbContainerPort = new ContainerPortBuilder()
                .withName("tcp-port")
                .withContainerPort(5432)
                .build();
        var dbContainer = new ContainerBuilder()
                .withName(applicationName)
                .withImage("postgres:alpine")
                .withImagePullPolicy("IfNotPresent")
                .withPorts(dbContainerPort)
                .withEnv(envVarList)
                .withVolumeMounts(volumeMounts)
                .build();
        var containers = Collections.singletonList(dbContainer);
        var podSpec = new PodSpecBuilder()
                .withContainers(containers)
                .withVolumes(volumes)
                .build();
        var template = new PodTemplateSpecBuilder()
                .withMetadata(metadata)
                .withSpec(podSpec)
                .build();
        var deploymentSpec = new DeploymentSpecBuilder()
                .withReplicas(1)
                .withSelector(labelSelector)
                .withTemplate(template)
                .build();
        var deployment = new DeploymentBuilder()
                .withMetadata(metadata)
                .withSpec(deploymentSpec)
                .build();
        try {
            LOGGER.info("🏗 Creating deployment");
            kubernetesClient.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .resource(deployment)
                    .create();
            LOGGER.info("✔ Deployment created");
        } catch (KubernetesClientException kubernetesClientException) {
            if (kubernetesClientException.getCode() == 409) {
                LOGGER.info("⚠ Deployment already exists, skipping creation");
            }
        }
    }
}
