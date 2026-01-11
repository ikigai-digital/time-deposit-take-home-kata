using Microsoft.EntityFrameworkCore;

namespace time_deposit_kata_net.Infrastructure
{
    public class TimeDepositDbContext : DbContext
    {
        public TimeDepositDbContext(DbContextOptions<TimeDepositDbContext> options) : base(options)
        {
        }

        public DbSet<TimeDeposit> TimeDeposits { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<TimeDeposit>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.PlanType).IsRequired();
                entity.Property(e => e.Days).IsRequired();
                entity.Property(e => e.Balance).IsRequired();
            });
        }
    }
}
