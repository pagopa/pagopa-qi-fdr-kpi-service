package it.pagopa.qi.fdrkpi.utils

import it.pagopa.generated.qi.fdrkpi.v1.model.*
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import java.net.URI
import java.time.OffsetDateTime
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("it.pagopa.qi.fdrkpi.utils.FdrKpiResponseBuilder")

val KPI_DESCRIPTION_URI =
    URI(
        "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
    )

// --- lfdr
fun dailyPspLfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    lateFdrV1: Int,
    lateFdrV2: Int,
    entityType: EntityTypeEnum,
): DailyLFDRMetricsDto {
    logger.debug(
        "Building daily LFDR metrics for date [{}], total reports: [{}], late FDR v1: [{}], late FDR v2: [{}], entity type: [{}]",
        paymentDate,
        totalReports,
        lateFdrV1,
        lateFdrV2,
        entityType
    )
    return DailyLFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        lateFdrV1,
        lateFdrV2,
        "FdR in ritardo",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.LFDR
    )
}

fun monthlyLfdrBuilder(
    kpiLfdrV1Value: String,
    kpiLfdrV2Value: String,
    entityType: EntityTypeEnum,
): MonthlyLFDRMetricsDto {
    logger.debug(
        "Building monthly LFDR metrics with v1 value: [{}], v2 value: [{}], entity type: [{}]",
        kpiLfdrV1Value,
        kpiLfdrV2Value,
        entityType
    )
    return MonthlyLFDRMetricsDto(
        "monthly",
        kpiLfdrV1Value,
        kpiLfdrV2Value,
        "FdR in ritardo",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.LFDR
    )
}

// --- Nrfdr
fun dailyNrfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    missingReports: Int,
    foundReports: Int,
    entityType: EntityTypeEnum,
): DailyNRFDRMetricsDto {
    logger.debug(
        "Building daily NRFDR metrics for date [{}], total reports: [{}], missing reports: [{}], found reports: [{}], entity type: [{}]",
        paymentDate,
        totalReports,
        missingReports,
        foundReports,
        entityType
    )
    return DailyNRFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        missingReports,
        foundReports,
        "FdR non rendicontati",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.NRFDR
    )
}

fun monthlyNrfdrBuilder(kpiValue: String, entityType: EntityTypeEnum): MonthlyNRFDRMetricsDto {
    logger.debug(
        "Building monthly NRFDR metrics with value: [{}], entity type: [{}]",
        kpiValue,
        entityType
    )
    return MonthlyNRFDRMetricsDto(
        kpiValue,
        "monthly",
        "FdR non rendicontati",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.NRFDR
    )
}

// --- Wpnfdr
fun dailyWpnfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    totalDiffNum: Int,
    entityType: EntityTypeEnum
): DailyWPNFDRMetricsDto {
    logger.debug(
        "Building daily WPNFDR metrics for date [{}], total reports: [{}], total diff num: [{}], entity type: [{}]",
        paymentDate,
        totalReports,
        totalDiffNum,
        entityType
    )
    return DailyWPNFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        totalDiffNum,
        "FdR con numero di pagamenti errato",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.WPNFDR
    )
}

fun monthlyWpnfdrBuilder(kpiValue: String, entityType: EntityTypeEnum): MonthlyWPNFDRMetricsDto {
    logger.debug(
        "Building monthly WPNFDR metrics with value: [{}], entity type: [{}]",
        kpiValue,
        entityType
    )
    return MonthlyWPNFDRMetricsDto(
        kpiValue,
        "monthly",
        "FdR con numero di pagamenti errato",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.WPNFDR
    )
}

// --- Wafdr
fun dailyWafdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    totalDiffNum: Int,
    entityType: EntityTypeEnum
): DailyWAFDRMetricsDto {
    logger.debug(
        "Building daily WAFDR metrics for date [{}], total reports: [{}], total diff num: [{}], entity type: [{}]",
        paymentDate,
        totalReports,
        totalDiffNum,
        entityType
    )
    return DailyWAFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        totalDiffNum,
        "FdR con importo errato",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.WAFDR
    )
}

fun monthlyWafdrBuilder(kpiValue: String, entityType: EntityTypeEnum): MonthlyWPNFDRMetricsDto {
    logger.debug(
        "Building monthly WAFDR metrics with value: [{}], entity type: [{}]",
        kpiValue,
        entityType
    )
    return MonthlyWPNFDRMetricsDto(
        kpiValue,
        "monthly",
        "FdR con importo errato",
        KPI_DESCRIPTION_URI,
        entityType,
        KpiNameEnum.WAFDR
    )
}
