package org.ikigaidigital.adapter.inbound.web

import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.TimeDepositPage
import org.ikigaidigital.Withdrawal
import org.ikigaidigital.application.port.inbound.GetTimeDepositsQuery
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(TimeDepositController::class)
class TimeDepositControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var getTimeDeposits: GetTimeDepositsQuery

    @MockBean
    private lateinit var updateAllBalances: UpdateAllBalancesUseCase

    @Test
    fun `GET returns the first page of time deposits with the contract schema`() {
        `when`(getTimeDeposits.getTimeDeposits(0, 100)).thenReturn(
            TimeDepositPage(
                items = listOf(
                    TimeDepositDetails(
                        1, "basic", BigDecimal("1000.83"), 31,
                        listOf(Withdrawal(7, BigDecimal("100.50"), LocalDate.of(2026, 1, 15))),
                    ),
                    TimeDepositDetails(2, "premium", BigDecimal("50.00"), 10, emptyList()),
                ),
                page = 0,
                size = 100,
                totalElements = 2,
            )
        )

        mockMvc.get("/time-deposits").andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "items":[
                        {"id":1,"planType":"basic","balance":1000.83,"days":31,
                         "withdrawals":[{"id":7,"amount":100.50,"date":"2026-01-15"}]},
                        {"id":2,"planType":"premium","balance":50.00,"days":10,"withdrawals":[]}
                      ],
                      "page":0,"size":100,"totalElements":2,"totalPages":1
                    }
                    """,
                    strict = true,
                )
            }
        }
    }

    @Test
    fun `GET passes the page parameters to the query`() {
        `when`(getTimeDeposits.getTimeDeposits(3, 2)).thenReturn(TimeDepositPage(emptyList(), 3, 2, 5))

        mockMvc.get("/time-deposits?page=3&size=2").andExpect {
            status { isOk() }
            content {
                json("""{"items":[],"page":3,"size":2,"totalElements":5,"totalPages":3}""", strict = true)
            }
        }
    }

    @Test
    fun `POST balance-updates triggers the balance update`() {
        mockMvc.post("/time-deposits/balance-updates").andExpect {
            status { isNoContent() }
        }

        verify(updateAllBalances).updateAllBalances()
    }
}
