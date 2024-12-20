package it.pagopa.qi.fdrkpi.controller.v1

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
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
        fdrKpiController = FdrKpiController(mock())
    }

    /*@Test
    fun `calculateKpi should return correct monthly KPI data`() {

        val brokerFiscalCode = "ABCDEF70P20Z701Z"
        val kpiType = "kpiType"
        val period = "202409"
        val date = "2024-09-15"
        val pspId = "CIPBITMM"

        val result =
            fdrKpiController.calculateKpi(
                kpiType = kpiType,
                period = period,
                date = date,
                brokerFiscalCode = brokerFiscalCode,
                pspId = pspId,
            )
    }

    @Test
    fun `calculateKpi should handle null parameters`() {

        val result =
            fdrKpiController.calculateKpi(
                kpiType = "",
                period = "",
                date = "",
                brokerFiscalCode = "",
                pspId = "",
            )
    }*/
}
