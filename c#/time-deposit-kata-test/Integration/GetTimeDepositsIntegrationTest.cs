using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using NUnit.Framework;
using Testcontainers.PostgreSql;
using time_deposit_kata_net;
using time_deposit_kata_net.Api;
using time_deposit_kata_net.Infrastructure;

namespace time_deposit_kata_test.Integration
{
    [TestFixture]
    [Category("Integration")]
    public class GetTimeDepositsIntegrationTest
    {
        private PostgreSqlContainer? _postgresContainer;
        private WebApplicationFactory<Program>? _factory;
        private HttpClient? _client;
        private bool _dockerAvailable;

        [OneTimeSetUp]
        public async Task OneTimeSetUp()
        {
            try
            {
                _postgresContainer = new PostgreSqlBuilder()
                    .WithImage("postgres:16-alpine")
                    .WithDatabase("testdb")
                    .Build();

                await _postgresContainer.StartAsync();
                _dockerAvailable = true;
            }
            catch (ArgumentException)
            {
                _dockerAvailable = false;
            }
        }

        [OneTimeTearDown]
        public async Task OneTimeTearDown()
        {
            if (_postgresContainer != null)
            {
                await _postgresContainer.DisposeAsync();
            }
        }

        [SetUp]
        public async Task SetUp()
        {
            if (!_dockerAvailable)
            {
                Assert.Ignore("Docker is not available. Skipping integration tests.");
                return;
            }

            _factory = new WebApplicationFactory<Program>()
                .WithWebHostBuilder(builder =>
                {
                    builder.UseEnvironment("Testing");

                    builder.ConfigureServices(services =>
                    {
                        var descriptor = services.SingleOrDefault(
                            d => d.ServiceType == typeof(DbContextOptions<TimeDepositDbContext>));
                        if (descriptor != null)
                            services.Remove(descriptor);

                        services.AddDbContext<TimeDepositDbContext>(options =>
                        {
                            options.UseNpgsql(_postgresContainer!.GetConnectionString());
                        });
                    });
                });

            _client = _factory.CreateClient();

            using var scope = _factory.Services.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
            await context.Database.EnsureCreatedAsync();
            
            context.Withdrawals.RemoveRange(context.Withdrawals);
            context.TimeDeposits.RemoveRange(context.TimeDeposits);
            await context.SaveChangesAsync();
        }

        [TearDown]
        public void TearDown()
        {
            _client?.Dispose();
            _factory?.Dispose();
        }

        [Test]
        public async Task GetAll_WithEmptyDatabase_ReturnsEmptyList()
        {
            // Act
            var response = await _client!.GetAsync("/api/time-deposits");

            // Assert
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
            var deposits = await response.Content.ReadFromJsonAsync<List<TimeDepositDto>>();
            Assert.IsNotNull(deposits);
            Assert.AreEqual(0, deposits!.Count);
        }

        [Test]
        public async Task GetAll_WithDeposits_ReturnsAllDeposits()
        {
            // Arrange
            using (var scope = _factory!.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                context.TimeDeposits.AddRange(
                    new TimeDeposit { Id = 1, PlanType = "basic", Balance = 1000.00, Days = 30 },
                    new TimeDeposit { Id = 2, PlanType = "student", Balance = 2000.00, Days = 60 },
                    new TimeDeposit { Id = 3, PlanType = "premium", Balance = 3000.00, Days = 90 }
                );
                await context.SaveChangesAsync();
            }

            // Act
            var response = await _client!.GetAsync("/api/time-deposits");

            // Assert
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
            var deposits = await response.Content.ReadFromJsonAsync<List<TimeDepositDto>>();
            
            Assert.IsNotNull(deposits);
            Assert.AreEqual(3, deposits!.Count);
            
            Assert.AreEqual(1, deposits[0].Id);
            Assert.AreEqual("basic", deposits[0].PlanType);
            Assert.AreEqual(1000.00, deposits[0].Balance);
            Assert.AreEqual(30, deposits[0].Days);
        }

        [Test]
        public async Task GetAll_WithWithdrawals_ReturnsDepositsWithWithdrawals()
        {
            // Arrange
            using (var scope = _factory!.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                var deposit = new TimeDeposit 
                { 
                    Id = 1, 
                    PlanType = "basic", 
                    Balance = 1000.00, 
                    Days = 30,
                    Withdrawals = new List<Withdrawal>
                    {
                        new Withdrawal { Id = 1, Amount = 100.00, Date = new DateTime(2025, 1, 15, 0, 0, 0, DateTimeKind.Utc) },
                        new Withdrawal { Id = 2, Amount = 200.00, Date = new DateTime(2025, 2, 20, 0, 0, 0, DateTimeKind.Utc) }
                    }
                };
                context.TimeDeposits.Add(deposit);
                await context.SaveChangesAsync();
            }

            // Act
            var response = await _client!.GetAsync("/api/time-deposits");

            // Assert
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
            var deposits = await response.Content.ReadFromJsonAsync<List<TimeDepositDto>>();
            
            Assert.IsNotNull(deposits);
            Assert.AreEqual(1, deposits!.Count);
            Assert.AreEqual(2, deposits[0].Withdrawals.Count);
            Assert.AreEqual(100.00, deposits[0].Withdrawals[0].Amount);
            Assert.AreEqual(200.00, deposits[0].Withdrawals[1].Amount);
        }

        [Test]
        public async Task GetAll_ReturnsCorrectSchema()
        {
            // Arrange
            using (var scope = _factory!.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                var deposit = new TimeDeposit 
                { 
                    Id = 1, 
                    PlanType = "premium", 
                    Balance = 5000.00, 
                    Days = 120,
                    Withdrawals = new List<Withdrawal>
                    {
                        new Withdrawal { Id = 1, Amount = 500.00, Date = new DateTime(2025, 3, 10, 0, 0, 0, DateTimeKind.Utc) }
                    }
                };
                context.TimeDeposits.Add(deposit);
                await context.SaveChangesAsync();
            }

            // Act
            var response = await _client!.GetAsync("/api/time-deposits");

            // Assert
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
            var deposits = await response.Content.ReadFromJsonAsync<List<TimeDepositDto>>();
            
            Assert.IsNotNull(deposits);
            var dto = deposits![0];
            
            // Verify all required schema fields are present
            Assert.AreEqual(1, dto.Id);
            Assert.AreEqual("premium", dto.PlanType);
            Assert.AreEqual(5000.00, dto.Balance);
            Assert.AreEqual(120, dto.Days);
            Assert.IsNotNull(dto.Withdrawals);
            Assert.AreEqual(1, dto.Withdrawals.Count);
            Assert.AreEqual(500.00, dto.Withdrawals[0].Amount);
        }
    }
}
