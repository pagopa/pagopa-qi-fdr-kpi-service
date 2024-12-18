package it.pagopa.qi.fdrkpi.utils

import it.pagopa.generated.qi.fdrkpi.v1.model.*
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.KpiNameEnum
import java.net.URI
import java.time.OffsetDateTime

// --- lfdr
fun dailyPspLfdrBuilder(
    paymentDate: OffsetDateTime,
    totalReports: Int,
    lateFdrV1: Int,
    lateFdrV2: Int,
    entityType: EntityTypeEnum,
): DailyLFDRMetricsDto {
    return DailyLFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        lateFdrV1,
        lateFdrV2,
        "FdR non rendicontati",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
        entityType,
        KpiNameEnum.LFDR
    )
}

fun monthlyLfdrBuilder(
    kpiLfdrV1Value: String,
    kpiLfdrV2Value: String,
    entityType: EntityTypeEnum,
): MonthlyLFDRMetricsDto {
    return MonthlyLFDRMetricsDto(
        "monthly",
        kpiLfdrV1Value,
        kpiLfdrV2Value,
        "FdR non rendicontati",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
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
    return DailyNRFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        missingReports,
        foundReports,
        "FdR non rendicontati",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
        entityType,
        KpiNameEnum.NRFDR
    )
}

fun monthlyNrfdrBuilder(kpiValue: String, entityType: EntityTypeEnum): MonthlyNRFDRMetricsDto {
    return MonthlyNRFDRMetricsDto(
        kpiValue,
        "monthly",
        "FdR non rendicontati",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
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
    return DailyWPNFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        totalDiffNum,
        "FdR con numero di pagamenti errato",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
        entityType,
        KpiNameEnum.WPNFDR
    )
}

fun monthlyWpnfdrBuilder(kpiValue: String, entityType: EntityTypeEnum): MonthlyWPNFDRMetricsDto {
    return MonthlyWPNFDRMetricsDto(
        kpiValue,
        "monthly",
        "FdR con numero di pagamenti errato",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
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
    return DailyWAFDRMetricsDto(
        paymentDate,
        totalReports,
        "daily",
        totalDiffNum,
        "FdR con importo errato",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
        entityType,
        KpiNameEnum.WAFDR
    )
}

fun monthlyWafdrBuilder(kpiValue: String, entityType: EntityTypeEnum): MonthlyWPNFDRMetricsDto {
    return MonthlyWPNFDRMetricsDto(
        kpiValue,
        "monthly",
        "FdR con importo errato",
        URI(
            "https://developer.pagopa.it/pago-pa/guides/sanp/prestatore-di-servizi-di-pagamento/quality-improvement"
        ),
        entityType,
        KpiNameEnum.WAFDR
    )
}
