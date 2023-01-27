package red.tetracube

import io.quarkus.picocli.runtime.annotations.TopCommand
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import red.tetracube.tms.enums.Operations
import red.tetracube.tms.operations.houses.create.HouseCreateOperation
import red.tetracube.tms.operations.installation.InstallationOrchestrator
import java.io.File


@TopCommand
@Command(
    name = "tms",
    description = ["Install and maintain TetraCube platform"]
)
class RootTMSCommand(
    private val installationOrchestrator: InstallationOrchestrator,
    private val houseCreateOperation: HouseCreateOperation
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

    @Option(
        names = ["--house-name"],
        description = [
            "Specifies the name the house to create or",
            "to use as house parent name for the new users"
        ],
        required = false
    )
    lateinit var houseName: String

    override fun run() {
        when(this.operation) {
            Operations.INSTALL -> this.installationOrchestrator.doInstallation()
            Operations.CREATE_HOUSE -> this.houseCreateOperation.createHouse(houseName)
            Operations.CREATE_GUEST -> TODO()
        }
    }
}