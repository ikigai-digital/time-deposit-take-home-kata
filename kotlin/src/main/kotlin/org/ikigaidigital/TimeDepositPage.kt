package org.ikigaidigital

data class TimeDepositPage(
    val items: List<TimeDepositDetails>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    val totalPages: Int get() = ((totalElements + size - 1) / size).toInt()
}
