using System;

namespace time_deposit_kata_net.Application
{
    /// <summary>
    /// Response model representing a time deposit with its details and withdrawals.
    /// </summary>
    public class TimeDepositResponse
    {
        /// <summary>
        /// Unique identifier for the time deposit.
        /// </summary>
        public int Id { get; set; }

        /// <summary>
        /// The plan type of the time deposit (basic, student, or premium).
        /// </summary>
        public string PlanType { get; set; } = string.Empty;

        /// <summary>
        /// Current balance of the time deposit.
        /// </summary>
        public double Balance { get; set; }

        /// <summary>
        /// Number of days the time deposit has been active.
        /// </summary>
        public int Days { get; set; }

        /// <summary>
        /// List of withdrawals associated with this time deposit.
        /// </summary>
        public List<WithdrawalResponse> Withdrawals { get; set; } = new List<WithdrawalResponse>();
    }

    /// <summary>
    /// Response model representing a withdrawal transaction.
    /// </summary>
    public class WithdrawalResponse
    {
        /// <summary>
        /// Unique identifier for the withdrawal.
        /// </summary>
        public int Id { get; set; }

        /// <summary>
        /// Amount withdrawn from the time deposit.
        /// </summary>
        public double Amount { get; set; }

        /// <summary>
        /// Date and time when the withdrawal was made (UTC).
        /// </summary>
        public DateTime Date { get; set; }
    }
}
