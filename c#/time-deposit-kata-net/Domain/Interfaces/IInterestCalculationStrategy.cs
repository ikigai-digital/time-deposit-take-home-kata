namespace time_deposit_kata_net
{
    /// <summary>
    /// Strategy interface for calculating interest based on plan type.
    /// Allows extensibility for future plan types without modifying existing code.
    /// </summary>
    public interface IInterestCalculationStrategy
    {
        /// <summary>
        /// Calculates the monthly interest for a time deposit.
        /// </summary>
        /// <param name="deposit">The time deposit to calculate interest for</param>
        /// <returns>The calculated interest amount</returns>
        double CalculateInterest(TimeDeposit deposit);
    }
}
