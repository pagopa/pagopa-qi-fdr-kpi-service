package it.pagopa.qi.fdrkpi.utils

import com.microsoft.azure.kusto.data.Client
import it.pagopa.generated.qi.fdrkpi.v1.model.KPIEntityResponseAllOfDto.EntityTypeEnum
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries
import java.time.LocalDate
import java.time.YearMonth

fun executeLfdrQuery(
    typeEnum: EntityTypeEnum,
    dateRange: Pair<LocalDate, LocalDate>,
    xPspCode: String,
    xEntityFiscalCode: String?,
    reKustoClient: Client
): List<Any> {
    var query = ""
    var idSearch = ""
    when (typeEnum) {
        EntityTypeEnum.PSP -> {
            query = KustoQueries.LFDR_PSP_QUERY
            idSearch = xPspCode
        }
        EntityTypeEnum.BROKER -> {
            query = KustoQueries.LFDR_BROKER_QUERY
            idSearch = xEntityFiscalCode!!
        }
    }
    return extractResult(query, typeEnum, dateRange, idSearch, reKustoClient)
}

fun executeWafdrQuery(
    typeEnum: EntityTypeEnum,
    dateRange: Pair<LocalDate, LocalDate>,
    xPspCode: String,
    xEntityFiscalCode: String?,
    reKustoClient: Client
): List<Any> {
    var query = ""
    var idSearch = ""
    when (typeEnum) {
        EntityTypeEnum.PSP -> {
            query = KustoQueries.WAFDR_PSP_QUERY
            idSearch = xPspCode
        }
        EntityTypeEnum.BROKER -> {
            query = KustoQueries.WAFDR_BROKER_QUERY
            idSearch = xEntityFiscalCode!!
        }
    }
    return extractResult(query, typeEnum, dateRange, idSearch, reKustoClient)
}

fun executeNrfdrQuery(
    typeEnum: EntityTypeEnum,
    dateRange: Pair<LocalDate, LocalDate>,
    xPspCode: String,
    xEntityFiscalCode: String?,
    reKustoClient: Client
): List<Any> {
    var query = ""
    var idSearch = ""
    when (typeEnum) {
        EntityTypeEnum.PSP -> {
            query = KustoQueries.NRFDR_PSP_QUERY
            idSearch = xPspCode
        }
        EntityTypeEnum.BROKER -> {
            query = KustoQueries.NRFDR_BROKER_QUERY
            idSearch = xEntityFiscalCode!!
        }
    }
    return extractResult(query, typeEnum, dateRange, idSearch, reKustoClient)
}

fun executeWpnfdrQuery(
    typeEnum: EntityTypeEnum,
    dateRange: Pair<LocalDate, LocalDate>,
    xPspCode: String,
    xEntityFiscalCode: String?,
    reKustoClient: Client
): List<Any> {
    var query = ""
    var idSearch = ""
    when (typeEnum) {
        EntityTypeEnum.PSP -> {
            query = KustoQueries.WPNFDR_PSP_QUERY
            idSearch = xPspCode
        }
        EntityTypeEnum.BROKER -> {
            query = KustoQueries.WPNFDR_BROKER_QUERY
            idSearch = xEntityFiscalCode!!
        }
    }
    return extractResult(query, typeEnum, dateRange, idSearch, reKustoClient)
}

fun extractResult(
    query: String,
    typeEnum: EntityTypeEnum,
    dateRange: Pair<LocalDate, LocalDate>,
    xPspCode: String,
    reKustoClient: Client
): List<Any> {
    val result =
        reKustoClient.executeQuery(
            preparePspQuery(query, typeEnum, dateRange.first, dateRange.second, xPspCode)
        )
    val primaryResult = result.primaryResults
    while (primaryResult.next()) {
        return primaryResult.currentRow
    }
    throw IllegalArgumentException("error")
}

fun preparePspQuery(
    query: String,
    entityTypeEnum: EntityTypeEnum,
    startDate: LocalDate,
    endDate: LocalDate,
    psp: String
): String {
    return when (entityTypeEnum) {
        EntityTypeEnum.PSP ->
            query
                .replace("\$START_DATE", startDate.toString())
                .replace("\$END_DATE", endDate.toString())
                .replace("\$PSP", psp)
        EntityTypeEnum.BROKER ->
            query
                .replace("\$START_DATE", startDate.toString())
                .replace("\$END_DATE", endDate.toString())
                .replace("\$Int_psp", psp)
    }
}

fun getDateRange(period: String, date: String): Pair<LocalDate, LocalDate> {
    return when (FdrKpiPeriod.valueOf(period)) {
        FdrKpiPeriod.daily -> {
            Pair(LocalDate.parse(date), LocalDate.parse(date))
        }
        FdrKpiPeriod.monthly -> {
            val ym = YearMonth.parse(date)
            Pair(ym.atDay(1), ym.atEndOfMonth())
        }
    }
}
