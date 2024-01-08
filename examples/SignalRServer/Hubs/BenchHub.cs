// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

using Microsoft.AspNetCore.SignalR;

namespace SignalRServer.Hubs
{
    public class BenchHub : Hub
    {
        private ILogger<BenchHub> _logger;
        public BenchHub(ILogger<BenchHub> logger)
        {
            _logger = logger;
        }

        public void Echo(long ticks, string payload)
        {
            Clients.Client(Context.ConnectionId).SendAsync("Receive", ticks, payload);
        }

        public void SendToConnection(string connectionId, long ticks, string payload)
        {
            Clients.Client(connectionId).SendAsync("Receive", ticks, payload);
        }

        public void Broadcast(long ticks, string payload)
        {
            Clients.All.SendAsync("Receive", ticks, payload);
        }

        #region Group
        public async Task JoinGroup(string group)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, group);
        }

        public async Task LeaveGroup(string group)
        {
            await Groups.RemoveFromGroupAsync(Context.ConnectionId, group);
        }

        public void SendToGroup(string group, long ticks, string payload)
        {
            Clients.Group(group).SendAsync("Receive", ticks, payload);
        }
        #endregion
    }
}