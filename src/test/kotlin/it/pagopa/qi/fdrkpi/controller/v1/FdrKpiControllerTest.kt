package it.pagopa.qi.fdrkpi.controller.v1

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
    }
}
