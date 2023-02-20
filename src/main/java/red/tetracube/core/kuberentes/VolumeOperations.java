package red.tetracube.core.kuberentes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;

@ApplicationScoped
public class VolumeOperations {

    private final KubernetesClient kubernetesClient;

    public VolumeOperations(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void createPersistentVolume(String namespace,
                                       String installationName,
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
            this.kubernetesClient.
        }

    }

}
