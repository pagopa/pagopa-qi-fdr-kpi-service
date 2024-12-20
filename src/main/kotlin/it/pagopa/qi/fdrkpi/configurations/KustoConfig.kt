package it.pagopa.qi.fdrkpi.configurations

import com.microsoft.azure.kusto.data.Client
import com.microsoft.azure.kusto.data.ClientFactory
import com.microsoft.azure.kusto.data.auth.ConnectionStringBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KustoConfig {

    fun createKustoClient(
        endpoint: String,
        clientId: String,
        applicationKey: String,
        tenantId: String
    ): Client {
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
        return createKustoClient(endpoint, clientId, applicationKey, tenantId)
    }
}
