package it.pagopa.qi.fdrkpi.service

import com.microsoft.azure.kusto.data.Client
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIResponseDto
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries
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
        xEntityFiscalCode: String?,
        kpiType: String,
        period: String,
        date: String,
        xPspCode: String
    ): KPIResponseDto {
        val dateRange: Pair<LocalDate, LocalDate> = getDateRange(period, date)
        val typeEnum = if (xEntityFiscalCode != null) EntityTypeEnum.BROKER else EntityTypeEnum.PSP
        var totalReports = 0

        if (FdrKpiPeriod.daily == FdrKpiPeriod.valueOf(period)) {
            totalReports =
                extractResult(
                    KustoQueries.TOTAL_FLOWS_QUERY,
                    typeEnum,
                    dateRange,
                    xPspCode,
                    reKustoClient
                )[0]
                    as Int
        }

        return when {
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.LFDR -> {
                val result =
                    executeLfdrQuery(
                        typeEnum,
                        dateRange,
                        xPspCode,
                        xEntityFiscalCode,
                        reKustoClient
                    )
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyPspLfdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            result[0] as Int,
                            result[1] as Int,
                            typeEnum
                        )
                    FdrKpiPeriod.monthly ->
                        monthlyLfdrBuilder(result[0] as String, result[1] as String, typeEnum)
                }
            }
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.WAFDR -> {
                val rows =
                    executeWafdrQuery(
                        typeEnum,
                        dateRange,
                        xPspCode,
                        xEntityFiscalCode,
                        reKustoClient
                    )
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyWafdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            rows[0] as Int,
                            typeEnum
                        )
                    FdrKpiPeriod.monthly -> monthlyWafdrBuilder(rows[0] as String, typeEnum)
                }
            }
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.NRFDR -> {
                val rows =
                    executeNrfdrQuery(
                        typeEnum,
                        dateRange,
                        xPspCode,
                        xEntityFiscalCode,
                        reKustoClient
                    )
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyNrfdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            rows[0] as Int,
                            rows[1] as Int,
                            typeEnum
                        )
                    FdrKpiPeriod.monthly -> monthlyNrfdrBuilder(rows[0] as String, typeEnum)
                }
            }
            KpiNameEnum.valueOf(kpiType) == KpiNameEnum.WPNFDR &&
                FdrKpiPeriod.daily == FdrKpiPeriod.valueOf(period) -> {
                val rows =
                    executeWpnfdrQuery(
                        typeEnum,
                        dateRange,
                        xPspCode,
                        xEntityFiscalCode,
                        reKustoClient
                    )
                when (FdrKpiPeriod.valueOf(period)) {
                    FdrKpiPeriod.daily ->
                        dailyWpnfdrBuilder(
                            OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                            totalReports,
                            rows[0] as Int,
                            typeEnum
                        )
                    FdrKpiPeriod.monthly -> monthlyWpnfdrBuilder(rows[0] as String, typeEnum)
                }
            }
            else -> throw IllegalArgumentException("error")
        }
    }
}
