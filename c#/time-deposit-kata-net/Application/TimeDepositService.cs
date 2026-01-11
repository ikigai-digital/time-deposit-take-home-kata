using System.Collections.Generic;
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

        public async Task<List<TimeDeposit>> GetAllTimeDepositsAsync()
        {
            return await _repository.GetAllWithWithdrawalsAsync();
        }
    }
}
