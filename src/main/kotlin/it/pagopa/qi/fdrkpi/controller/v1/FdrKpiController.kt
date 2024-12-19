package it.pagopa.qi.fdrkpi.controller.v1

import it.pagopa.generated.qi.fdrkpi.v1.api.FdrKpiApi
import it.pagopa.generated.qi.fdrkpi.v1.model.*
import it.pagopa.qi.fdrkpi.service.FdrKpiService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController("FdrKpiV1Controller")
class FdrKpiController(@Autowired private val fdrKpiService: FdrKpiService) : FdrKpiApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * GET /fdr-kpi/{kpiType}/{period}
     *
     * @param xEntityFiscalCode The fiscal code of the entity: - For direct PSP queries: use PSP
     *   fiscal code - For broker queries: use broker fiscal code (required)
     * @param kpiType The type of KPI to calculate (required)
     * @param period The time period granularity (single day or calendar month) (required)
     * @param date For daily KPIs: Specify the full date (YYYY-MM-DD). Must be at least 10 days
     *   before current date. For monthly KPIs: Specify year and month (YYYY-MM). (required)
     * @param xPspCode PSP code - Required only for broker queries. - If entityCode is a broker
     *   fiscal code: this parameter is required - If entityCode is a PSP fiscal code: this
     *   parameter should not be provided (it would be a duplicate) (optional)
     * @return KPI calculated (status code 200) or Formally invalid input Possible error types: -
     *   DATE_TOO_RECENT: Daily KPI requests must be for dates at least 10 days in the past (status
     *   code 400) or PSP or Broker not found (status code 404) or Internal server error (status
     *   code 500)
     */
    override fun calculateKpi(
        xEntityFiscalCode: String,
        kpiType: String,
        period: String,
        date: String,
        xPspCode: String?
    ): ResponseEntity<KPIResponseDto> {
        val requesterInfo =
            if (xPspCode != null) {
                "Broker [$xEntityFiscalCode] (for PSP [$xPspCode])"
            } else {
                "PSP [$xEntityFiscalCode]"
            }

        logger.info(
            "Received [{}] [{}] KPI request from [{}] for date [{}]",
            period,
            kpiType,
            requesterInfo,
            date
        )
        val response = fdrKpiService.calculateKpi(xEntityFiscalCode, kpiType, period, date)
        return ResponseEntity.ok(response)
    }
}
