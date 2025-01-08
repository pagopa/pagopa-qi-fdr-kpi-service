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
     * @param kpiType The type of KPI to calculate (required)
     * @param period The time period granularity (single day or calendar month) (required)
     * @param date For daily KPIs: Specify the full date (YYYY-MM-DD). Must be at least 10 days
     *   before current date. For monthly KPIs: Specify year and month (YYYY-MM). (required)
     * @param brokerId The fiscal code of the broker
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
        brokerId: String?,
        pspId: String?
    ): ResponseEntity<KPIResponseDto> {
        val requesterInfo =
            when {
                brokerId != null && pspId != null -> "Broker [$brokerId] (for PSP [$pspId])"
                brokerId != null -> "Broker [$brokerId]"
                pspId != null -> "PSP [$pspId]"
                else -> "Unknown requester"
            }

        logger.info(
            "Received [{}] [{}] KPI request from {} for date [{}]",
            period,
            kpiType,
            requesterInfo,
            date
        )

        try {
            val response = fdrKpiService.calculateKpi(kpiType, period, date, brokerId, pspId)
            logger.info(
                "Successfully calculated [{}] [{}] KPI for {} for date [{}]",
                period,
                kpiType,
                requesterInfo,
                date
            )
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error(
                "Error calculating [{}] [{}] KPI for {} for date [{}]: {}",
                period,
                kpiType,
                requesterInfo,
                date,
                e.message,
                e
            )
            throw e
        }
    }
}
