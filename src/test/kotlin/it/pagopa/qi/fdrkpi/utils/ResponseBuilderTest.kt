package it.pagopa.qi.fdrkpi.utils

import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import java.net.URI
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class ResponseBuilderTest {

    private val paymentDate: OffsetDateTime = OffsetDateTime.parse("2023-12-03T10:15:30+01:00")
    private val kpiUri =
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        )
    private val brokerId = "01153230360"
    private val pspId = "SARDIT31"

    @Test
    fun `Should return DailyLFDRMetricsDto`() {
        val result = dailyLfdrBuilder(paymentDate, 1, 1, 1, EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals(paymentDate, result.paymentDate)
        assertEquals(1, result.totalReports)
        assertEquals(1, result.lateFdrV1)
        assertEquals(1, result.lateFdrV2)
        assertEquals("daily", result.responseType)
        assertEquals("FdR in ritardo", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.LFDR, result.kpiName)
    }
    @Test
    fun `Should return MonthlyLFDRMetricsDto`() {
        val result = monthlyLfdrBuilder("90%", "80%", EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals("monthly", result.responseType)
        assertEquals("90%", result.kpiLfdrV1Value)
        assertEquals("80%", result.kpiLfdrV2Value)
        assertEquals("FdR in ritardo", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.LFDR, result.kpiName)
    }

    @Test
    fun `Should return DailyNRFDRMetricsDto`() {
        val result = dailyNrfdrBuilder(paymentDate, 10, 3, 7, EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals(paymentDate, result.paymentDate)
        assertEquals(10, result.totalReports)
        assertEquals("daily", result.responseType)
        assertEquals(3, result.missingReports)
        assertEquals(7, result.foundReports)
        assertEquals("FdR non rendicontati", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.NRFDR, result.kpiName)
    }

    @Test
    fun `Should return MonthlyNRFDRMetricsDto`() {
        val result = monthlyNrfdrBuilder("85%", EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals("85%", result.kpiValue)
        assertEquals("monthly", result.responseType)
        assertEquals("FdR non rendicontati", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.NRFDR, result.kpiName)
    }

    @Test
    fun `Should return DailyWPNFDRMetricsDto`() {
        val result = dailyWpnfdrBuilder(paymentDate, 15, 2, EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals(paymentDate, result.paymentDate)
        assertEquals(15, result.totalReports)
        assertEquals("daily", result.responseType)
        assertEquals(2, result.totalDiffNum)
        assertEquals("FdR con numero di pagamenti errato", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.WPNFDR, result.kpiName)
    }

    @Test
    fun `Should return MonthlyWPNFDRMetricsDto`() {
        val result = monthlyWpnfdrBuilder("70%", EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals("70%", result.kpiValue)
        assertEquals("monthly", result.responseType)
        assertEquals("FdR con numero di pagamenti errato", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.WPNFDR, result.kpiName)
    }

    @Test
    fun `Should return DailyWAFDRMetricsDto`() {
        val result = dailyWafdrBuilder(paymentDate, 20, 5, EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals(paymentDate, result.paymentDate)
        assertEquals(20, result.totalReports)
        assertEquals("daily", result.responseType)
        assertEquals(5, result.totalDiffAmount)
        assertEquals("FdR con importo errato", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.WAFDR, result.kpiName)
    }

    @Test
    fun `Should return MonthlyWAFDRMetricsDto`() {
        val result = monthlyWafdrBuilder("65%", EntityTypeEnum.PSP, brokerId, pspId)
        assertEquals("65%", result.kpiValue)
        assertEquals("monthly", result.responseType)
        assertEquals("FdR con importo errato", result.kpiDescription)
        assertEquals(kpiUri, result.kpiDescriptionUrl)
        assertEquals(EntityTypeEnum.PSP, result.entityType)
        assertEquals(KpiNameEnum.WAFDR, result.kpiName)
    }
}
