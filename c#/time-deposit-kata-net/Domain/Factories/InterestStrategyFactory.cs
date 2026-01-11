using System.Collections.Generic;
using time_deposit_kata_net.InterestStrategies;

namespace time_deposit_kata_net
{
    /// <summary>
    /// Factory for creating interest calculation strategies based on plan type.
    /// Extensible: Add new plan types by registering new strategies here.
    /// </summary>
    public class InterestStrategyFactory
    {
        private static readonly Dictionary<string, IInterestCalculationStrategy> _strategies = new()
        {
            { "basic", new BasicPlanInterestStrategy() },
            { "student", new StudentPlanInterestStrategy() },
            { "premium", new PremiumPlanInterestStrategy() }
        };

        /// <summary>
        /// Gets the appropriate interest calculation strategy for a given plan type.
        /// </summary>
        /// <param name="planType">The plan type (case-insensitive)</param>
        /// <returns>The interest calculation strategy, or null if plan type is not recognized</returns>
        public static IInterestCalculationStrategy? GetStrategy(string planType)
        {
            if (string.IsNullOrWhiteSpace(planType))
            {
                return null;
            }

            return _strategies.TryGetValue(planType.ToLowerInvariant(), out var strategy) 
                ? strategy 
                : null;
        }

        /// <summary>
        /// Registers a new interest calculation strategy for a plan type.
        /// Allows extension without modifying existing code.
        /// </summary>
        /// <param name="planType">The plan type to register</param>
        /// <param name="strategy">The strategy implementation</param>
        public static void RegisterStrategy(string planType, IInterestCalculationStrategy strategy)
        {
            _strategies[planType.ToLowerInvariant()] = strategy;
        }
    }
}
