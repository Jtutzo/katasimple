package com.jtutzo.katasimple

import com.jtutzo.katasimple.provider.DefaultSagaSchema
import com.jtutzo.katasimple.provider.SagaSqlSchemaImpl
import org.axonframework.common.jdbc.ConnectionProvider
import org.axonframework.common.jdbc.PersistenceExceptionResolver
import org.axonframework.common.transaction.TransactionManager
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.eventhandling.tokenstore.jdbc.GenericTokenTableFactory
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore
import org.axonframework.eventhandling.tokenstore.jdbc.TokenSchema
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema
import org.axonframework.eventsourcing.eventstore.jdbc.HsqlEventTableFactory
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore
import org.axonframework.serialization.Serializer
import org.axonframework.spring.config.AxonConfiguration
import org.h2.tools.Server
import org.jooq.conf.RenderNameStyle
import org.jooq.conf.Settings
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
class AxonConfig {

    @Bean
    fun eventStorageEngine(defaultSerializer: Serializer,
                           persistenceExceptionResolver: PersistenceExceptionResolver,
                           serializer: Serializer,
                           configuration: AxonConfiguration,
                           connectionProvider: ConnectionProvider,
                           transactionManager: TransactionManager,
                           eventSchema: EventSchema): EventStorageEngine =
            JdbcEventStorageEngine.builder()
                    .snapshotSerializer(defaultSerializer)
                    .upcasterChain(configuration.upcasterChain())
                    .persistenceExceptionResolver(persistenceExceptionResolver)
                    .eventSerializer(serializer)
                    .connectionProvider(connectionProvider)
                    .transactionManager(transactionManager)
                    .schema(eventSchema)
                    .build()

    @Bean
    fun tokenStore(connectionProvider: ConnectionProvider, serializer: Serializer, tokenSchema: TokenSchema): TokenStore =
            JdbcTokenStore.builder()
                    .connectionProvider(connectionProvider)
                    .serializer(serializer)
                    .schema(tokenSchema)
                    .build()

    @Bean
    fun sagaStore(connectionProvider: ConnectionProvider, serializer: Serializer, sagaSchema: DefaultSagaSchema): JdbcSagaStore =
            JdbcSagaStore.builder()
                    .connectionProvider(connectionProvider)
                    .sqlSchema(SagaSqlSchemaImpl(sagaSchema))
                    .serializer(serializer)
                    .build()

    @Bean
    fun eventSchema(): EventSchema = EventSchema.builder()
            .eventTable("domain_event_entry")
            .globalIndexColumn("global_index")
            .eventIdentifierColumn("event_identifier")
            .metaDataColumn("meta_data")
            .payloadRevisionColumn("payload_revision")
            .payloadTypeColumn("payload_type")
            .timestampColumn("time_stamp")
            .aggregateIdentifierColumn("aggregate_identifier")
            .sequenceNumberColumn("sequence_number")
            .build()

    @Bean
    fun tokenSchema(): TokenSchema = TokenSchema.builder()
            .setTokenTable("token_entry")
            .setProcessorNameColumn("processor_name")
            .setTokenTypeColumn("token_type")
            .build()

    @Bean
    fun sagaSchema(): DefaultSagaSchema = DefaultSagaSchema().apply {
        associationValueEntryTable = "association_value_entry"
        sagaEntryTable = "saga_entry"
        associationKeyColumn = "association_key"
        associationValueColumn = "association_value"
        sagaIdColumn = "saga_id"
        sagaTypeColumn = "saga_type"
        serializedSagaColumn = "serialized_saga"
    }

    @Autowired
    fun createAxonSchema(eventStorageEngine: EventStorageEngine, tokenStore: TokenStore) {
        (eventStorageEngine as JdbcEventStorageEngine).createSchema(HsqlEventTableFactory.INSTANCE)
        (tokenStore as JdbcTokenStore).createSchema(GenericTokenTableFactory())
    }

}

@Configuration
class H2Config {

    private val logger = LoggerFactory.getLogger(H2Config::class.java)

    @Autowired
    fun h2TcpServer(): Server? {
        var server: Server? = null
        try {
            server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start()
        } catch (e: Exception) {
            logger.error("Failed to start h2 tcp server.")
        }
        return server
    }
}

@Configuration
class JooqConfig {

    @Bean
    fun settings(): Settings = Settings().withRenderNameStyle(RenderNameStyle.LOWER)
}

@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
    }
}
