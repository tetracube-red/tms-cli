package red.tetracube.core.preferences;

public interface DatabaseProperties {

    String applicationName();
    String secretName();
    String username();
    String dbName();
    String pgDataPath();
    String configurationName();
    String persistentVolumeName();
    String persistentVolumeClaimName();
    String initDbScripts();

}
