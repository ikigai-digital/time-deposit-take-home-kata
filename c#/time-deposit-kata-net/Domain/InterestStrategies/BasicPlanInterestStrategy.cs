namespace time_deposit_kata_net.InterestStrategies
{
    /// <summary>
    /// Interest calculation strategy for Basic Plan.
    /// Rules: 1% annual interest, no interest for first 30 days.
    /// </summary>
    public class BasicPlanInterestStrategy : IInterestCalculationStrategy
    {
        private const int MinimumDays = 30;
        private const double AnnualInterestRate = 0.01;
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
