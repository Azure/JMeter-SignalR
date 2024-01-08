package azure.signalr.groovy

import azure.signalr.ConnectionBundle
import azure.signalr.SignalRUtil
import azure.signalr.sampler.ConnectionBundleInvokeSampler
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionState
import org.apache.jmeter.samplers.SampleResult as SR
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit

Logger log = LoggerFactory.getLogger(ConnectionBundleInvokeSampler.class);

SR sr = new SR();
sr.sampleStart();
sr.setSampleLabel("Batch JoinGroup");

log.debug("Start invoking JoinGroup");
// This(JoinGroup) is an example of invoke method
ConnectionBundle connectionBundle = SignalRUtil.GetThreadLocalConnectionBundle();
for (int i = 0; i < connectionBundle.GetConnectionCount(); i++) {
    HubConnection hubConnection = connectionBundle.GetHubConnection(i);
    // If the connection is not connected, skip it
    if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
        try {
            String groupName = connectionBundle.GetGroupName(i);
            long startTime = System.currentTimeMillis();
            // This is not blocking.
            connectionBundle.GetHubConnection(i).invoke("JoinGroup", groupName)
                    .timeout(5, TimeUnit.SECONDS)
                    .subscribe(() -> {
                        SR joinSampleResult = SR.createTestSample(startTime, System.currentTimeMillis());
                        joinSampleResult.setSampleLabel("JoinGroup");
                        joinSampleResult.setSuccessful(true);
                        connectionBundle.AddResult(joinSampleResult);
                    }, throwable -> {
                        log.error(throwable.toString());
                        SR joinSampleResult = SR.createTestSample(startTime, System.currentTimeMillis());
                        joinSampleResult.setSampleLabel("JoinGroup");
                        joinSampleResult.setSuccessful(false);
                        joinSampleResult.setResponseMessage(throwable.toString());
                        connectionBundle.AddResult(joinSampleResult);
                    });

        } catch (Exception e) {
            // Catch the exception to allow the loop to continue
            log.error(e.toString());
        }
    }
}

log.debug("finish invoking LeaveGroup");

sr.sampleEnd();
sr.setSuccessful(true);

SignalRUtil.FlushSampleResult(sr, SampleResult);

