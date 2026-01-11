using System.Collections.Generic;
using NUnit.Framework;
using time_deposit_kata_net;

namespace time_deposit_kata_test.Unit
{
    public class TimeDepositCalculatorTest
    {
        private TimeDepositCalculator _calculator = null!;

        [SetUp]
        public void Setup()
        {
            _calculator = new TimeDepositCalculator();
        }

        [Test]
        public void UpdateBalance_BasicPlan_Applies1PercentInterestAfter30Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "basic",
                    Balance = 1200.00,
                    Days = 45
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            // Basic: 1% annual / 12 months = 0.0833% monthly
            // 1200 * 0.01 / 12 = 1.00
            Assert.AreEqual(1201.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_BasicPlan_NoInterestWithin30Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "basic",
                    Balance = 1200.00,
                    Days = 25
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            Assert.AreEqual(1200.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_StudentPlan_Applies3PercentInterestBetween30And365Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "student",
                    Balance = 1200.00,
                    Days = 100
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            // Student: 3% annual / 12 months
            // 1200 * 0.03 / 12 = 3.00
            Assert.AreEqual(1203.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_StudentPlan_NoInterestAfter1Year()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "student",
                    Balance = 1200.00,
                    Days = 400
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            Assert.AreEqual(1200.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_StudentPlan_NoInterestWithin30Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "student",
                    Balance = 1200.00,
                    Days = 25
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            Assert.AreEqual(1200.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_PremiumPlan_Applies5PercentInterestAfter45Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "premium",
                    Balance = 1200.00,
                    Days = 60
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            // Premium: 5% annual / 12 months
            // 1200 * 0.05 / 12 = 5.00
            Assert.AreEqual(1205.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_PremiumPlan_NoInterestWithin45Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "premium",
                    Balance = 1200.00,
                    Days = 40
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            Assert.AreEqual(1200.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_PremiumPlan_NoInterestWithin30Days()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "premium",
                    Balance = 1200.00,
                    Days = 25
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            Assert.AreEqual(1200.00, deposits[0].Balance);
        }

        [Test]
        public void UpdateBalance_MultipleDeposits_AppliesCorrectInterestToEach()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit { Id = 1, PlanType = "basic", Balance = 1200.00, Days = 45 },
                new TimeDeposit { Id = 2, PlanType = "student", Balance = 1200.00, Days = 100 },
                new TimeDeposit { Id = 3, PlanType = "premium", Balance = 1200.00, Days = 60 }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            Assert.AreEqual(1201.00, deposits[0].Balance); // Basic: +1.00
            Assert.AreEqual(1203.00, deposits[1].Balance); // Student: +3.00
            Assert.AreEqual(1205.00, deposits[2].Balance); // Premium: +5.00
        }

        [Test]
        public void UpdateBalance_InterestRoundedTo2DecimalPlaces()
        {
            // Arrange
            var deposits = new List<TimeDeposit>
            {
                new TimeDeposit
                {
                    Id = 1,
                    PlanType = "basic",
                    Balance = 1000.00,
                    Days = 45
                }
            };

            // Act
            _calculator.UpdateBalance(deposits);

            // Assert
            // 1000 * 0.01 / 12 = 0.8333... rounded to 0.83
            Assert.AreEqual(1000.83, deposits[0].Balance);
        }
    }
}
