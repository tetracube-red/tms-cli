package red.tetracube.install;

import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@CommandLine.Command(
        name = "install",
        mixinStandardHelpOptions = true
)
public class InstallCommand implements Runnable {

    @CommandLine.Option(
            names = {"--k8s-config"},
            description = {"locate the kubernetes configuration file"},
            required = true
    )
    private File k8sFile;

    @CommandLine.Option(
            names = {"--hostname"},
            description = {"define the platform's hostname"},
            required = true
    )
    private String hostname;

    @CommandLine.Option(
            names = {"--installation-name"},
            description = {"define the platform installation name"},
            required = true
    )
    private String installationName;

    @CommandLine.Option(
            names = {"--db-password"},
            description = {"define the name of database"},
            required = false
    )
    private String dbPassword;

    @CommandLine.Option(
            names = {"--cert-files"},
            description = {"specifies key and certificate files separated by comma"},
            required = true,
            split = ","
    )
    private List<File> certificates;

    @Inject
    InstallationCoordinatorService installationCoordinatorService;

    @Override
    public void run() {
        this.installationCoordinatorService.startInstallation();
    }
}
