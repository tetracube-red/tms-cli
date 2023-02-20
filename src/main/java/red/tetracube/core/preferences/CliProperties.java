package red.tetracube.core.preferences;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "cli")
public interface CliProperties {

    DatabaseProperties database();

}
