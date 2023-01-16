package red.tetracube

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import red.tetracube.tms.operations.installation.InstallationOrchestrator
import red.tetracube.tms.properties.TMSConfigProperties

@QuarkusMain
class RootTMSCommand(
    private val installationOrchestrator: InstallationOrchestrator,
    private val tmsConfigProperties: TMSConfigProperties
) : QuarkusApplication {

    @Throws(Exception::class)
    override fun run(vararg args: String): Int {
        if (tmsConfigProperties.operationType == "install") {
            this.installationOrchestrator.doInstallation()
        }
        return 0
    }
}