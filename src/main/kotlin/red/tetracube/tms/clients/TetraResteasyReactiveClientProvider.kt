package red.tetracube.tms.clients

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.arc.Arc
import io.quarkus.arc.InstanceHandle
import io.quarkus.rest.client.reactive.jackson.runtime.serialisers.ClientJacksonMessageBodyWriter
import org.jboss.resteasy.reactive.client.impl.ClientBuilderImpl
import org.jboss.resteasy.reactive.client.impl.WebTargetImpl
import org.jboss.resteasy.reactive.server.jackson.JacksonBasicMessageBodyReader
import org.keycloak.admin.client.spi.ResteasyClientProvider
import javax.net.ssl.SSLContext
import javax.ws.rs.client.Client
import javax.ws.rs.client.WebTarget

class TetraResteasyReactiveClientProvider : ResteasyClientProvider {

    override fun newRestEasyClient(messageHandler: Any?, sslContext: SSLContext?, disableTrustManager: Boolean): Client? {
        val clientBuilder = ClientBuilderImpl()
        return registerJacksonProviders(clientBuilder)
                .verifyHost(false)
                .trustAll(true)
                .build()
    }

    // this code is much more complicated than expected because it needs to handle various permutations
    // where beans may or may not exist
    private fun registerJacksonProviders(clientBuilder: ClientBuilderImpl): ClientBuilderImpl {
        var clientBuilder = clientBuilder
        val arcContainer = Arc.container()
        checkNotNull(arcContainer) { this.javaClass.name + " should only be used in a Quarkus application" }
        val objectMapperInstance = arcContainer.instance(ObjectMapper::class.java)
        var objectMapper: ObjectMapper? = null
        val readerInstance = arcContainer
                .instance(JacksonBasicMessageBodyReader::class.java)
        if (readerInstance.isAvailable) {
            clientBuilder = clientBuilder.register(readerInstance.get())
        } else {
            objectMapper = getObjectMapper(objectMapper, objectMapperInstance)
            clientBuilder = clientBuilder.register(JacksonBasicMessageBodyReader(objectMapper))
        }
        val writerInstance = arcContainer
                .instance(ClientJacksonMessageBodyWriter::class.java)
        if (writerInstance.isAvailable) {
            clientBuilder = clientBuilder.register(writerInstance.get())
        } else {
            objectMapper = getObjectMapper(objectMapper, objectMapperInstance)
            clientBuilder = clientBuilder.register(ClientJacksonMessageBodyWriter(objectMapper))
        }
        return clientBuilder
    }

    // the whole idea here is to reuse the ObjectMapper instance
    private fun getObjectMapper(value: ObjectMapper?,
                                objectMapperInstance: InstanceHandle<ObjectMapper>): ObjectMapper? {
        return value
                ?: if (objectMapperInstance.isAvailable)
                    objectMapperInstance.get()
                else
                    ObjectMapper()
    }

    override fun <R> targetProxy(target: WebTarget, targetClass: Class<R>?): R {
        return (target as WebTargetImpl).proxy(targetClass)
    }
}