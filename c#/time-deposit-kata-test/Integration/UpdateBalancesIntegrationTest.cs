using System.Net;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using NUnit.Framework;
using Testcontainers.PostgreSql;
using time_deposit_kata_net;
using time_deposit_kata_net.Infrastructure;

namespace time_deposit_kata_test.Integration
{
    /// <summary>
    /// Integration tests using Testcontainers with a real PostgreSQL database.
    /// Demonstrates proper integration testing against actual database infrastructure.
    /// Requires Docker to be running - tests will be skipped if Docker is unavailable.
    /// </summary>
    [TestFixture]
    [Category("Integration")]
    public class UpdateBalancesIntegrationTest
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
                // Start PostgreSQL container with a custom database name
                _postgresContainer = new PostgreSqlBuilder()
                    .WithImage("postgres:16-alpine")
                    .WithDatabase("testdb")
                    .Build();

                await _postgresContainer.StartAsync();
                _dockerAvailable = true;
            }
            catch (ArgumentException)
            {
                // Docker is not available - tests will be skipped
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
                        // Remove existing DbContext registrations
                        var descriptor = services.SingleOrDefault(
                            d => d.ServiceType == typeof(DbContextOptions<TimeDepositDbContext>));
                        if (descriptor != null)
                            services.Remove(descriptor);

                        // Add PostgreSQL using Testcontainers connection string
                        services.AddDbContext<TimeDepositDbContext>(options =>
                        {
                            options.UseNpgsql(_postgresContainer!.GetConnectionString());
                        });
                    });
                });

            _client = _factory.CreateClient();

            // Ensure database schema exists and clear data for each test
            using var scope = _factory.Services.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
            await context.Database.EnsureCreatedAsync();
            
            // Clear existing data for test isolation
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
        public async Task UpdateBalances_WithDepositsInDatabase_ReturnsOkAndAppliesInterest()
        {
            // Arrange: Seed the database with a time deposit
            using (var scope = _factory!.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                context.TimeDeposits.Add(new TimeDeposit
                {
                    Id = 1,
                    PlanType = "basic",
                    Balance = 1200.00,
                    Days = 45
                });
                await context.SaveChangesAsync();
            }

            // Act: Call the update balances endpoint
            var response = await _client!.PostAsync("/api/time-deposits/update-balances", null);

            // Assert: Verify response and updated balance
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);

            using (var scope = _factory.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                var deposit = await context.TimeDeposits.FirstOrDefaultAsync(d => d.Id == 1);

                Assert.IsNotNull(deposit);
                Assert.AreEqual(1201.00, deposit!.Balance);
            }
        }

        [Test]
        public async Task UpdateBalances_WithEmptyDatabase_ReturnsOk()
        {
            // Act: Call endpoint with no deposits in database
            var response = await _client!.PostAsync("/api/time-deposits/update-balances", null);

            // Assert: Should succeed even with no data
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
        }

        [Test]
        public async Task UpdateBalances_WithMultiplePlanTypes_AppliesCorrectInterest()
        {
            // Arrange: Seed with different plan types
            using (var scope = _factory!.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                context.TimeDeposits.AddRange(
                    new TimeDeposit { Id = 1, PlanType = "basic", Balance = 1200.00, Days = 45 },
                    new TimeDeposit { Id = 2, PlanType = "student", Balance = 1200.00, Days = 100 },
                    new TimeDeposit { Id = 3, PlanType = "premium", Balance = 1200.00, Days = 60 }
                );
                await context.SaveChangesAsync();
            }

            // Act
            var response = await _client!.PostAsync("/api/time-deposits/update-balances", null);

            // Assert
            Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);

            using (var scope = _factory.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
                var deposits = await context.TimeDeposits.OrderBy(d => d.Id).ToListAsync();

                Assert.AreEqual(3, deposits.Count);
                Assert.AreEqual(1201.00, deposits[0].Balance); // Basic: 1% / 12
                Assert.AreEqual(1203.00, deposits[1].Balance); // Student: 3% / 12
                Assert.AreEqual(1205.00, deposits[2].Balance); // Premium: 5% / 12
            }
        }
    }
}
