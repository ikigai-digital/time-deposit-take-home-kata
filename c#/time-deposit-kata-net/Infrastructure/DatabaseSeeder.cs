using Microsoft.EntityFrameworkCore;
using time_deposit_kata_net;

namespace time_deposit_kata_net.Infrastructure
{
    public static class DatabaseSeeder
    {
        public static async Task SeedAsync(TimeDepositDbContext context)
        {
            // Only seed if database is empty
            if (await context.TimeDeposits.AnyAsync())
            {
                return;
            }

            var timeDeposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "basic",
                    Balance = 10000.00,
                    Days = 45,
                    Withdrawals = new List<Withdrawal>
                    {
                        new Withdrawal
                        {
                            Id = 1,
                            Amount = 500.00,
                            Date = new DateTime(2025, 1, 10, 0, 0, 0, DateTimeKind.Utc)
                        }
                    }
                },
                new TimeDeposit
                {
                    Id = 2,
                    PlanType = "student",
                    Balance = 5000.00,
                    Days = 100,
                    Withdrawals = new List<Withdrawal>
                    {
                        new Withdrawal
                        {
                            Id = 2,
                            Amount = 200.00,
                            Date = new DateTime(2025, 1, 15, 0, 0, 0, DateTimeKind.Utc)
                        },
                        new Withdrawal
                        {
                            Id = 3,
                            Amount = 300.00,
                            Date = new DateTime(2025, 2, 1, 0, 0, 0, DateTimeKind.Utc)
                        }
                    }
                },
                new TimeDeposit
                {
                    Id = 3,
                    PlanType = "premium",
                    Balance = 25000.00,
                    Days = 60,
                    Withdrawals = new List<Withdrawal>
                    {
                        new Withdrawal
                        {
                            Id = 4,
                            Amount = 1000.00,
                            Date = new DateTime(2025, 1, 20, 0, 0, 0, DateTimeKind.Utc)
                        }
                    }
                },
                new TimeDeposit
                {
                    Id = 4,
                    PlanType = "basic",
                    Balance = 7500.00,
                    Days = 25
                },
                new TimeDeposit
                {
                    Id = 5,
                    PlanType = "student",
                    Balance = 3000.00,
                    Days = 400
                },
                new TimeDeposit
                {
                    Id = 6,
                    PlanType = "premium",
                    Balance = 15000.00,
                    Days = 40
                }
            };

            context.TimeDeposits.AddRange(timeDeposits);
            await context.SaveChangesAsync();
        }
    }
}
