package it.pagopa.qi.fdrkpi.controller.v1

import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseDto
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIResponseDto
import it.pagopa.generated.qi.fdrkpi.v1.model.MonthlyKPIResponseDto
import java.net.URI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.http.ResponseEntity
import reactor.test.StepVerifier

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
                exchange = null
            )

        StepVerifier.create(result)
            .expectNextMatches { response: ResponseEntity<KPIResponseDto> ->
                val kpiResponse = response.body as MonthlyKPIResponseDto

                response.statusCode.is2xxSuccessful &&
                    kpiResponse.responseType == "monthly" &&
                    kpiResponse.idPsp == "CIPBITMM" &&
                    kpiResponse.kpiName == KPIEntityResponseDto.KpiNameEnum.LFDR &&
                    kpiResponse.kpiLfdrV1Value == "0.01" &&
                    kpiResponse.kpiLfdrV2Value == "0.02" &&
                    kpiResponse.kpiDescription == "FdR in ritardo" &&
                    kpiResponse.kpiDescriptionUrl ==
                        URI(
                            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
                        )
            }
            .verifyComplete()
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
                exchange = null
            )

        StepVerifier.create(result)
            .expectNextMatches { response: ResponseEntity<KPIResponseDto> ->
                val kpiResponse = response.body as MonthlyKPIResponseDto

                response.statusCode.is2xxSuccessful &&
                    kpiResponse.responseType == "monthly" &&
                    kpiResponse.idPsp == "CIPBITMM"
            }
            .verifyComplete()
    }
}
