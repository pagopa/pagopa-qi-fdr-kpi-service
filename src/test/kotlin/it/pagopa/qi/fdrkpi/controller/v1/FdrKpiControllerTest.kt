package it.pagopa.qi.fdrkpi.controller.v1

import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseDto
import it.pagopa.generated.qi.fdrkpi.v1.model.MonthlyKPIResponseDto
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations

/**
 * @author d.tangredi
 * @created 16/12/2024 - 10:49
 */
class FdrKpiControllerTest {

    private lateinit var fdrKpiController: FdrKpiController
    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fdrKpiController = FdrKpiController()
    }

    @Test
    fun `calculateKpi should return correct monthly KPI data`() {

        val xEntityFiscalCode = "ABCDEF70P20Z701Z"
        val kpiType = "kpiType"
        val period = "202409"
        val date = "2024-09-15"
        val xPspCode = "CIPBITMM"

        val result =
            fdrKpiController.calculateKpi(
                xEntityFiscalCode = xEntityFiscalCode,
                kpiType = kpiType,
                period = period,
                date = date,
                xPspCode = xPspCode,
            )

        // Assertions
        assertThat(result.statusCode.is2xxSuccessful).isTrue

        val kpiResponse = result.body as MonthlyKPIResponseDto
        assertThat(kpiResponse.responseType).isEqualTo("monthly")
        assertThat(kpiResponse.idPsp).isEqualTo("CIPBITMM")
        assertThat(kpiResponse.kpiName).isEqualTo(KPIEntityResponseDto.KpiNameEnum.LFDR)
        assertThat(kpiResponse.kpiLfdrV1Value).isEqualTo("0.01")
        assertThat(kpiResponse.kpiLfdrV2Value).isEqualTo("0.02")
        assertThat(kpiResponse.kpiDescription).isEqualTo("FdR in ritardo")
        assertThat(kpiResponse.kpiDescriptionUrl)
            .isEqualTo(
                URI(
                    "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
                )
            )
    }

    @Test
    fun `calculateKpi should handle null parameters`() {

        val result =
            fdrKpiController.calculateKpi(
                xEntityFiscalCode = null,
                kpiType = null,
                period = null,
                date = null,
                xPspCode = null,
            )

        // Assertions
        assertThat(result.statusCode.is2xxSuccessful).isTrue

        val kpiResponse = result.body as MonthlyKPIResponseDto
        assertThat(kpiResponse.responseType).isEqualTo("monthly")
        assertThat(kpiResponse.idPsp).isEqualTo("CIPBITMM")
    }
}
