package red.tetracube.install;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class InstallOptions {

    private File k8sFile;
    private String hostname;
    private String installationName;
    private String dbPassword;
    private List<File> certFiles;

    private final Logger LOGGER = LoggerFactory.getLogger(InstallOptions.class);

    public InstallOptions(CommandLine.ParseResult parseResult) {
        var commandParseResult = parseResult.subcommand();
        Optional.ofNullable(commandParseResult.matchedOption("k8s-config"))
                .ifPresent(k8sConfig -> {
                    this.k8sFile = k8sConfig.getValue();
                    LOGGER.info("K8s config -> {}", this.k8sFile.getName());
                });
        Optional.ofNullable(commandParseResult.matchedOption("hostname"))
                .ifPresent(hostname -> {
                    this.hostname = hostname.getValue();
                    LOGGER.info("Hostname config -> {}", this.hostname);
                });
        Optional.ofNullable(commandParseResult.matchedOption("installation-name"))
                .ifPresent(installationName -> {
                    this.installationName = installationName.getValue();
                    LOGGER.info("Installation name config -> {}", this.installationName);
                });
        Optional.ofNullable(commandParseResult.matchedOption("db-password"))
                .ifPresentOrElse(
                        dbPassword -> {
                            this.dbPassword = dbPassword.getValue();
                            LOGGER.info("DB password config -> {}", this.dbPassword);
                        },
                        () -> {
                            this.dbPassword = UUID.randomUUID().toString().replace("-", "");
                            LOGGER.info("DB password config -> {}", this.dbPassword);
                        }
                );
        Optional.ofNullable(commandParseResult.matchedOption("cert-files"))
                .ifPresent(certFiles -> {
                    this.certFiles = certFiles.getValue();
                    var files = this.certFiles.stream().map(File::getName).collect(Collectors.joining(", "));
                    LOGGER.info("Cert files config: {}", files);
                });
    }

    public File getK8sFile() {
        return k8sFile;
    }

    public String getHostname() {
        return hostname;
    }

    public String getInstallationName() {
        return installationName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public List<File> getCertFiles() {
        return certFiles;
    }
}
