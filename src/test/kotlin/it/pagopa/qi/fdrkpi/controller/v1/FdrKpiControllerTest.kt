package it.pagopa.qi.fdrkpi.controller.v1

import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.qi.fdrkpi.exceptionhandler.DateTooRecentException
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidKpiTypeException
import it.pagopa.qi.fdrkpi.exceptionhandler.PspNotFoundException
import it.pagopa.qi.fdrkpi.service.FdrKpiService
import it.pagopa.qi.fdrkpi.utils.dailyLfdrBuilder
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.ResponseEntity

class FdrKpiControllerTest {

    private lateinit var fdrKpiService: FdrKpiService
    private lateinit var fdrKpiController: FdrKpiController

    companion object {
        @JvmStatic
        private fun requesterInfoProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("BROKER123", "PSP456", "Broker [BROKER123] (for PSP [PSP456])"),
                Arguments.of("BROKER123", null, "Broker [BROKER123]"),
                Arguments.of(null, "PSP456", "PSP [PSP456]"),
                Arguments.of(null, null, "Unknown requester")
            )
        }
    }

    @BeforeEach
    fun setup() {
        fdrKpiService = mock(FdrKpiService::class.java)
        fdrKpiController = FdrKpiController(fdrKpiService)
    }

    @ParameterizedTest
    @MethodSource("requesterInfoProvider")
    fun `Should correctly format requester info for different combinations of brokerFiscalCode and pspId`(
        brokerFiscalCode: String?,
        pspId: String?
    ) {
        val kpiResponse =
            dailyLfdrBuilder(
                paymentDate = OffsetDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                totalReports = 10,
                lateFdrV1 = 1,
                lateFdrV2 = 2,
                entityType = EntityTypeEnum.PSP,
                brokerFiscalCode = brokerFiscalCode,
                pspId = pspId
            )

        given(fdrKpiService.calculateKpi("LFDR", "daily", "2023-10-01", brokerFiscalCode, pspId))
            .willReturn(kpiResponse)

        val response =
            fdrKpiController.calculateKpi("LFDR", "daily", "2023-10-01", brokerFiscalCode, pspId)

        assertEquals(ResponseEntity.ok(kpiResponse), response)
    }

    @Test
    fun `Should propagate service exceptions`() {
        val errorMessage = "Test error message"
        given(fdrKpiService.calculateKpi("LFDR", "daily", "2023-10-01", "BROKER123", "PSP456"))
            .willThrow(RuntimeException(errorMessage))

        val exception =
            assertThrows<RuntimeException> {
                fdrKpiController.calculateKpi("LFDR", "daily", "2023-10-01", "BROKER123", "PSP456")
            }
        assertEquals(errorMessage, exception.message)
    }

    @Test
    fun `Should handle InvalidKpiTypeException`() {
        given(fdrKpiService.calculateKpi("INVALID_KPI", "daily", "2023-10-01", null, "PSP456"))
            .willThrow(InvalidKpiTypeException("Invalid KPI Type"))

        val exception =
            assertThrows<InvalidKpiTypeException> {
                fdrKpiController.calculateKpi("INVALID_KPI", "daily", "2023-10-01", null, "PSP456")
            }
        assertEquals("Invalid KPI Type", exception.message)
    }

    @Test
    fun `Should handle DateTooRecentException`() {
        given(fdrKpiService.calculateKpi("LFDR", "daily", "2025-01-09", null, "PSP456"))
            .willThrow(DateTooRecentException("Date Too Recent"))

        val exception =
            assertThrows<DateTooRecentException> {
                fdrKpiController.calculateKpi("LFDR", "daily", "2025-01-09", null, "PSP456")
            }
        assertEquals("Date Too Recent", exception.message)
    }

    @Test
    fun `Should handle PspNotFoundException`() {
        given(fdrKpiService.calculateKpi("LFDR", "daily", "2023-10-01", null, "INVALID_PSP"))
            .willThrow(PspNotFoundException("PSP Not Found"))

        val exception =
            assertThrows<PspNotFoundException> {
                fdrKpiController.calculateKpi("LFDR", "daily", "2023-10-01", null, "INVALID_PSP")
            }
        assertEquals("PSP Not Found", exception.message)
    }
}
