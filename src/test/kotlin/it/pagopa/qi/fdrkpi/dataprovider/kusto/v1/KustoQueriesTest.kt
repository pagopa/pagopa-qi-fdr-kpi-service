package it.pagopa.qi.fdrkpi.dataprovider.kusto.v1

import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KustoQueriesTest {

    @Test
    fun `TEST_CONNECTION_QUERY should contain show tables command`() {
        val query = KustoQueries.TEST_CONNECTION_QUERY
        assertNotNull(query)
        assertTrue(query.contains(".show tables"))
        assertTrue(query.trim().lines().size == 1)
    }

    @Test
    fun `LFDR_PSP_QUERY should contain required elements`() {
        val query = KustoQueries.LFDR_PSP_QUERY
        assertNotNull(query)
        assertTrue(query.contains("KPI_RENDICONTAZIONI"))
        assertTrue(query.contains("ID_PSP == \"\$PSP\""))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v1"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v2"))
        assertTrue(query.contains("\$START_DATE"))
        assertTrue(query.contains("\$END_DATE"))
    }

    @Test
    fun `LFDR_BROKER_QUERY should contain broker-specific elements`() {
        val query = KustoQueries.LFDR_BROKER_QUERY
        assertNotNull(query)
        assertTrue(query.contains("ID_BROKER_PSP == \$Int_psp"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v1"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v2"))
    }

    @Test
    fun `NRFDR queries should contain required calculations`() {
        val pspQuery = KustoQueries.NRFDR_PSP_QUERY
        val brokerQuery = KustoQueries.NRFDR_BROKER_QUERY

        assertNotNull(pspQuery)
        assertNotNull(brokerQuery)

        assertTrue(pspQuery.contains("PERC_FLUSSI_ASSENTI"))
        assertTrue(pspQuery.contains("FDR_ASSENTI=sum(FLUSSI_ASSENTI)"))
        assertTrue(pspQuery.contains("ID_PSP == \"\$PSP\""))

        assertTrue(brokerQuery.contains("PERC_FLUSSI_ASSENTI"))
        assertTrue(brokerQuery.contains("FDR_ASSENTI=sum(FLUSSI_ASSENTI)"))
        assertTrue(brokerQuery.contains("ID_BROKER_PSP == \$Int_psp"))
    }

    @Test
    fun `WPNFDR queries should calculate payment number differences`() {
        val pspQuery = KustoQueries.WPNFDR_PSP_QUERY
        val brokerQuery = KustoQueries.WPNFDR_BROKER_QUERY

        assertNotNull(pspQuery)
        assertNotNull(brokerQuery)

        listOf(pspQuery, brokerQuery).forEach { query ->
            assertTrue(query.contains("TOTALE_DIFF_NUM=sum(TOTALE_DIFF_NUM)"))
            assertTrue(query.contains("PERC_DIFF_NUM"))
        }
    }

    @Test
    fun `WAFDR queries should calculate amount differences`() {
        val pspQuery = KustoQueries.WAFDR_PSP_QUERY
        val brokerQuery = KustoQueries.WAFDR_BROKER_QUERY

        assertNotNull(pspQuery)
        assertNotNull(brokerQuery)

        listOf(pspQuery, brokerQuery).forEach { query ->
            assertTrue(query.contains("TOTALE_DIFF_AMOUNT=sum(TOTALE_DIFF_AMOUNT)"))
            assertTrue(query.contains("PERC_DIFF_AMOUNT"))
        }
    }

    @Test
    fun `AMOUNT_TO_TRANSFER_QUERY should contain required filters`() {
        val query = KustoQueries.AMOUNT_TO_TRANSFER_QUERY
        assertNotNull(query)
        assertTrue(query.contains("KPI_RENDICONTAZIONI_DETAILS"))
        assertTrue(query.contains("IS_REND == false"))
        assertTrue(query.contains("TOTALE_IMPORTO=sum(AMOUNT_TRANSFER)"))
    }

    @Test
    fun `TOTAL_FLOWS_QUERY should calculate total flows`() {
        val query = KustoQueries.TOTAL_FLOWS_QUERY
        assertNotNull(query)
        assertTrue(query.contains("TOTALE_FLUSSI=sum(TOTALE_FLUSSI)"))
    }

    @Test
    fun `All queries should be properly trimmed`() {
        val queries =
            listOf(
                KustoQueries.TEST_CONNECTION_QUERY,
                KustoQueries.LFDR_PSP_QUERY,
                KustoQueries.LFDR_BROKER_QUERY,
                KustoQueries.NRFDR_PSP_QUERY,
                KustoQueries.NRFDR_BROKER_QUERY,
                KustoQueries.WPNFDR_PSP_QUERY,
                KustoQueries.WPNFDR_BROKER_QUERY,
                KustoQueries.WAFDR_PSP_QUERY,
                KustoQueries.WAFDR_BROKER_QUERY,
                KustoQueries.AMOUNT_TO_TRANSFER_QUERY,
                KustoQueries.TOTAL_FLOWS_QUERY
            )

        queries.forEach { query ->
            assertNotNull(query)
            assertEquals(query, query.trimIndent(), "Query should be properly trimmed")
            assertTrue(query.isNotBlank(), "Query should not be blank")
        }
    }
}
