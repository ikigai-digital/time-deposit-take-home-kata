using System.Reflection;
using Microsoft.EntityFrameworkCore;
using time_deposit_kata_net;
using time_deposit_kata_net.Application;
using time_deposit_kata_net.Infrastructure;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    // Include XML comments for Swagger documentation
    var xmlFile = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
    var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlFile);
    if (File.Exists(xmlPath))
    {
        options.IncludeXmlComments(xmlPath);
    }
    
    options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "Time Deposit API",
        Version = "v1",
        Description = "RESTful API for managing time deposits with automatic interest calculation. Supports Basic, Student, and Premium plan types with different interest rates and rules."
    });
});

// Configure database - skip in test environment as tests configure their own
var environment = builder.Environment.EnvironmentName;
if (environment != "Testing")
{
    // Connection string from environment variable (Docker) or configuration file
    var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") 
        ?? Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection")
        ?? "Host=localhost;Database=timedeposits;Username=postgres;Password=postgres";

    builder.Services.AddDbContext<TimeDepositDbContext>(options =>
        options.UseNpgsql(connectionString, npgsqlOptions =>
            npgsqlOptions.EnableRetryOnFailure(
                maxRetryCount: 5,
                maxRetryDelay: TimeSpan.FromSeconds(30),
                errorCodesToAdd: null)));
}

builder.Services.AddScoped<ITimeDepositRepository, TimeDepositRepository>();
builder.Services.AddScoped<ITimeDepositService, TimeDepositService>();
builder.Services.AddScoped<TimeDepositCalculator>();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "Time Deposit API v1");
        c.RoutePrefix = "swagger";
    });
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

// Apply database migrations and seed data - only when not in test mode
if (!app.Environment.IsEnvironment("Testing"))
{
    using (var scope = app.Services.CreateScope())
    {
        var context = scope.ServiceProvider.GetRequiredService<TimeDepositDbContext>();
        
        // Retry database connection with exponential backoff
        var maxRetries = 10;
        var delay = TimeSpan.FromSeconds(2);
        for (int i = 0; i < maxRetries; i++)
        {
            try
            {
                context.Database.Migrate();
                await DatabaseSeeder.SeedAsync(context);
                break;
            }
            catch (Exception) when (i < maxRetries - 1)
            {
                await Task.Delay(delay);
                delay = TimeSpan.FromSeconds(Math.Min(delay.TotalSeconds * 2, 30));
            }
        }
    }
}

await app.RunAsync();

public partial class Program { }
