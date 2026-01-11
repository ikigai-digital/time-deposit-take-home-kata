namespace time_deposit_kata_net.InterestStrategies
{
    /// <summary>
    /// Interest calculation strategy for Student Plan.
    /// Rules: 3% annual interest, no interest for first 30 days, no interest after 1 year (365 days).
    /// </summary>
    public class StudentPlanInterestStrategy : IInterestCalculationStrategy
    {
        private const int MinimumDays = 30;
        private const int MaximumDays = 365;
        private const double AnnualInterestRate = 0.03;
        private const int MonthsPerYear = 12;

        public double CalculateInterest(TimeDeposit deposit)
        {
            if (deposit.Days <= MinimumDays || deposit.Days >= MaximumDays)
            {
                return 0;
            }

            return deposit.Balance * AnnualInterestRate / MonthsPerYear;
        }
    }
}
