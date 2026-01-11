using System.Collections.Generic;
using System.Threading.Tasks;

namespace time_deposit_kata_net.Application
{
    public interface ITimeDepositRepository
    {
        Task<List<TimeDeposit>> GetAllAsync();
        Task SaveChangesAsync();
    }
}
