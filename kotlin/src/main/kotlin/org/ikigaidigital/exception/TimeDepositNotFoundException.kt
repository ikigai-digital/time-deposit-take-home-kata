package org.ikigaidigital.exception

class TimeDepositNotFoundException(message: String) : RuntimeException(message)

class InsufficientBalanceException(message: String) : RuntimeException(message)