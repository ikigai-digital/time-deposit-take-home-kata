using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace time_deposit_kata_net
{
    public class Withdrawal
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [ForeignKey(nameof(TimeDeposit))]
        public int TimeDepositId { get; set; }

        public TimeDeposit TimeDeposit { get; set; } = null!;

        [Required]
        public double Amount { get; set; }

        [Required]
        public DateTime Date { get; set; }
    }
}
