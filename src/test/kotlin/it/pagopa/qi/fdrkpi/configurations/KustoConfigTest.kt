package it.pagopa.qi.fdrkpi.configurations

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KustoConfigTest {
    private val kustoConfig = KustoConfig()

    @Test
    fun `createKustoClient should create Kusto Client with correct settings`() {
        val endpoint = "https://test-cluster.kusto.windows.net"
        val clientId = "test-client-id"
        val applicationKey = "test-application-key"
        val tenantId = "test-tenant-id"

        val client = kustoConfig.createKustoClient(endpoint, clientId, applicationKey, tenantId)

        assertThat(client).isNotNull
    }
}
