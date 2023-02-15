package red.tetracube;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import red.tetracube.install.InstallCommand;

@TopCommand
@Command(
        name = "tms",
        mixinStandardHelpOptions = true,
        subcommands = {InstallCommand.class}
)
public class TmsCommand implements Runnable {

    @Override
    public void run() {
    }

}
