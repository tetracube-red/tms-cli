package red.tetracube;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import red.tetracube.install.InstallCommand;

import java.io.File;

@TopCommand
@Command(
        name = "tms",
        mixinStandardHelpOptions = true,
        subcommands = {InstallCommand.class}
)
public class GreetingCommand implements Runnable {

    @CommandLine.Option(
            names = {"--k8s-config"},
            description = {"locate the kubernetes configuration file"},
            required = true
    )
    private File k8sFile;

    @Override
    public void run() {
    }

}
