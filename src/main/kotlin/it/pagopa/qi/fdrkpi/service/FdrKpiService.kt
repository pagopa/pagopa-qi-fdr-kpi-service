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
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidKpiTypeException
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidPeriodException
import it.pagopa.qi.fdrkpi.exceptionhandler.NoResultsFoundException
import it.pagopa.qi.fdrkpi.exceptionhandler.PspNotFoundException
import it.pagopa.qi.fdrkpi.utils.*
import java.time.LocalDate
import java.time.ZoneOffset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FdrKpiService(
    @Autowired val reKustoClient: Client,
    @Value("\${azuredataexplorer.re.database}") private val database: String
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun calculateKpi(
        kpiType: String,
        period: String,
        date: String,
        brokerId: String?,
        pspId: String?
    ): KPIResponseDto {
        validateInputs(kpiType, period, date)

        val dateRange = getDateRange(FdrKpiPeriod.valueOf(period), date)
        val totalReports =
            if (FdrKpiPeriod.daily == FdrKpiPeriod.valueOf(period)) {
                executeQuery(KustoQueries.TOTAL_FLOWS_QUERY, dateRange, brokerId, pspId)
                    .firstOrNull() as? Int
                    ?: 0
            } else 0

        return when (KpiNameEnum.valueOf(kpiType)) {
            KpiNameEnum.LFDR -> buildLfdrResponse(period, dateRange, totalReports, brokerId, pspId)
            KpiNameEnum.WAFDR ->
                buildWafdrResponse(period, dateRange, totalReports, brokerId, pspId)
            KpiNameEnum.NRFDR ->
                buildNrfdrResponse(period, dateRange, totalReports, brokerId, pspId)
            KpiNameEnum.WPNFDR ->
                buildWpnfdrResponse(period, dateRange, totalReports, brokerId, pspId)
        }
    }

    private fun buildLfdrResponse(
        period: String,
        dateRange: Pair<LocalDate, LocalDate>,
        totalReports: Int,
        brokerId: String?,
        pspId: String?
    ): KPIResponseDto {
        val result = executeQuery(LFDR_QUERY, dateRange, brokerId, pspId)
        val entityType = if (brokerId != null) EntityTypeEnum.BROKER else EntityTypeEnum.PSP
        return when (FdrKpiPeriod.valueOf(period)) {
            FdrKpiPeriod.daily ->
                dailyLfdrBuilder(
                    dateRange.first
                        .atStartOfDay()
                        .atOffset(
                            ZoneOffset.UTC
                        ), // OffsetDateTime.of(dateRange.first.atStartOfDay(), ZoneOffset.UTC),
                    totalReports,
                    result[0] as Int,
                    result[1] as Int,
                    entityType,
                    brokerId,
                    pspId
                )
            FdrKpiPeriod.monthly ->
                monthlyLfdrBuilder(
                    result[0].toString(),
                    result[1].toString(),
                    entityType,
                    brokerId,
                    pspId
                )
        }
    }

    private fun buildWafdrResponse(
        period: String,
        dateRange: Pair<LocalDate, LocalDate>,
        totalReports: Int,
        brokerId: String?,
        pspId: String?
    ): KPIResponseDto {
        val result = executeQuery(WAFDR_QUERY, dateRange, brokerId, pspId)
        val entityType = if (brokerId != null) EntityTypeEnum.BROKER else EntityTypeEnum.PSP
        return when (FdrKpiPeriod.valueOf(period)) {
            FdrKpiPeriod.daily ->
                dailyWafdrBuilder(
                    dateRange.first.atStartOfDay().atOffset(ZoneOffset.UTC),
                    totalReports,
                    result[0] as Int,
                    entityType,
                    brokerId,
                    pspId
                )
            FdrKpiPeriod.monthly ->
                monthlyWafdrBuilder(result[0].toString(), entityType, brokerId, pspId)
        }
    }

    private fun buildNrfdrResponse(
        period: String,
        dateRange: Pair<LocalDate, LocalDate>,
        totalReports: Int,
        brokerId: String?,
        pspId: String?
    ): KPIResponseDto {
        val result = executeQuery(NRFDR_QUERY, dateRange, brokerId, pspId)
        val entityType = if (brokerId != null) EntityTypeEnum.BROKER else EntityTypeEnum.PSP
        val missingReports = result[0] as Int
        return when (FdrKpiPeriod.valueOf(period)) {
            FdrKpiPeriod.daily ->
                dailyNrfdrBuilder(
                    dateRange.first.atStartOfDay().atOffset(ZoneOffset.UTC),
                    totalReports,
                    missingReports,
                    totalReports - missingReports,
                    entityType,
                    brokerId,
                    pspId
                )
            FdrKpiPeriod.monthly ->
                monthlyNrfdrBuilder(result[0].toString(), entityType, brokerId, pspId)
        }
    }

    private fun buildWpnfdrResponse(
        period: String,
        dateRange: Pair<LocalDate, LocalDate>,
        totalReports: Int,
        brokerId: String?,
        pspId: String?
    ): KPIResponseDto {
        val result = executeQuery(WPNFDR_QUERY, dateRange, brokerId, pspId)
        val entityType = if (brokerId != null) EntityTypeEnum.BROKER else EntityTypeEnum.PSP
        return when (FdrKpiPeriod.valueOf(period)) {
            FdrKpiPeriod.daily ->
                dailyWpnfdrBuilder(
                    dateRange.first.atStartOfDay().atOffset(ZoneOffset.UTC),
                    totalReports,
                    result[0] as Int,
                    entityType,
                    brokerId,
                    pspId
                )
            FdrKpiPeriod.monthly ->
                monthlyWpnfdrBuilder(result[0].toString(), entityType, brokerId, pspId)
        }
    }

    private fun validateInputs(kpiType: String, period: String, date: String) {
        if (!FdrKpiPeriod.values().any { it.name == period }) throw InvalidPeriodException(period)
        if (!KpiNameEnum.values().any { it.name == kpiType }) throw InvalidKpiTypeException(kpiType)
        validateDate(period, date)
    }

    private fun executeQuery(
        query: String,
        dateRange: Pair<LocalDate, LocalDate>,
        brokerId: String?,
        pspId: String?
    ): List<Any> {
        val preparedQuery = prepareQuery(query, dateRange.first, dateRange.second, brokerId, pspId)
        logger.debug("Executing query: $preparedQuery")
        return try {
            val result = reKustoClient.executeQuery(database, preparedQuery)

            if (!result.primaryResults.next()) {
                throw NoResultsFoundException(pspId)
            }
            val row = result.primaryResults.currentRow
            if (row.size > 1) {
                val percV1 = row[0] as Int
                val percV2 = row[1] as Int
                when {
                    percV1 == -1 && percV2 == -1 -> {
                        throw PspNotFoundException(pspId)
                    }
                    else -> {
                        row
                    }
                }
            } else {
                row
            }
        } catch (e: PspNotFoundException) {
            throw e
        } catch (e: NoResultsFoundException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error executing query for PSP code: $pspId", e)
            throw Exception(pspId)
        }
    }
}
