using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace time_deposit_kata_net
{
    public class TimeDeposit
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public string PlanType { get; set; } = string.Empty;

        [Required]
        public double Balance { get; set; }

        [Required]
        public int Days { get; set; }

        public List<Withdrawal> Withdrawals { get; set; } = new List<Withdrawal>();
    }
}
