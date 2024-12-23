package it.pagopa.qi.fdrkpi.service

import com.microsoft.azure.kusto.data.Client
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.LFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.TOTAL_FLOWS_QUERY
import it.pagopa.qi.fdrkpi.exceptionhandler.*
import it.pagopa.qi.fdrkpi.utils.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito.*

class FdrKpiServiceTest {

    private val reKustoClient: Client = mock(Client::class.java)
    private val fdrKpiService: FdrKpiService = FdrKpiService(reKustoClient, "re")

    private val brokerFiscalCode = "02654890025"
    private val pspID = "SARDIT31"

    companion object {
        private val date: OffsetDateTime =
            OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        @JvmStatic
        private fun successfullyQueriesProvider(): Stream<Arguments> =
            Stream.of(
                // LFDR - daily
                Arguments.of(
                    LFDR_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.daily,
                    10,
                    listOf(2, 3),
                    dailyPspLfdrBuilder(date, 10, 2, 3, EntityTypeEnum.PSP)
                ),
                // LFDR - monthly
                Arguments.of(
                    LFDR_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(2, 3),
                    monthlyLfdrBuilder("2", "3", EntityTypeEnum.PSP)
                ),
                // WAFDR - daily
                /* Arguments.of(
                    WAFDR_QUERY,
                    KpiNameEnum.WAFDR,
                    FdrKpiPeriod.daily,
                    8,
                    listOf(5),
                    dailyWafdrBuilder(date, 8, 5, EntityTypeEnum.PSP)
                ),
                // WAFDR - monthly
                Arguments.of(
                    WAFDR_QUERY,
                    KpiNameEnum.WAFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(7),
                    monthlyWafdrBuilder("7", EntityTypeEnum.PSP)
                ),
                // NRFDR - daily
                Arguments.of(
                    NRFDR_QUERY,
                    KpiNameEnum.NRFDR,
                    FdrKpiPeriod.daily,
                    6,
                    listOf(4),
                    dailyNrfdrBuilder(date, 6, 4, 2, EntityTypeEnum.PSP)
                ),
                // NRFDR - monthly
                Arguments.of(
                    NRFDR_QUERY,
                    KpiNameEnum.NRFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(10),
                    monthlyNrfdrBuilder("10", EntityTypeEnum.PSP)
                ),
                // WPNFDR - daily
                Arguments.of(
                    WPNFDR_QUERY,
                    KpiNameEnum.WPNFDR,
                    FdrKpiPeriod.daily,
                    5,
                    listOf(3),
                    dailyWpnfdrBuilder(date, 5, 3, EntityTypeEnum.PSP)
                ),
                // WPNFDR - monthly
                Arguments.of(
                    WPNFDR_QUERY,
                    KpiNameEnum.WPNFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(4),
                    monthlyWpnfdrBuilder("4", EntityTypeEnum.PSP)
                ),*/
            )
    }

    @ParameterizedTest
    @MethodSource("successfullyQueriesProvider")
    fun `Should return correct response from Kusto DB for PSP queries`(
        queryString: String,
        kpiNameEnum: KpiNameEnum,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {
        val dateString = if (fdrKpiPeriod == FdrKpiPeriod.daily) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod, dateString)

        if (fdrKpiPeriod == FdrKpiPeriod.daily) {
            mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, null, pspID, listOf(totalReports))
        }

        mockKustoResponse(queryString, dateRange, null, pspID, queryResponse)

