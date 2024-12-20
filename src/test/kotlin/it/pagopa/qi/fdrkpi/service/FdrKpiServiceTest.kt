package it.pagopa.qi.fdrkpi.service

import com.microsoft.azure.kusto.data.Client
import com.microsoft.azure.kusto.data.KustoOperationResult
import com.microsoft.azure.kusto.data.KustoResultSetTable
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.LFDR_PSP_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.NRFDR_PSP_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.WAFDR_PSP_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.WPNFDR_PSP_QUERY
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidKpiTypeException
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidPeriodException
import it.pagopa.qi.fdrkpi.exceptionhandler.NoResultsFoundException
import it.pagopa.qi.fdrkpi.exceptionhandler.PspNotFoundException
import it.pagopa.qi.fdrkpi.utils.*
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*

class FdrKpiServiceTest {

    private val reKustoClient: Client = mock(Client::class.java)
    private val fdrKpiService = FdrKpiService(reKustoClient)
    private val xEntityFiscalCode = "SARDIT31"

    companion object {
        private val date: OffsetDateTime =
            OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        @JvmStatic
        fun kpiTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    LFDR_PSP_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.daily,
                    2,
                    listOf(1, 2),
                    dailyPspLfdrBuilder(date, 2, 1, 2, EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    WAFDR_PSP_QUERY,
                    KpiNameEnum.WAFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(3),
                    monthlyWafdrBuilder("3", EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    NRFDR_PSP_QUERY,
                    KpiNameEnum.NRFDR,
                    FdrKpiPeriod.daily,
                    4,
                    listOf(1),
                    dailyNrfdrBuilder(date, 4, 1, 3, EntityTypeEnum.PSP)
                ),
                Arguments.of(
                    WPNFDR_PSP_QUERY,
                    KpiNameEnum.WPNFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(2),
                    monthlyWpnfdrBuilder("2", EntityTypeEnum.PSP)
                )
            )
    }

    @ParameterizedTest
    @MethodSource("kpiTestCases")
    fun `calculateKpi should return correct response`(
        queryString: String,
        kpiName: KpiNameEnum,
        period: FdrKpiPeriod,
        totalReports: Int,
        queryResult: List<Any>,
        expectedResponse: Any
    ) {
        val dateString = if (period == FdrKpiPeriod.daily) "2023-10-01" else "2023-10"

        // Mock della risposta per il query totale report
        if (period == FdrKpiPeriod.daily) {
            val totalReportsResponse = mockKustoResponse(listOf(totalReports))
            `when`(reKustoClient.executeQuery(any())).thenReturn(totalReportsResponse)
        }

        // Mock della risposta per il KPI principale
        val kpiResponse = mockKustoResponse(queryResult)
        `when`(reKustoClient.executeQuery(any())).thenReturn(kpiResponse)

        val response =
            fdrKpiService.calculateKpi(xEntityFiscalCode, kpiName.name, period.name, dateString)

        assertEquals(expectedResponse, response)
    }

    @Test
    fun `calculateKpi throws InvalidPeriodException for invalid period`() {
        val exception =
            assertThrows<InvalidPeriodException> {
                fdrKpiService.calculateKpi(
                    xEntityFiscalCode,
                    "LFDR",
                    "invalid_period",
                    "2023-10-01"
                )
            }
        assertEquals(
            "The provided period 'invalid_period' is invalid. Please ensure it follows the expected format.",
            exception.description
        )
    }

    @Test
    fun `calculateKpi throws InvalidKpiTypeException for invalid KPI type`() {
        val exception =
            assertThrows<InvalidKpiTypeException> {
                fdrKpiService.calculateKpi(xEntityFiscalCode, "INVALID_KPI", "daily", "2023-10-01")
            }
        assertEquals("The provided KPI type 'INVALID_KPI' is invalid.", exception.description)
    }

    @Test
    fun `calculateKpi throws PspNotFoundException`() {
        val kustoResponse = mockKustoResponse(listOf(-1, -1))
        `when`(reKustoClient.executeQuery(any())).thenReturn(kustoResponse)

        assertThrows<PspNotFoundException> {
            fdrKpiService.calculateKpi(xEntityFiscalCode, "LFDR", "daily", "2023-10-01")
        }
    }

    @Test
    fun `calculateKpi throws NoResultsFoundException`() {
        val kustoResponse = mockKustoResponse(listOf(0, 0))
        `when`(reKustoClient.executeQuery(any())).thenReturn(kustoResponse)

        assertThrows<NoResultsFoundException> {
            fdrKpiService.calculateKpi(xEntityFiscalCode, "LFDR", "daily", "2023-10-01")
        }
    }

    private fun mockKustoResponse(response: List<Any>): KustoOperationResult {
        val operationResult = mock(KustoOperationResult::class.java)
        val resultSetTable = mock(KustoResultSetTable::class.java)
        `when`(resultSetTable.next()).thenReturn(true)
        `when`(resultSetTable.currentRow).thenReturn(response)
        `when`(operationResult.primaryResults).thenReturn(resultSetTable)
        return operationResult
    }
}
