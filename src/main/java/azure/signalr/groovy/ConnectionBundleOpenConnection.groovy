package azure.signalr.groovy

import azure.signalr.ConnectionBundle
import azure.signalr.SignalRUtil
import azure.signalr.sampler.ConnectionBundleOpenConnectionSampler
import com.microsoft.signalr.HttpHubConnectionBuilder
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import org.apache.jmeter.samplers.SampleResult as SR
import org.slf4j.Logger
import org.slf4j.LoggerFactory

Logger log = LoggerFactory.getLogger(ConnectionBundleOpenConnectionSampler.class);

// get webAppUrl from environment variable
String webAppUrl = System.getenv("webAppUrl");
if (webAppUrl == null) {
    webAppUrl = "http://localhost:5000/bench";
}
log.info("WebAppUrl: " + webAppUrl);

// get connectionCountTotal from environment variable
String connectionCountTotalStr = System.getenv("connectionCountTotal");
if (connectionCountTotalStr == null) {
    connectionCountTotalStr = "1";
}
log.info("connectionCountTotal: " + connectionCountTotalStr);
int connectionCountTotal = Integer.parseInt(connectionCountTotalStr);
int totalThreads = ctx.getThreadGroup().getNumberOfThreads()
log.info("Total threads configured in current thread group: " + totalThreads)
int connectionCountPerBundle = connectionCountTotal / totalThreads
if (connectionCountPerBundle <= 0) {
    connectionCountPerBundle = 1
}
log.info("connectionCountPerBundle: " + connectionCountPerBundle);

// get group size from environment variable
String groupSizeStr = System.getenv("groupSize");
if (groupSizeStr == null) {
    // each connection will belong to its own group by default
    groupSizeStr = "1";
}
log.info("groupSize: " + groupSizeStr);
int groupSize = Integer.parseInt(groupSizeStr);

// Create a connection builder
HttpHubConnectionBuilder connectionBuilder = HubConnectionBuilder.create(webAppUrl);

// Pass in connectionBuilder, connectionCount, groupSize to create a connectionBundle
// The connectionCountPerBundle is the number of connections in this connectionBundle
// The group size is the number of connections that will be assign the same group name. The group name has a prefix of the JMeter instance and bundle index and
ConnectionBundle connectionBundle = new ConnectionBundle(connectionBuilder, connectionCountPerBundle, groupSize);

SignalRUtil.SetThreadLocalHubConnectionBundle(connectionBundle);

//setup callback on every hub connection
for (int i = 0; i < connectionBundle.GetConnectionCount(); i++) {
    HubConnection hubConnection = connectionBundle.GetHubConnection(i);
    final String callBackMethodName = "Receive";
    hubConnection.on(callBackMethodName, (Long startTime, String paylod) -> {
        long endTime = System.currentTimeMillis();
        log.info(callBackMethodName + " " + paylod.length() + " bytes after " + (endTime-startTime) + " milliseconds");
        SR sr = SR.createTestSample(startTime, endTime);
        sr.setSampleLabel(callBackMethodName + " From Server");
        sr.setSuccessful(true);
        connectionBundle.AddResult(sr);
    }, Long.class, String.class);
}

// start connection bundle
SR sr = connectionBundle.Start();

SignalRUtil.FlushSampleResult(sr, SampleResult);