        val response =
            fdrKpiService.calculateKpi(kpiNameEnum.name, fdrKpiPeriod.name, dateString, null, pspID)
        assertEquals(expectedResponse, response)
    }

    /*@ParameterizedTest
    @MethodSource("successfullyQueriesProvider")
    fun `Should return correct response from Kusto DB for Broker queries`(
        queryString: String,
        kpiNameEnum: KpiNameEnum,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {
        val dateString = if (fdrKpiPeriod == FdrKpiPeriod.daily) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod, dateString)

        if (fdrKpiPeriod == FdrKpiPeriod.daily) {
            mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, brokerFiscalCode, null, listOf(totalReports))
        }

        mockKustoResponse(queryString, dateRange, brokerFiscalCode, null, queryResponse)

        val response = fdrKpiService.calculateKpi(
            kpiNameEnum.name,
            fdrKpiPeriod.name,
            dateString,
            brokerFiscalCode,
            null
        )
        assertEquals(expectedResponse, response)
    }*/

    /*@ParameterizedTest
    @MethodSource("successfullyQueriesProvider")
    fun `Should return correct response from Kusto DB for Broker and PSP queries`(
        queryString: String,
        kpiNameEnum: KpiNameEnum,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {
        val dateString = if (fdrKpiPeriod == FdrKpiPeriod.daily) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod, dateString)

        if (fdrKpiPeriod == FdrKpiPeriod.daily) {
            mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, null, pspID, listOf(totalReports))
        }

        mockKustoResponse(queryString, dateRange, null, pspID, queryResponse)

        val response = fdrKpiService.calculateKpi(
            kpiNameEnum.name,
            fdrKpiPeriod.name,
            dateString,
            null,
            pspID
        )
        assertEquals(expectedResponse, response)
    }*/

    @Test
    fun `Should throw InvalidPeriodException`() {
        val ex =
            assertThrows<InvalidPeriodException> {
                fdrKpiService.calculateKpi(
                    KpiNameEnum.LFDR.name,
                    "invalid_period",
                    "2023-10-01",
                    null,
                    pspID
                )
            }
        assertEquals("Invalid Period", ex.message)
    }

    @Test
    fun `Should throw InvalidKpiTypeException`() {
        val ex =
            assertThrows<InvalidKpiTypeException> {
                fdrKpiService.calculateKpi(
                    "invalid_kpi",
                    FdrKpiPeriod.daily.name,
                    "2023-10-01",
                    null,
                    pspID
                )
            }
        assertEquals("Invalid KPI Type", ex.message)
    }

    @Test
    fun `Should throw DateTooRecentException for daily period`() {
        val today = LocalDate.now().toString()
        val ex =
            assertThrows<DateTooRecentException> {
                fdrKpiService.calculateKpi(
                    KpiNameEnum.LFDR.name,
                    FdrKpiPeriod.daily.name,
                    today,
                    null,
                    pspID
                )
            }
        assertEquals("Date Too Recent", ex.message)
    }

    @Test
    fun `Should throw DateTooRecentException for monthly period`() {
        val dateString = "2024-12-23"
        val ex =
            assertThrows<DateTooRecentException> {
                fdrKpiService.calculateKpi(
                    KpiNameEnum.LFDR.name,
                    FdrKpiPeriod.daily.name,
                    dateString,
                    null,
                    pspID
                )
            }
        assertEquals("Date Too Recent", ex.message)
    }

    /*@Test
    fun `Should throw NoResultsFoundException when no rows returned`() {
        val dateString = "2023-10-01"
        val dateRange = getDateRange(FdrKpiPeriod.daily, dateString)

        mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, null, pspID, listOf(10))
        mockKustoEmptyResponse(LFDR_QUERY, dateRange, null, pspID)

        val ex = assertThrows<NoResultsFoundException> {
            fdrKpiService.calculateKpi(
                KpiNameEnum.LFDR.name,
                FdrKpiPeriod.daily.name,
                dateString,
                null,
                pspID
            )
        }
        assertEquals("No Results Found", ex.message)
    }*/

    @Test
    fun `Should throw PspNotFoundException when percV1 and percV2 are -1`() {
        val dateString = "2023-10"
        val dateRange = getDateRange(FdrKpiPeriod.monthly, dateString)

        mockKustoResponse(KustoQueries.TOTAL_FLOWS_QUERY, dateRange, null, "ABCDEFGH", listOf(10))

        mockKustoResponse(KustoQueries.LFDR_QUERY, dateRange, null, "ABCDEFGH", listOf(-1, -1))

        val ex =
            assertThrows<PspNotFoundException> {
                fdrKpiService.calculateKpi(
                    KpiNameEnum.LFDR.name,
                    FdrKpiPeriod.monthly.name,
                    dateString,
                    null,
                    "ABCDEFGH"
                )
            }
        assertEquals("PSP Not Found", ex.message)
    }

    /*@Test
    fun `Should throw NoResultsFoundException when percV1=0 and percV2=0`() {
        val dateString = "2023-10-01"
        val dateRange = getDateRange(FdrKpiPeriod.daily, dateString)

        // TOT_FLOWS => 10
        mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, null, pspID, listOf(10))
        // KPI => 0,0
        mockKustoResponse(LFDR_QUERY, dateRange, null, pspID, listOf(0, 0))

        val ex = assertThrows<NoResultsFoundException> {
            fdrKpiService.calculateKpi(
                KpiNameEnum.LFDR.name,
                FdrKpiPeriod.daily.name,
                dateString,
                null,
                pspID
            )
        }
        assertEquals("No Results Found", ex.message)
    }*/

    @Test
    fun `Should throw generic Exception when underlying query fails`() {
        val dateString = "2023-10-01"
        val dateRange = getDateRange(FdrKpiPeriod.daily, dateString)

        mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, null, pspID, listOf(10))

        val query = prepareQuery(LFDR_QUERY, dateRange.first, dateRange.second, null, pspID)

        given(reKustoClient.executeQuery(eq("re"), eq(query)))
            .willThrow(RuntimeException("some error from ADX"))

        val ex =
            assertThrows<Exception> {
                fdrKpiService.calculateKpi(
                    KpiNameEnum.LFDR.name,
                    FdrKpiPeriod.daily.name,
                    dateString,
                    null,
                    pspID
                )
            }
        assertEquals(pspID, ex.message)
    }

    @Test
    fun `Should throw RuntimeException for brokerFiscalCode and pspId null on prepareQuery`() {
        val dateRange = Pair(LocalDate.parse("2023-01-01"), LocalDate.parse("2023-01-01"))
        val ex =
            assertThrows<RuntimeException> {
                prepareQuery(LFDR_QUERY, dateRange.first, dateRange.second)
            }
        assertEquals("BrokerFiscalCode and PspId are not defined", ex.message)
    }

    private fun mockKustoResponse(
        queryString: String,
        dateRange: Pair<LocalDate, LocalDate>,
        brokerFiscalCode: String?,
        pspId: String?,
        responseRow: List<Any>
    ) {
        val preparedQuery =
            prepareQuery(queryString, dateRange.first, dateRange.second, null, pspId)
        val operationResult = mock(KustoOperationResult::class.java)
        val resultSet = mock(KustoResultSetTable::class.java)

        given(resultSet.next()).willReturn(true)
        given(resultSet.currentRow).willReturn(responseRow)
        given(operationResult.primaryResults).willReturn(resultSet)
        given(reKustoClient.executeQuery("re", preparedQuery)).willReturn(operationResult)
    }

    /*private fun mockKustoEmptyResponse(
        queryString: String,
        dateRange: Pair<LocalDate, LocalDate>,
        brokerFiscalCode: String?,
        pspId: String?
    ) {
        val preparedQuery =
            prepareQuery(queryString, dateRange.first, dateRange.second, null, pspId)
        val operationResult = mock(KustoOperationResult::class.java)
        val resultSet = mock(KustoResultSetTable::class.java)

        given(resultSet.next()).willReturn(false)
        given(operationResult.primaryResults).willReturn(resultSet)
        given(reKustoClient.executeQuery("re", preparedQuery)).willReturn(operationResult)
    }*/
}
