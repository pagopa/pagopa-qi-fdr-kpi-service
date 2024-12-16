package it.pagopa.qi.fdrkpi.dataprovider.v1.kusto

object KustoQueries {

    /** Query to test database connection and show available tables */
    val TEST_CONNECTION_QUERY =
        """
        .show tables
    """
            .trimIndent()

    /** Query to calculate Late "Flusso di Rendicontazione" (LFDR) KPI for PSP */
    val LFDR_PSP_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_PSP == "${'$'}PSP"
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    FDR_IN_RITARDO_F=sum(FDR_IN_RITARDO_FIRST_VERSION),
                    FDR_IN_RITARDO_L=sum(FDR_IN_RITARDO_LAST_VERSION)
        | extend PERC_FLUSSI_RITARDO_v1=(FDR_IN_RITARDO_F * 100) / TOTALE_FLUSSI
        | extend PERC_FLUSSI_RITARDO_v2=(FDR_IN_RITARDO_L * 100) / TOTALE_FLUSSI
        | project PERC_FLUSSI_RITARDO_v1, PERC_FLUSSI_RITARDO_v2
    """
            .trimIndent()

    /** Query to calculate Late "Flusso di Rendicontazione" (LFDR) KPI for BROKER PSP */
    val LFDR_BROKER_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_BROKER_PSP == ${'$'}Int_psp
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    FDR_IN_RITARDO_F=sum(FDR_IN_RITARDO_FIRST_VERSION),
                    FDR_IN_RITARDO_L=sum(FDR_IN_RITARDO_LAST_VERSION)
        | extend PERC_FLUSSI_RITARDO_v1=(FDR_IN_RITARDO_F * 100) / TOTALE_FLUSSI
        | extend PERC_FLUSSI_RITARDO_v2=(FDR_IN_RITARDO_L * 100) / TOTALE_FLUSSI
        | project PERC_FLUSSI_RITARDO_v1, PERC_FLUSSI_RITARDO_v2
    """
            .trimIndent()

    /** Query to calculate Not Received "Flusso di Rendicontazione" (NRFDR) KPI for PSP */
    val NRFDR_PSP_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_PSP == "${'$'}PSP"
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    FDR_ASSENTI=sum(FLUSSI_ASSENTI),
                    FDR_PRESENTI=sum(FLUSSI_PRESENTI)
        by ID_PSP
        | extend PERC_FLUSSI_ASSENTI=(FDR_ASSENTI * 100) / TOTALE_FLUSSI
        | project PERC_FLUSSI_ASSENTI
    """
            .trimIndent()

    /** Query to calculate Not Received "Flusso di Rendicontazione" (NRFDR) KPI for BROKER PSP */
    val NRFDR_BROKER_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_BROKER_PSP == ${'$'}Int_psp
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    FDR_ASSENTI=sum(FLUSSI_ASSENTI),
                    FDR_PRESENTI=sum(FLUSSI_PRESENTI)
        by ID_PSP
        | extend PERC_FLUSSI_ASSENTI=(FDR_ASSENTI * 100) / TOTALE_FLUSSI
        | project PERC_FLUSSI_ASSENTI
    """
            .trimIndent()

    /**
     * Query to calculate Wrong Payment Number in "Flusso di Rendicontazione" (WPNFDR) KPI for PSP
     */
    val WPNFDR_PSP_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_PSP == "${'$'}PSP"
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    TOTALE_DIFF_NUM=sum(TOTALE_DIFF_NUM)
        | extend PERC_DIFF_NUM=(TOTALE_DIFF_NUM * 100) / TOTALE_FLUSSI
        | project PERC_DIFF_NUM
    """
            .trimIndent()

    /**
     * Query to calculate Wrong Payment Number in "Flusso di Rendicontazione" (WPNFDR) KPI for
     * BROKER PSP
     */
    val WPNFDR_BROKER_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_BROKER_PSP == ${'$'}Int_psp
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    TOTALE_DIFF_NUM=sum(TOTALE_DIFF_NUM)
        | extend PERC_DIFF_NUM=(TOTALE_DIFF_NUM * 100) / TOTALE_FLUSSI
        | project PERC_DIFF_NUM
    """
            .trimIndent()

    /** Query to calculate Wrong Amount in "Flusso di Rendicontazione" (WAFDR) KPI for PSP */
    val WAFDR_PSP_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_PSP == "${'$'}PSP"
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    TOTALE_DIFF_AMOUNT=sum(TOTALE_DIFF_AMOUNT)
        | extend PERC_DIFF_AMOUNT=(TOTALE_DIFF_AMOUNT * 100) / TOTALE_FLUSSI
        | project PERC_DIFF_AMOUNT
    """
            .trimIndent()

    /** Query to calculate Wrong Amount in "Flusso di Rendicontazione" (WAFDR) KPI for BROKER PSP */
    val WAFDR_BROKER_QUERY =
        """
        let start=datetime(${'$'}START_DATE 00:00:00);
        let end=datetime(${'$'}END_DATE 23:59:59);
        KPI_RENDICONTAZIONI
        | where GIORNATA_PAGAMENTO between (start .. end)
        | where ID_BROKER_PSP == ${'$'}Int_psp
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI),
                    TOTALE_DIFF_AMOUNT=sum(TOTALE_DIFF_AMOUNT)
        | extend PERC_DIFF_AMOUNT=(TOTALE_DIFF_AMOUNT * 100) / TOTALE_FLUSSI
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
        | where ID_PSP == "${'$'}PSP"
        | summarize TOTALE_FLUSSI=sum(TOTALE_FLUSSI)
    """
            .trimIndent()
}
