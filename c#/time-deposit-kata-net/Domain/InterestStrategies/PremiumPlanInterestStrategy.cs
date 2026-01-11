namespace time_deposit_kata_net.InterestStrategies
{
    /// <summary>
    /// Interest calculation strategy for Premium Plan.
    /// Rules: 5% annual interest, interest starts after 45 days (no interest for first 30 days applies to all plans).
    /// </summary>
    public class PremiumPlanInterestStrategy : IInterestCalculationStrategy
    {
        private const int MinimumDays = 45; // Premium plan requires 45 days, but 30-day rule still applies
        private const double AnnualInterestRate = 0.05;
        private const int MonthsPerYear = 12;

        public double CalculateInterest(TimeDeposit deposit)
        {
            if (deposit.Days <= MinimumDays)
            {
                return 0;
            }

            return deposit.Balance * AnnualInterestRate / MonthsPerYear;
        }
    }
}
