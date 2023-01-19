package red.tetracube

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import red.tetracube.tms.operations.guests.create.GuestCreateOperation
import red.tetracube.tms.operations.installation.InstallationOrchestrator
import red.tetracube.tms.properties.TMSConfigProperties

@QuarkusMain
class RootTMSCommand(
    private val installationOrchestrator: InstallationOrchestrator,
    private val guestCreateOperation: GuestCreateOperation,
    private val tmsConfigProperties: TMSConfigProperties
) : QuarkusApplication {

    @Throws(Exception::class)
    override fun run(vararg args: String): Int {
        if (tmsConfigProperties.operationType == "install") {
            this.installationOrchestrator.doInstallation()
        } else if (tmsConfigProperties.operationType == "create guest") {
            this.guestCreateOperation.createUser()
        }
        Quarkus.waitForExit();
        return 0
    }
}