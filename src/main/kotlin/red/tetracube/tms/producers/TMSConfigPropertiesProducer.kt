package red.tetracube.tms.producers;

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import picocli.CommandLine.ParseResult
import red.tetracube.tms.properties.TMSConfiguration
import java.io.File
import java.io.FileInputStream
import javax.inject.Singleton
import javax.ws.rs.Produces


@Singleton
class TMSConfigPropertiesProducer {

    private val logger = LoggerFactory.getLogger(TMSConfigPropertiesProducer::class.java)

    @Produces
    @Singleton
    fun tmsCliConfiguration(parseResult: ParseResult): TMSConfiguration {
        val configFile = parseResult.matchedOption("c").getValue<File>()
        logger.info("Getting yaml configuration file")
        val yamlInputStream = FileInputStream(configFile)
        val yaml = Yaml()
        return yaml.loadAs(yamlInputStream, TMSConfiguration::class.java)
    }
}
