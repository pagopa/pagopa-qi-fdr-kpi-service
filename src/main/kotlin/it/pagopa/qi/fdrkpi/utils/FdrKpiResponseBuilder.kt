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
fun dailyLfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    lateFdrV1: Int,
    lateFdrV2: Int,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): DailyLFDRMetricsDto {
    logger.debug(
        "Building daily LFDR metrics for date [{}], total reports: [{}], late FDR v1: [{}], late FDR v2: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        paymentDate,
        totalReports,
        lateFdrV1,
        lateFdrV2,
        entityType,
        pspId,
        brokerId
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
        .pspId(pspId)
        .brokerId(brokerId)
}

fun monthlyLfdrBuilder(
    kpiLfdrV1Value: String,
    kpiLfdrV2Value: String,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): MonthlyLFDRMetricsDto {
    logger.debug(
        "Building monthly LFDR metrics with v1 value: [{}], v2 value: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        kpiLfdrV1Value,
        kpiLfdrV2Value,
        entityType,
        pspId,
        brokerId
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
        .pspId(pspId)
        .brokerId(brokerId)
}

// --- Nrfdr
fun dailyNrfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    missingReports: Int,
    foundReports: Int,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): DailyNRFDRMetricsDto {
    logger.debug(
        "Building daily NRFDR metrics for date [{}], total reports: [{}], missing reports: [{}], found reports: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        paymentDate,
        totalReports,
        missingReports,
        foundReports,
        entityType,
        pspId,
        brokerId
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
        .pspId(pspId)
        .brokerId(brokerId)
}

fun monthlyNrfdrBuilder(
    kpiValue: String,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): MonthlyNRFDRMetricsDto {
    logger.debug(
        "Building monthly NRFDR metrics with value: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        kpiValue,
        entityType,
        pspId,
        brokerId
    )
    return MonthlyNRFDRMetricsDto(
            kpiValue,
            "monthly",
            "FdR non rendicontati",
            KPI_DESCRIPTION_URI,
            entityType,
            KpiNameEnum.NRFDR
        )
        .pspId(pspId)
        .brokerId(brokerId)
}

// --- Wpnfdr
fun dailyWpnfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    totalDiffNum: Int,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): DailyWPNFDRMetricsDto {
    logger.debug(
        "Building daily WPNFDR metrics for date [{}], total reports: [{}], total diff num: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        paymentDate,
        totalReports,
        totalDiffNum,
        entityType,
        pspId,
        brokerId
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
        .pspId(pspId)
        .brokerId(brokerId)
}

fun monthlyWpnfdrBuilder(
    kpiValue: String,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): MonthlyWPNFDRMetricsDto {
    logger.debug(
        "Building monthly WPNFDR metrics with value: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        kpiValue,
        entityType,
        pspId,
        brokerId
    )
    return MonthlyWPNFDRMetricsDto(
            kpiValue,
            "monthly",
            "FdR con numero di pagamenti errato",
            KPI_DESCRIPTION_URI,
            entityType,
            KpiNameEnum.WPNFDR
        )
        .pspId(pspId)
        .brokerId(brokerId)
}

// --- Wafdr
fun dailyWafdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    totalDiffNum: Int,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): DailyWAFDRMetricsDto {
    logger.debug(
        "Building daily WAFDR metrics for date [{}], total reports: [{}], total diff num: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        paymentDate,
        totalReports,
        totalDiffNum,
        entityType,
        pspId,
        brokerId
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
        .pspId(pspId)
        .brokerId(brokerId)
}

fun monthlyWafdrBuilder(
    kpiValue: String,
    entityType: EntityTypeEnum,
    brokerId: String?,
    pspId: String?
): MonthlyWPNFDRMetricsDto {
    logger.debug(
        "Building monthly WAFDR metrics with value: [{}], entity type: [{}], pspId: [{}], brokerId: [{}]",
        kpiValue,
        entityType,
        pspId,
        brokerId
    )
    return MonthlyWPNFDRMetricsDto(
            kpiValue,
            "monthly",
            "FdR con importo errato",
            KPI_DESCRIPTION_URI,
            entityType,
            KpiNameEnum.WAFDR
        )
        .pspId(pspId)
        .brokerId(brokerId)
}
