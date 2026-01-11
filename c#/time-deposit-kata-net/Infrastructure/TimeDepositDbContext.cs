using Microsoft.EntityFrameworkCore;

namespace time_deposit_kata_net.Infrastructure
{
    public class TimeDepositDbContext : DbContext
    {
        public TimeDepositDbContext(DbContextOptions<TimeDepositDbContext> options) : base(options)
        {
        }

        public DbSet<TimeDeposit> TimeDeposits { get; set; }
        public DbSet<Withdrawal> Withdrawals { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<TimeDeposit>()
                .HasMany(t => t.Withdrawals)
                .WithOne(w => w.TimeDeposit)
                .HasForeignKey(w => w.TimeDepositId);
        }
    }
}
