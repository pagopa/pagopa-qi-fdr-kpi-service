package it.pagopa.qi.fdrkpi.dataprovider.kusto.v1

/**
 * Contains all Kusto queries used for KPI calculations.
 *
 * Common patterns used across queries:
 *
 * Null/Zero handling pattern:
 * ```
 * iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI), 0, (VALUE * 100) / TOTALE_FLUSSI)
 * ```
 *
 * This pattern is used to safely handle percentage calculations where TOTALE_FLUSSI is the
 * denominator:
 * - If TOTALE_FLUSSI is 0 → returns 0
 * - If TOTALE_FLUSSI is null → returns 0
 * - If TOTALE_FLUSSI has a valid non-zero value → calculates (VALUE * 100) / TOTALE_FLUSSI
 *
 * Example: For TOTALE_FLUSSI = 100 and VALUE = 5 → returns (5 * 100) / 100 = 5 (meaning 5%)
 */
object KustoQueries {

    fun generateIdFilter(brokerFiscalCode: String?, pspId: String?): String {
        return when {
            pspId != null && brokerFiscalCode != null ->
                "| where ID_PSP == \"$pspId\" and ID_BROKER_PSP == \"$brokerFiscalCode\""
            pspId != null -> "| where ID_PSP == \"$pspId\""
            brokerFiscalCode != null -> "| where ID_BROKER_PSP == \"$brokerFiscalCode\""
            else -> throw RuntimeException("BrokerFiscalCode and PspId are not defined")
        }
    }

    /** Query to test database connection and show available tables */
    val TEST_CONNECTION_QUERY =
        """
        .show tables
    """
            .trimIndent()

    /** Query to calculate Late "Flusso di Rendicontazione" (LFDR) KPI for PSP */
    val LFDR_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        ${'$'}FILTER
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    FDR_IN_RITARDO_F=sum(FDR_IN_RITARDO_FIRST_VERSION),
                    FDR_IN_RITARDO_L=sum(FDR_IN_RITARDO_LAST_VERSION),
                    TOT_COUNT = count()
        | extend PERC_FLUSSI_RITARDO_v1 = iff(TOT_COUNT == 0, -1, iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI), 0, (FDR_IN_RITARDO_F * 100) / TOTALE_FLUSSI))
        | extend PERC_FLUSSI_RITARDO_v2 = iff(TOT_COUNT == 0, -1, iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI), 0, (FDR_IN_RITARDO_L * 100) / TOTALE_FLUSSI))
        | project PERC_FLUSSI_RITARDO_v1, PERC_FLUSSI_RITARDO_v2
    """
            .trimIndent()

    /** Query to calculate Not Received "Flusso di Rendicontazione" (NRFDR) KPI for PSP */
    val NRFDR_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        ${'$'}FILTER
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    FDR_ASSENTI=sum(FLUSSI_ASSENTI),
                    FDR_PRESENTI=sum(FLUSSI_PRESENTI),
                    TOT_COUNT = count()
        by ID_PSP
        | extend PERC_FLUSSI_ASSENTI = iff(TOT_COUNT == 0, -1, iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI), 0, (FDR_ASSENTI * 100) / TOTALE_FLUSSI))
        | project PERC_FLUSSI_ASSENTI
    """
            .trimIndent()

    /**
     * Query to calculate Wrong Payment Number in "Flusso di Rendicontazione" (WPNFDR) KPI for PSP
     */
    val WPNFDR_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        ${'$'}FILTER
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    TOTALE_DIFF_NUM=sum(TOTALE_DIFF_NUM),
                    TOT_COUNT = count()
        | extend PERC_DIFF_NUM = iff(TOT_COUNT == 0, -1, iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI), 0, (TOTALE_DIFF_NUM * 100) / TOTALE_FLUSSI))
        | project PERC_DIFF_NUM
    """
            .trimIndent()

    /** Query to calculate Wrong Amount in "Flusso di Rendicontazione" (WAFDR) KPI for PSP */
    val WAFDR_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        ${'$'}FILTER
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    TOTALE_DIFF_AMOUNT=sum(TOTALE_DIFF_AMOUNT),
                    TOT_COUNT = count()
        | extend PERC_DIFF_AMOUNT = iff(TOT_COUNT == 0, -1, iff(TOTALE_FLUSSI == 0 or isnull(TOTALE_FLUSSI), 0, (TOTALE_DIFF_AMOUNT * 100) / TOTALE_FLUSSI)) 
        | project PERC_DIFF_AMOUNT
    """
            .trimIndent()

    /** Query to calculate the total amount to be transferred */
    val AMOUNT_TO_TRANSFER_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI_DETAILS
        | where DATA_PAGAMENTO between (start .. end)
        | where ID_PSP == "${'$'}PSP"
        | where IS_REND == false
        | summarize TOTALE_IMPORTO=sum(AMOUNT_TRANSFER)
    """
            .trimIndent()

    /** Query to calculate the total number of flows */
    val TOTAL_FLOWS_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        ${'$'}FILTER
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI)
    """
            .trimIndent()
}
