package red.tetracube.tms.operations.guests.create

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.jboss.resteasy.spi.WriterException
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import red.tetracube.tms.clients.TetraResteasyReactiveClientProvider
import red.tetracube.tms.properties.TMSConfigProperties
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import kotlin.io.path.Path
import kotlin.math.log


@ApplicationScoped
class GuestCreateOperation(
    private val tmsConfigProperties: TMSConfigProperties,
    private val objectMapper: ObjectMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(GuestCreateOperation::class.java)

    private lateinit var keycloak: Keycloak

    @PostConstruct
    fun initKeycloak() {
        val resteasyClient = TetraResteasyReactiveClientProvider()
            .newRestEasyClient(null, null, true)
        keycloak = KeycloakBuilder.builder()
            .serverUrl(tmsConfigProperties.gatekeeperBasePath())
            .realm("master")
            .username(tmsConfigProperties.gatekeeperAdminUsername)
            .password(tmsConfigProperties.gatekeeperPassword)
            .clientId("admin-cli")
            .resteasyClient(resteasyClient)
            .build()
    }

    fun createUser() {
        val userCredential = CredentialRepresentation()
        userCredential.isTemporary = false
        userCredential.type = CredentialRepresentation.PASSWORD
        userCredential.value = UUID.randomUUID().toString()
            .replace("\\-", "")
            .substring(2, 10)

        val user = UserRepresentation()
        user.username = tmsConfigProperties.guestName
        user.isEnabled = true
        user.isEmailVerified = true

        logger.info("Creating guest into keycloak")
        val response = keycloak.realm(tmsConfigProperties.namespaceName())
            .users()
            .create(user)
        if (response.status != 201 && response.status != 409) {
            logger.warn("Operation failed: {} -> {}", response.status, response.entity)
            return
        }

        logger.info("Getting just created guest's id")
        val userId = keycloak.realm(tmsConfigProperties.namespaceName())
            .users()
            .search(user.username)
            .first()
            .id

        logger.info("Setting guest's credentials")
        keycloak.realm(tmsConfigProperties.namespaceName())
            .users()[userId]
            .resetPassword(userCredential)

        generateQRData(userCredential.value)
    }

    fun generateQRData(userCredentials: String) {
        logger.info("Building QRCode data")
        val outDataCollector = emptyMap<String, String>().toMutableMap()
        outDataCollector["gatekeeper_base_path"] = "https://${tmsConfigProperties.gatekeeperHostname()}"
        outDataCollector["gatekeeper_realm"] = tmsConfigProperties.namespaceName()
        outDataCollector["client_id"] = tmsConfigProperties.mobileClientId
        outDataCollector["username"] = tmsConfigProperties.guestName
        outDataCollector["credentials"] = userCredentials

        logger.info("Retrieving client secrets")
        val secret = keycloak.realm(tmsConfigProperties.namespaceName())
            .clients()
            .findByClientId(tmsConfigProperties.mobileClientId)
            .first()
            .secret
        outDataCollector["client_secret"] = secret
        createQR(outDataCollector)
        runBrowser()
    }

    @Throws(WriterException::class, IOException::class)
    fun createQR(dataMap: Map<String, String>) {
        logger.info("Generating QR Code")
        val data = objectMapper.writeValueAsString(dataMap)
        val path = File(
            File(
                GuestCreateOperation::class.java.classLoader
                    .getResource("META-INF/resources/static")!!
                    .path
            )
                .path + "/qr_guest_info.png"
        ).path
        val charset = "UTF-8"
        val hashMap: MutableMap<EncodeHintType?, ErrorCorrectionLevel?> =
            hashMapOf<EncodeHintType?, ErrorCorrectionLevel?>().toMutableMap()
        hashMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L

        val matrix = MultiFormatWriter()
            .encode(
                String(data.toByteArray(charset(charset))),
                BarcodeFormat.QR_CODE,
                500,
                500
            )

        logger.info("Writing image file")
        MatrixToImageWriter.writeToPath(
            matrix,
            path.substring(path.lastIndexOf('.') + 1),
            Path(path)
        )
    }

    private fun runBrowser() {
        logger.info("Searching for web browser in your operating system")
        val osRuntime = Runtime.getRuntime()
        val url = "http://localhost:9090/index.html"
        val browsers = arrayOf(
            "chrome",
            "google-chrome",
            "firefox",
            "firefox-bin",
            "mozilla",
            "epiphany",
            "konqueror",
            "netscape",
            "opera",
            "links",
            "lynx"
        )

        val cmd = StringBuffer()
        for (i in browsers.indices) if (i == 0) cmd.append(
            String.format(
                "%s \"%s\"",
                browsers[i],
                url
            )
        ) else cmd.append(
            String.format(
                " || %s \"%s\"",
                browsers[i], url
            )
        )
        logger.info("Determining which operating system is running")
        logger.info("Launching {} on {}", cmd.toString(), System.getProperty("os.name"))
        if (System.getProperty("os.name").lowercase().startsWith("windows")) {
            osRuntime.exec("rundll32 SHELL32.DLL,ShellExec_RunDLL $url")
        } else {
            osRuntime.exec(arrayOf("sh", "-c", cmd.toString()))
        }
    }
}