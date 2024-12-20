package it.pagopa.qi.fdrkpi.service

import com.microsoft.azure.kusto.data.Client
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIResponseDto
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.LFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.NRFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.WAFDR_QUERY
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.WPNFDR_QUERY
import it.pagopa.qi.fdrkpi.utils.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FdrKpiService(
    @Autowired val reKustoClient: Client,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun calculateKpi(
        kpiType: String,
        period: String,
        date: String,
        brokerFiscalCode: String?,
        pspId: String?,
    ): KPIResponseDto {
        val dateRange: Pair<LocalDate, LocalDate> =
            getDateRange(period, date) // TODO manage exception cases
        var totalReports = 0

        if (FdrKpiPeriod.daily == FdrKpiPeriod.valueOf(period)) {
            totalReports =
                executeQuery(KustoQueries.TOTAL_FLOWS_QUERY, dateRange, brokerFiscalCode, pspId)[0]
                    as Int
        }
        val result: List<Any>
        return when {
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.LFDR -> {
                result = executeQuery(LFDR_QUERY, dateRange, brokerFiscalCode, pspId)
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyPspLfdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            result[0] as Int,
                            result[1] as Int,
                            EntityTypeEnum.PSP
                        )
                    FdrKpiPeriod.monthly ->
                        monthlyLfdrBuilder(
                            (result[0] as Int).toString(),
                            (result[1] as Int).toString(),
                            EntityTypeEnum.PSP
                        )
                }
            }
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.WAFDR -> {
                result = executeQuery(WAFDR_QUERY, dateRange, brokerFiscalCode, pspId)
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyWafdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            result[0] as Int,
                            EntityTypeEnum.PSP
                        )
                    FdrKpiPeriod.monthly ->
                        monthlyWafdrBuilder((result[0] as Int).toString(), EntityTypeEnum.PSP)
                }
            }
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.NRFDR -> {
                result = executeQuery(NRFDR_QUERY, dateRange, brokerFiscalCode, pspId)
                val missingReports = result[0] as Int
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyNrfdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            result[0] as Int,
                            totalReports - missingReports,
                            EntityTypeEnum.PSP
                        )
                    FdrKpiPeriod.monthly ->
                        monthlyNrfdrBuilder((result[0] as Int).toString(), EntityTypeEnum.PSP)
                }
            }
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.WPNFDR -> {
                result = executeQuery(WPNFDR_QUERY, dateRange, brokerFiscalCode, pspId)
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyWpnfdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            result[0] as Int,
                            EntityTypeEnum.PSP
                        )
                    FdrKpiPeriod.monthly ->
                        monthlyWpnfdrBuilder((result[0] as Int).toString(), EntityTypeEnum.PSP)
                }
            }
            else -> throw IllegalArgumentException("error") // TODO
        }
    }

    private fun executeQuery(
        query: String,
        dateRange: Pair<LocalDate, LocalDate>,
        brokerFiscalCode: String?,
        pspId: String?
    ): List<Any> {
        val result =
            reKustoClient.executeQuery(
                "re", // TODO
                prepareQuery(query, dateRange.first, dateRange.second, brokerFiscalCode, pspId)
            )
        val primaryResult = result.primaryResults
        while (primaryResult.next()) {
            return primaryResult.currentRow
        }
        throw IllegalArgumentException("error") // TODO
    }
}
