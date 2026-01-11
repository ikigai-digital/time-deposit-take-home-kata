using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using time_deposit_kata_net.Application;

namespace time_deposit_kata_net.Api
{
    /// <summary>
    /// Controller for managing time deposits and their balances.
    /// </summary>
    [ApiController]
    [Route("api/time-deposits")]
    public class TimeDepositsController : ControllerBase
    {
        private readonly ITimeDepositService _timeDepositService;

        public TimeDepositsController(ITimeDepositService timeDepositService)
        {
            _timeDepositService = timeDepositService;
        }

        /// <summary>
        /// Retrieves all time deposits from the database.
        /// </summary>
        /// <remarks>
        /// Returns a list of all time deposits with their associated withdrawals.
        /// Each time deposit includes:
        /// - id: Unique identifier
        /// - planType: The plan type (basic, student, or premium)
        /// - balance: Current balance
        /// - days: Number of days the deposit has been active
        /// - withdrawals: List of withdrawals associated with the deposit
        /// </remarks>
        /// <returns>A list of time deposits with their withdrawals</returns>
        /// <response code="200">Returns the list of time deposits</response>
        [HttpGet]
        [ProducesResponseType(typeof(List<TimeDepositResponse>), StatusCodes.Status200OK)]
        public async Task<ActionResult<List<TimeDepositResponse>>> GetAll()
        {
            var deposits = await _timeDepositService.GetAllTimeDepositsAsync();
            return Ok(deposits);
        }

        /// <summary>
        /// Updates the balances of all time deposits by applying monthly interest.
        /// </summary>
        /// <remarks>
        /// Calculates and applies monthly interest to all time deposits based on their plan type:
        /// - Basic Plan: 1% annual interest (applied after 30 days)
        /// - Student Plan: 3% annual interest (applied between 30-365 days, no interest after 1 year)
        /// - Premium Plan: 5% annual interest (applied after 45 days)
        /// 
        /// No interest is applied for the first 30 days for any plan type.
        /// Interest is calculated as monthly (annual rate / 12) and rounded to 2 decimal places.
        /// </remarks>
        /// <returns>OK if the operation was successful</returns>
        /// <response code="200">Balances updated successfully</response>
        [HttpPost("update-balances")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        public async Task<IActionResult> UpdateBalances()
        {
            await _timeDepositService.UpdateAllBalancesAsync();
            return Ok();
        }
    }
}
