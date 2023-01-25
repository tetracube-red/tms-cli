package red.tetracube

import io.quarkus.picocli.runtime.annotations.TopCommand
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import red.tetracube.tms.enums.Operations
import red.tetracube.tms.operations.installation.InstallationOrchestrator
import java.io.File


@TopCommand
@Command(
    name = "tms",
    description = ["Install and maintain TetraCube platform"]
)
class RootTMSCommand(
    private val installationOrchestrator: InstallationOrchestrator
) : Runnable {

    private val logger = LoggerFactory.getLogger(RootTMSCommand::class.java)

    @Option(
        names = ["--config-file", "-c"],
        description = [
            "Set the installation config file in yaml format"
        ],
        required = true
    )
    lateinit var configFile: File

    @Option(
        names = ["--operation"],
        description = [
            "Describe the operation to actuate",
            "Valid values: \${COMPLETION-CANDIDATES}"
        ],
        required = true
    )
    lateinit var operation: Operations

    /*  @Throws(Exception::class)
      override fun run(vararg args: String): Int {
          if (tmsConfigProperties.operationType == "install") {
              this.installationOrchestrator.doInstallation()
          } else if (tmsConfigProperties.operationType == "create guest") {
              this.guestCreateOperation.createUser()
          }
          Quarkus.waitForExit();
          return 0
      }*/

    override fun run() {
        when(this.operation) {
            Operations.INSTALL -> this.installationOrchestrator.doInstallation()
            Operations.CREATE_HOUSE -> TODO()
            Operations.CREATE_GUEST -> TODO()
        }
    }
}