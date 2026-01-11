using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using time_deposit_kata_net.Application;

namespace time_deposit_kata_net.Api
{
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
        /// Retrieves all time deposits with their withdrawals.
        /// </summary>
        /// <returns>List of time deposits</returns>
        [HttpGet]
        public async Task<ActionResult<List<TimeDepositResponse>>> GetAll()
        {
            var deposits = await _timeDepositService.GetAllTimeDepositsAsync();
            return Ok(deposits);
        }

        /// <summary>
        /// Updates the balances of all time deposits by applying monthly interest.
        /// </summary>
        /// <returns>OK if successful</returns>
        [HttpPost("update-balances")]
        public async Task<IActionResult> UpdateBalances()
        {
            await _timeDepositService.UpdateAllBalancesAsync();
            return Ok();
        }
    }
}
