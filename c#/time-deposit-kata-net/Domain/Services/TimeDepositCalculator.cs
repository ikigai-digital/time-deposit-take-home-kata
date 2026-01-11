using System;
using System.Collections.Generic;

namespace time_deposit_kata_net
{
    /// <summary>
    /// Calculates and updates balances for time deposits using strategy pattern.
    /// Extensible design: New plan types can be added by registering new strategies
    /// </summary>
    public class TimeDepositCalculator
    {
        /// <summary>
        /// Updates the balance of all time deposits by applying monthly interest.
        /// Method signature preserved for backward compatibility.
        /// </summary>
        /// <param name="temps">List of time deposits to update</param>
        public void UpdateBalance(List<TimeDeposit> temps)
        {
            for (int i = 0; i < temps.Count; i++)
            {
                var strategy = InterestStrategyFactory.GetStrategy(temps[i].PlanType);
                
                if (strategy == null)
                {
                    // Unknown plan type - no interest applied
                    continue;
                }

                var interest = strategy.CalculateInterest(temps[i]);
                temps[i].Balance += Math.Round(interest, 2);
            }
        }
    }
}
