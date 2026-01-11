using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using time_deposit_kata_net.Application;

namespace time_deposit_kata_net.Infrastructure
{
    public class TimeDepositRepository : ITimeDepositRepository
    {
        private readonly TimeDepositDbContext _context;

        public TimeDepositRepository(TimeDepositDbContext context)
        {
            _context = context;
        }

        public async Task<List<TimeDeposit>> GetAllAsync()
        {
            return await _context.TimeDeposits.ToListAsync();
        }

        public async Task SaveChangesAsync()
        {
            await _context.SaveChangesAsync();
        }
    }
}
