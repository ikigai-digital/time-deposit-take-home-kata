using Microsoft.EntityFrameworkCore;
using time_deposit_kata_net;
using time_deposit_kata_net.Application;
using time_deposit_kata_net.Infrastructure;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure database - skip in test environment as tests configure their own
var environment = builder.Environment.EnvironmentName;
if (environment != "Testing")
{
    var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") 
        ?? "Host=localhost;Database=timedeposits;Username=postgres;Password=postgres";

    builder.Services.AddDbContext<TimeDepositDbContext>(options =>
        options.UseNpgsql(connectionString));
}

builder.Services.AddScoped<ITimeDepositRepository, TimeDepositRepository>();
builder.Services.AddScoped<ITimeDepositService, TimeDepositService>();
builder.Services.AddScoped<TimeDepositCalculator>();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

// Ensure database is created - only when not in test mode
if (!app.Environment.IsEnvironment("Testing"))
{
    using (var scope = app.Services.CreateScope())
    {
        var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
        context.Database.EnsureCreated();
    }
}

app.Run();

public partial class Program { }
