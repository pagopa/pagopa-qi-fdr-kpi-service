package it.pagopa.qi.fdrkpi.configurations

import com.microsoft.azure.kusto.data.Client
import com.microsoft.azure.kusto.data.ClientFactory
import com.microsoft.azure.kusto.data.auth.ConnectionStringBuilder
import com.microsoft.azure.kusto.data.auth.endpoints.KustoTrustedEndpoints
import com.microsoft.azure.kusto.data.auth.endpoints.MatchRule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KustoConfig {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createKustoClient(
        endpoint: String,
        clientId: String,
        applicationKey: String,
        tenantId: String
    ): Client {
        logger.info(
            "Creating Kusto client for endpoint [{}] with tenant ID [{}]",
            endpoint,
            tenantId
        )

        /*
           Adds a new trusted host for local development only.
           The host set in the envs that matches it should start with the substring "http://localhost",
           to avoid authorization/token checks
        */
        KustoTrustedEndpoints.addTrustedHosts(
            listOf(MatchRule("localhost-data-explorer", true)),
            false
        )

        val connectionStringBuilder =
            ConnectionStringBuilder.createWithAadApplicationCredentials(
                endpoint,
                clientId,
                applicationKey,
                tenantId
            )
        return ClientFactory.createClient(connectionStringBuilder)
    }

    @Bean(name = ["reKustoClient"])
    fun reKustoClient(
        @Value("\${azuredataexplorer.re.endpoint}") endpoint: String,
        @Value("\${azuredataexplorer.re.clientId}") clientId: String,
        @Value("\${azuredataexplorer.re.applicationKey}") applicationKey: String,
        @Value("\${azuredataexplorer.re.applicationTenantId}") tenantId: String
    ): Client {
        logger.info("Initializing RE Kusto client")
        return createKustoClient(endpoint, clientId, applicationKey, tenantId)
    }
}
