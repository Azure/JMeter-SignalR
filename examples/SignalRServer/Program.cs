using System.Security.Cryptography;
using SignalRServer.Hubs;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddSignalR(
        // Allow testing large messages. The default is 32KB.
        options => options.MaximumReceiveMessageSize = 1 << 30
        )
    // Add Azure SignalR Service: https://learn.microsoft.com/en-us/azure/azure-signalr/signalr-overview
    // .AddAzureSignalR( "xxx")
    ;

var app = builder.Build();

app.UseAuthorization();

app.MapControllers();
app.MapHub<BenchHub>("/bench");

app.Run();