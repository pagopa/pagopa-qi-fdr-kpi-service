package it.pagopa.qi.fdrkpi.service

import com.microsoft.azure.kusto.data.Client
import com.microsoft.azure.kusto.data.KustoOperationResult
import com.microsoft.azure.kusto.data.KustoResultSetTable
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIResponseDto
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries
import it.pagopa.qi.fdrkpi.utils.*
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*

class FdrKpiServiceTest {

    private val reKustoClient: Client = mock(Client::class.java)
    private val fdrKpiService: FdrKpiService = FdrKpiService(reKustoClient)
    private val brokerFiscalCode = "02654890025"
    private val pspID = "SARDIT31"

    companion object {
        private val date: OffsetDateTime =
            OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        @JvmStatic
        private fun successfullyQueries(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    KustoQueries.LFDR_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.daily,
                    1,
                    listOf(2, 3),
                    dailyPspLfdrBuilder(date, 1, 2, 3, EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.LFDR_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(2, 3),
                    monthlyLfdrBuilder("2", "3", EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.WAFDR_QUERY,
                    KpiNameEnum.WAFDR,
                    FdrKpiPeriod.daily,
                    2,
                    listOf(1),
                    dailyWafdrBuilder(date, 2, 1, EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.WAFDR_QUERY,
                    KpiNameEnum.WAFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(1),
                    monthlyWafdrBuilder("1", EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.NRFDR_QUERY,
                    KpiNameEnum.NRFDR,
                    FdrKpiPeriod.daily,
                    6,
                    listOf(5),
                    dailyNrfdrBuilder(date, 6, 5, 1, EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.NRFDR_QUERY,
                    KpiNameEnum.NRFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(1),
                    monthlyNrfdrBuilder("1", EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.WPNFDR_QUERY,
                    KpiNameEnum.WPNFDR,
                    FdrKpiPeriod.daily,
                    4,
                    listOf(3),
                    dailyWpnfdrBuilder(date, 4, 3, EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    KustoQueries.WPNFDR_QUERY,
                    KpiNameEnum.WPNFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(1),
                    monthlyWpnfdrBuilder("1", EntityTypeEnum.PSP)
                )
            )
    }

    @ParameterizedTest
    @MethodSource("successfullyQueries")
    fun `Should return correct response from Kusto DB for PSP queries`(
        queryString: String,
        kpiNameEnum: KpiNameEnum,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {

        val dateString = if (FdrKpiPeriod.daily == fdrKpiPeriod) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod.name, dateString)

        // totalReports query mock
        if (FdrKpiPeriod.daily == fdrKpiPeriod) {
            val queryTotalReports =
                prepareQuery(
                    KustoQueries.TOTAL_FLOWS_QUERY,
                    dateRange.first,
                    dateRange.second,
                    null,
                    pspID
                )
            val totalReportsCountKustoResp = mock(KustoOperationResult::class.java)
            val totalReportsResultSetTable = mock(KustoResultSetTable::class.java)
            given(totalReportsResultSetTable.currentRow).willReturn(listOf(totalReports))
            given(totalReportsCountKustoResp.primaryResults).willReturn(totalReportsResultSetTable)
            given(totalReportsResultSetTable.next()).willReturn(true)
            given(reKustoClient.executeQuery(any(), eq(queryTotalReports)))
                .willReturn(totalReportsCountKustoResp)
        }

        // Query Kusto mock
        val queryKusto = prepareQuery(queryString, dateRange.first, dateRange.second, null, pspID)
        val queryKustoResp = mock(KustoOperationResult::class.java)
        val queryResultSetTable = mock(KustoResultSetTable::class.java)
        given(queryResultSetTable.currentRow).willReturn(queryResponse)
        given(queryKustoResp.primaryResults).willReturn(queryResultSetTable)
        given(queryResultSetTable.next()).willReturn(true)
        given(reKustoClient.executeQuery(any(), eq(queryKusto))).willReturn(queryKustoResp)

        val response =
            fdrKpiService.calculateKpi(kpiNameEnum.name, fdrKpiPeriod.name, dateString, null, pspID)
        assertEquals(expectedResponse, response)
    }

    @ParameterizedTest
    @MethodSource("successfullyQueries")
    fun `Should return correct response from Kusto DB for Broker queries`(
        queryString: String,
        kpiNameEnum: KpiNameEnum,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {

        val dateString = if (FdrKpiPeriod.daily == fdrKpiPeriod) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod.name, dateString)

        // totalReports query mock
        if (FdrKpiPeriod.daily == fdrKpiPeriod) {
            val queryTotalReports =
                prepareQuery(
                    KustoQueries.TOTAL_FLOWS_QUERY,
                    dateRange.first,
                    dateRange.second,
                    brokerFiscalCode,
                    null
                )
            val totalReportsCountKustoResp = mock(KustoOperationResult::class.java)
            val totalReportsResultSetTable = mock(KustoResultSetTable::class.java)
            given(totalReportsResultSetTable.currentRow).willReturn(listOf(totalReports))
            given(totalReportsCountKustoResp.primaryResults).willReturn(totalReportsResultSetTable)
            given(totalReportsResultSetTable.next()).willReturn(true)
            given(reKustoClient.executeQuery(any(), eq(queryTotalReports)))
                .willReturn(totalReportsCountKustoResp)
        }

        // Query Kusto mock
        val queryKusto =
            prepareQuery(queryString, dateRange.first, dateRange.second, brokerFiscalCode, null)
        val queryKustoResp = mock(KustoOperationResult::class.java)
        val queryResultSetTable = mock(KustoResultSetTable::class.java)
        given(queryResultSetTable.currentRow).willReturn(queryResponse)
        given(queryKustoResp.primaryResults).willReturn(queryResultSetTable)
        given(queryResultSetTable.next()).willReturn(true)
        given(reKustoClient.executeQuery(any(), eq(queryKusto))).willReturn(queryKustoResp)

        val response =
            fdrKpiService.calculateKpi(
                kpiNameEnum.name,
                fdrKpiPeriod.name,
                dateString,
                brokerFiscalCode,
                null
            )
        assertEquals(expectedResponse, response)
    }

    @ParameterizedTest
    @MethodSource("successfullyQueries")
    fun `Should return correct response from Kusto DB for Broker and PSP queries`(
        queryString: String,
        kpiNameEnum: KpiNameEnum,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {

        val dateString = if (FdrKpiPeriod.daily == fdrKpiPeriod) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod.name, dateString)

        // totalReports query mock
        if (FdrKpiPeriod.daily == fdrKpiPeriod) {
            val queryTotalReports =
                prepareQuery(
                    KustoQueries.TOTAL_FLOWS_QUERY,
                    dateRange.first,
                    dateRange.second,
                    brokerFiscalCode,
                    pspID
                )
            val totalReportsCountKustoResp = mock(KustoOperationResult::class.java)
            val totalReportsResultSetTable = mock(KustoResultSetTable::class.java)
            given(totalReportsResultSetTable.currentRow).willReturn(listOf(totalReports))
            given(totalReportsCountKustoResp.primaryResults).willReturn(totalReportsResultSetTable)
            given(totalReportsResultSetTable.next()).willReturn(true)
            given(reKustoClient.executeQuery(any(), eq(queryTotalReports)))
                .willReturn(totalReportsCountKustoResp)
        }

        // Query Kusto mock
        val queryKusto =
            prepareQuery(queryString, dateRange.first, dateRange.second, brokerFiscalCode, pspID)
        val queryKustoResp = mock(KustoOperationResult::class.java)
        val queryResultSetTable = mock(KustoResultSetTable::class.java)
        given(queryResultSetTable.currentRow).willReturn(queryResponse)
        given(queryKustoResp.primaryResults).willReturn(queryResultSetTable)
        given(queryResultSetTable.next()).willReturn(true)
        given(reKustoClient.executeQuery(any(), eq(queryKusto))).willReturn(queryKustoResp)

        val response =
            fdrKpiService.calculateKpi(
                kpiNameEnum.name,
                fdrKpiPeriod.name,
                dateString,
                brokerFiscalCode,
                pspID
            )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `Should throw InvalidError`() {
        val dateString = "2023-10"

        // Query Kusto mock
        val queryKustoResp = mock(KustoOperationResult::class.java)
        val queryResultSetTable = mock(KustoResultSetTable::class.java)
        given(queryKustoResp.primaryResults).willReturn(queryResultSetTable)
        given(queryResultSetTable.next()).willReturn(false)
        given(reKustoClient.executeQuery(any(), any())).willReturn(queryKustoResp)

        val ex =
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                fdrKpiService.calculateKpi(
                    KpiNameEnum.LFDR.name,
                    FdrKpiPeriod.monthly.name,
                    dateString,
                    brokerFiscalCode,
                    pspID
                )
            }
        assertEquals("error", ex.message)
    }

    @Test
    fun `Should throw RuntimeException for brokerFiscalCode and PspID null on prepareQuery invoke`() {
        val dateRange = getDateRange("daily", "2023-01-01")
        val ex =
            org.junit.jupiter.api.assertThrows<RuntimeException> {
                prepareQuery(KustoQueries.LFDR_QUERY, dateRange.first, dateRange.second)
            }
        assertEquals("BrokerFiscalCode and PspId are not defined", ex.message)
    }
}
