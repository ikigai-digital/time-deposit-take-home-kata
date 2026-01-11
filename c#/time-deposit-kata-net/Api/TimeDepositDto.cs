namespace time_deposit_kata_net.Api
{
    public class TimeDepositDto
    {
        public int Id { get; set; }
        public string PlanType { get; set; } = string.Empty;
        public double Balance { get; set; }
        public int Days { get; set; }
        public List<WithdrawalDto> Withdrawals { get; set; } = new List<WithdrawalDto>();
    }

    public class WithdrawalDto
    {
        public int Id { get; set; }
        public double Amount { get; set; }
        public DateTime Date { get; set; }
    }
}
