using System.Collections.Generic;
using System.Threading.Tasks;

namespace time_deposit_kata_net.Application
{
    public interface ITimeDepositService
    {
        Task UpdateAllBalancesAsync();
        Task<List<TimeDepositResponse>> GetAllTimeDepositsAsync();
    }
}
