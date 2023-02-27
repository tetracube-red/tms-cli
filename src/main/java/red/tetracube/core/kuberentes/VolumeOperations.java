package red.tetracube.core.kuberentes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class VolumeOperations {

    private final KubernetesClient kubernetesClient;

    private final Logger LOGGER = LoggerFactory.getLogger(VolumeOperations.class);

    public VolumeOperations(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void createPersistentVolume(String installationName,
                                       String applicationName,
                                       String volumeName,
                                       String quantity,
                                       String physicalPath,
                                       String affinityNodeName) {
        var metadata = new ObjectMetaBuilder()
                .withName(volumeName)
                .withLabels(
                        new HashMap<>() {{
                            put("name", applicationName);
                            put("part-of", installationName);
                        }}
                )
                .build();
        var localVolumeSource = new LocalVolumeSourceBuilder()
                .withPath(physicalPath)
                .build();
        var matchExpression = new NodeSelectorRequirementBuilder()
                .withKey("kubernetes.io/hostname")
                .withOperator("In")
                .withValues(affinityNodeName)
                .build();
        var nodeAffinitySelectorTerm = new NodeSelectorTermBuilder()
                .withMatchExpressions(matchExpression)
                .build();
        var nodeAffinitySelector = new NodeSelectorBuilder()
                .withNodeSelectorTerms(nodeAffinitySelectorTerm)
                .build();
        var volumeNodeAffinity = new VolumeNodeAffinityBuilder()
                .withRequired(nodeAffinitySelector)
                .build();
        var persistentVolumeSpec = new PersistentVolumeSpecBuilder()
                .withCapacity(
                        new HashMap<>() {{
                            put("storage", Quantity.parse(quantity));
                        }}
                )
                .withVolumeMode("Filesystem")
                .withAccessModes("ReadWriteOnce")
                .withStorageClassName("local-storage")
                .withLocal(localVolumeSource)
                .withNodeAffinity(volumeNodeAffinity)
                .build();
        var persistentVolume = new PersistentVolumeBuilder()
                .withMetadata(metadata)
                .withSpec(persistentVolumeSpec)
                .build();
        try {
            LOGGER.info("⛏ Creating persistent volume {}", volumeName);
            this.kubernetesClient.persistentVolumes()
                    .resource(persistentVolume)
                    .create();
            LOGGER.info("✔ Persistent volume {} created", volumeName);
        } catch (KubernetesClientException exception) {
            if (exception.getStatus().getCode() == 409) {
                LOGGER.info("⚠ Persistent volume {} already exists", volumeName);
            }
        }
    }

    public void createPersistentVolumeClaim(String namespace,
                                            String installationName,
                                            String applicationName,
                                            String capacity,
                                            String persistentVolumeClaimName) {
        var persistentVolumeClaimMetadata = new ObjectMetaBuilder()
                .withName(persistentVolumeClaimName)
                .withLabels(
                        new HashMap<>() {{
                            put("name", applicationName);
                            put("part-of", installationName);
                        }}
                )
                .build();
        var persistentVolumeClaim = new PersistentVolumeClaimBuilder()
                .withMetadata(persistentVolumeClaimMetadata)
                .withNewSpec()
                .withStorageClassName("local-storage")
                .withAccessModes(List.of("ReadWriteOnce"))
                .withNewResources()
                .withRequests(
                        new HashMap<>() {
                            {
                                put("storage", Quantity.parse(capacity));
                            }
                        }
                )
                .endResources()
                .endSpec()
                .build();
        try {
            LOGGER.info("⛏ Creating volume claim {}", persistentVolumeClaimName);
            this.kubernetesClient.persistentVolumeClaims()
                    .inNamespace(namespace)
                    .resource(persistentVolumeClaim)
                    .create();
            LOGGER.info("✔ Persistent volume claim {} created", persistentVolumeClaimName);
        } catch (KubernetesClientException exception) {
            LOGGER.info("⚠ Persistent volume claim {} already exists", persistentVolumeClaimName);
        }
    }

}
