namespace time_deposit_kata_net.Application
{
    public class TimeDepositResponse
    {
        public int Id { get; set; }
        public string PlanType { get; set; } = string.Empty;
        public double Balance { get; set; }
        public int Days { get; set; }
        public List<WithdrawalResponse> Withdrawals { get; set; } = new List<WithdrawalResponse>();
    }

    public class WithdrawalResponse
    {
        public int Id { get; set; }
        public double Amount { get; set; }
        public DateTime Date { get; set; }
    }
}
