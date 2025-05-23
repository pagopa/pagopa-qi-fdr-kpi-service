package it.pagopa.qi.fdrkpi.service

import com.microsoft.azure.kusto.data.Client
import com.microsoft.azure.kusto.data.KustoOperationResult
import com.microsoft.azure.kusto.data.KustoResultSetTable
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIResponseDto
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.LFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.NRFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.TOTAL_FLOWS_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.WAFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.WPNFDR_QUERY
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
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*

class FdrKpiServiceTest {

    private val reKustoClient: Client = mock(Client::class.java)
    private val fdrKpiService: FdrKpiService = FdrKpiService(reKustoClient, "re")

    private val pspID = "SARDIT31"

    companion object {
        private val date: OffsetDateTime =
            OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        @JvmStatic
        private fun successfullyQueriesProvider(): Stream<Arguments> {
            val pspID = "SARDIT31"

            return Stream.of(
                // LFDR - daily
                Arguments.of(
                    LFDR_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.daily,
                    10,
                    listOf(20, 30),
                    dailyLfdrBuilder(date, 10, 2, 3, EntityTypeEnum.PSP, null, pspID)
                ),
                // LFDR - monthly
                Arguments.of(
                    LFDR_QUERY,
                    KpiNameEnum.LFDR,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(2, 3),
                    monthlyLfdrBuilder("2", "3", EntityTypeEnum.PSP, null, pspID)
                )
            )
        }

        @JvmStatic
        private fun kpiQueriesProvider(): Stream<Arguments> {
            val pspID = "SARDIT31"

            return Stream.of(
                // LFDR - daily
                Arguments.of(
                    KpiNameEnum.LFDR,
                    LFDR_QUERY,
                    FdrKpiPeriod.daily,
                    10,
                    listOf(20, 30),
                    dailyLfdrBuilder(
                        paymentDate = OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                        totalReports = 10,
                        lateFdrV1 = 2,
                        lateFdrV2 = 3,
                        entityType = EntityTypeEnum.PSP,
                        brokerFiscalCode = null,
                        pspId = pspID
                    )
                ),
                // WAFDR - daily
                Arguments.of(
                    KpiNameEnum.WAFDR,
                    WAFDR_QUERY,
                    FdrKpiPeriod.daily,
                    10,
                    listOf(30),
                    dailyWafdrBuilder(
                        paymentDate = OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                        totalReports = 10,
                        totalDiffNum = 3,
                        entityType = EntityTypeEnum.PSP,
                        brokerFiscalCode = null,
                        pspId = pspID
                    )
                ),
                // WAFDR - monthly
                Arguments.of(
                    KpiNameEnum.WAFDR,
                    WAFDR_QUERY,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(3),
                    monthlyWafdrBuilder("3", EntityTypeEnum.PSP, null, pspID)
                ),
                // NRFDR - daily
                Arguments.of(
                    KpiNameEnum.NRFDR,
                    NRFDR_QUERY,
                    FdrKpiPeriod.daily,
                    10,
                    listOf(20),
                    dailyNrfdrBuilder(
                        paymentDate = OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                        totalReports = 10,
                        missingReports = 2,
                        foundReports = 8,
                        entityType = EntityTypeEnum.PSP,
                        brokerFiscalCode = null,
                        pspId = pspID
                    )
                ),
                // NRFDR - monthly
                Arguments.of(
                    KpiNameEnum.NRFDR,
                    NRFDR_QUERY,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(2),
                    monthlyNrfdrBuilder("2", EntityTypeEnum.PSP, null, pspID)
                ),
                // WPNFDR - daily
                Arguments.of(
                    KpiNameEnum.WPNFDR,
                    WPNFDR_QUERY,
                    FdrKpiPeriod.daily,
                    10,
                    listOf(40),
                    dailyWpnfdrBuilder(
                        paymentDate = OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                        totalReports = 10,
                        totalDiffNum = 4,
                        entityType = EntityTypeEnum.PSP,
                        brokerFiscalCode = null,
                        pspId = pspID
                    )
                ),
                // WPNFDR - monthly
                Arguments.of(
                    KpiNameEnum.WPNFDR,
                    WPNFDR_QUERY,
                    FdrKpiPeriod.monthly,
                    0,
                    listOf(4),
                    monthlyWpnfdrBuilder("4", EntityTypeEnum.PSP, null, pspID)
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("kpiQueriesProvider")
    fun `Should return correct responses for different KPI types`(
        kpiType: KpiNameEnum,
        queryString: String,
        fdrKpiPeriod: FdrKpiPeriod,
        totalReports: Int,
        queryResponse: List<Any>,
        expectedResponse: KPIResponseDto
    ) {
        val dateString = if (fdrKpiPeriod == FdrKpiPeriod.daily) "2023-10-01" else "2023-10"
        val dateRange = getDateRange(fdrKpiPeriod, dateString)
        val pspId = "SARDIT31"
        val brokerFiscalCode: String? = null

        if (fdrKpiPeriod == FdrKpiPeriod.daily) {
            mockKustoResponse(
                TOTAL_FLOWS_QUERY,
                dateRange,
                pspId,
                brokerFiscalCode,
                listOf(totalReports)
            )
        }

        mockKustoResponse(queryString, dateRange, pspId, brokerFiscalCode, queryResponse)

        val response =
            fdrKpiService.calculateKpi(
                kpiType.name,
                fdrKpiPeriod.name,
                dateString,
                brokerFiscalCode,
                pspId
            )

        assertEquals(expectedResponse, response)
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
            mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, pspID, null, listOf(totalReports))
        }

        mockKustoResponse(queryString, dateRange, pspID, null, queryResponse)

        val response =
            fdrKpiService.calculateKpi(kpiNameEnum.name, fdrKpiPeriod.name, dateString, null, pspID)

        assertEquals(expectedResponse, response)
    }

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
    fun `Should throw PspNotFoundException when percV1 and percV2 are -1`() {
        val dateString = "2023-10"
        val dateRange = getDateRange(FdrKpiPeriod.monthly, dateString)

        mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, "ABCDEFGH", null, listOf(10))

        mockKustoResponse(LFDR_QUERY, dateRange, "ABCDEFGH", null, listOf(-1, -1))

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

    @Test
    fun `Should throw generic Exception when underlying query fails`() {
        val dateString = "2023-10-01"
        val dateRange = getDateRange(FdrKpiPeriod.daily, dateString)

        mockKustoResponse(TOTAL_FLOWS_QUERY, dateRange, pspID, null, listOf(10))

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
        pspId: String?,
        brokerFiscalCode: String?,
        responseRow: List<Any>
    ) {
        val preparedQuery =
            prepareQuery(queryString, dateRange.first, dateRange.second, brokerFiscalCode, pspId)
        val operationResult = mock(KustoOperationResult::class.java)
        val resultSet = mock(KustoResultSetTable::class.java)

        given(resultSet.next()).willReturn(true, false)
        given(resultSet.currentRow).willReturn(responseRow)
        given(operationResult.primaryResults).willReturn(resultSet)
        given(reKustoClient.executeQuery("re", preparedQuery)).willReturn(operationResult)
    }
}
