package it.pagopa.qi.fdrkpi.controller.v1

import it.pagopa.generated.qi.fdrkpi.v1.api.FdrKpiApi
import it.pagopa.generated.qi.fdrkpi.v1.model.*
import it.pagopa.qi.fdrkpi.service.FdrKpiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController("FdrKpiV1Controller")
class FdrKpiController(@Autowired private val fdrKpiService: FdrKpiService) : FdrKpiApi {
    /**
     * GET /fdr-kpi/{kpiType}/{period}
     *
     * @param kpiType The type of KPI to calculate (required)
     * @param period The time period granularity (single day or calendar month) (required)
     * @param date For daily KPIs: Specify the full date (YYYY-MM-DD). Must be at least 10 days
     *   before current date. For monthly KPIs: Specify year and month (YYYY-MM). (required)
     * @param brokerFiscalCode The fiscal code of the broker
     * @param pspId The fiscal code of the PSP
     * @return KPI calculated (status code 200) or Formally invalid input Possible error types: -
     *   DATE_TOO_RECENT: Daily KPI requests must be for dates at least 10 days in the past (status
     *   code 400) or PSP or Broker not found (status code 404) or Internal server error (status
     *   code 500)
     */
    override fun calculateKpi(
        kpiType: String,
        period: String,
        date: String,
        brokerFiscalCode: String?,
        pspId: String?
    ): ResponseEntity<KPIResponseDto> {
        val response = fdrKpiService.calculateKpi(kpiType, period, date, brokerFiscalCode, pspId)
        return ResponseEntity.ok(response)
    }
}
