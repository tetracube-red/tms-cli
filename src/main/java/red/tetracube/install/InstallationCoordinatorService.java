package red.tetracube.install;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InstallationCoordinatorService {

    @Inject
    InstallOptions installOptions;

    private final Logger LOGGER = LoggerFactory.getLogger(InstallationCoordinatorService.class);

    public void startInstallation() {
        LOGGER.info("Starting installation for {}", installOptions.getInstallationName());
    }

}
