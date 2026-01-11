using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace time_deposit_kata_net.Application
{
    public class TimeDepositService : ITimeDepositService
    {
        private readonly ITimeDepositRepository _repository;
        private readonly TimeDepositCalculator _calculator;

        public TimeDepositService(ITimeDepositRepository repository, TimeDepositCalculator calculator)
        {
            _repository = repository;
            _calculator = calculator;
        }

        public async Task UpdateAllBalancesAsync()
        {
            var deposits = await _repository.GetAllAsync();
            _calculator.UpdateBalance(deposits);
            await _repository.SaveChangesAsync();
        }

        public async Task<List<TimeDepositResponse>> GetAllTimeDepositsAsync()
        {
            var deposits = await _repository.GetAllWithWithdrawalsAsync();
            
            return deposits.Select(d => new TimeDepositResponse
            {
                Id = d.Id,
                PlanType = d.PlanType,
                Balance = d.Balance,
                Days = d.Days,
                Withdrawals = d.Withdrawals.Select(w => new WithdrawalResponse
                {
                    Id = w.Id,
                    Amount = w.Amount,
                    Date = w.Date
                }).ToList()
            }).ToList();
        }
    }
}
