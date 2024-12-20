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
        val query = KustoQueries.LFDR_QUERY
        assertNotNull(query)
        assertTrue(query.contains("KPI_RENDICONTAZIONI"))
        assertTrue(query.contains("\$FILTER"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v1"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v2"))
        assertTrue(query.contains("\$START_DATE"))
        assertTrue(query.contains("\$END_DATE"))
    }

    @Test
    fun `LFDR_BROKER_QUERY should contain broker-specific elements`() {
        val query = KustoQueries.LFDR_QUERY
        assertNotNull(query)
        assertTrue(query.contains("\$FILTER"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v1"))
        assertTrue(query.contains("PERC_FLUSSI_RITARDO_v2"))
    }

    @Test
    fun `NRFDR queries should contain required calculations`() {
        val query = KustoQueries.NRFDR_QUERY

        assertNotNull(query)

        assertTrue(query.contains("PERC_FLUSSI_ASSENTI"))
        assertTrue(query.contains("FDR_ASSENTI=sum(FLUSSI_ASSENTI)"))
        assertTrue(query.contains("\$FILTER"))
    }

    @Test
    fun `WPNFDR queries should calculate payment number differences`() {
        val query = KustoQueries.WPNFDR_QUERY

        assertNotNull(query)
        assertTrue(query.contains("TOTALE_DIFF_NUM=sum(TOTALE_DIFF_NUM)"))
        assertTrue(query.contains("PERC_DIFF_NUM"))
    }

    @Test
    fun `WAFDR queries should calculate amount differences`() {
        val query = KustoQueries.WAFDR_QUERY

        assertNotNull(query)
        assertTrue(query.contains("TOTALE_DIFF_AMOUNT=sum(TOTALE_DIFF_AMOUNT)"))
        assertTrue(query.contains("PERC_DIFF_AMOUNT"))
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
                KustoQueries.LFDR_QUERY,
                KustoQueries.NRFDR_QUERY,
                KustoQueries.WPNFDR_QUERY,
                KustoQueries.WAFDR_QUERY,
                KustoQueries.AMOUNT_TO_TRANSFER_QUERY,
                KustoQueries.TOTAL_FLOWS_QUERY
            )

        queries.forEach { query ->
            assertNotNull(query)
            assertEquals(query, query.trimIndent(), "Query should be properly trimmed")
            assertTrue(query.isNotBlank(), "Query should not be blank")
        }
    }

    @Test
    fun `All percentage queries should handle null and zero values`() {
        val percentageQueries =
            listOf(
                KustoQueries.LFDR_QUERY to
                    listOf("PERC_FLUSSI_RITARDO_v1", "PERC_FLUSSI_RITARDO_v2"),
                KustoQueries.NRFDR_QUERY to listOf("PERC_FLUSSI_ASSENTI"),
                KustoQueries.WPNFDR_QUERY to listOf("PERC_DIFF_NUM"),
                KustoQueries.WAFDR_QUERY to listOf("PERC_DIFF_AMOUNT")
            )

        percentageQueries.forEach { (query, percentageFields) ->
            assertNotNull(query)

            assertTrue(
                query.contains("TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI)"),
                "Query should handle null and zero values for TOTALE_FLUSSI"
            )

            percentageFields.forEach { field ->
                assertTrue(
                    query.contains("extend $field = iff("),
                    "Percentage field $field should use iff for null/zero handling"
                )
            }
        }
    }

    @Test
    fun `Non-percentage queries should not contain null zero handling`() {
        val nonPercentageQueries =
            listOf(
                KustoQueries.TEST_CONNECTION_QUERY,
                KustoQueries.AMOUNT_TO_TRANSFER_QUERY,
                KustoQueries.TOTAL_FLOWS_QUERY
            )

        nonPercentageQueries.forEach { query ->
            assertNotNull(query)
            assertTrue(
                !query.contains("iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI))"),
                "Non-percentage queries should not contain null/zero handling"
            )
        }
    }
}
