package azure.signalr.sampler;

import azure.signalr.ConnectionBundle;
import azure.signalr.SignalRUtil;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionState;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionBundleSendSampler extends AbstractSampler {
    private static final Logger log = LoggerFactory.getLogger(ConnectionBundleSendSampler.class);

    @Override
    public SampleResult sample(Entry entry) {

        SampleResult sampleResult = new SampleResult();
        sampleResult.setSampleLabel("Batch Send");
        sampleResult.sampleStart();

        // get group size from environment variable
        String payloadSizeInBytesStr = System.getenv("payloadSizeInBytes");
        if (payloadSizeInBytesStr == null) {
            payloadSizeInBytesStr = "2048";
        }
        log.info("payloadSizeInBytes: " + payloadSizeInBytesStr);
        int payloadSizeInBytes = Integer.parseInt(payloadSizeInBytesStr);

        String sendDelayInMilliSecondsStr = System.getenv("sendDelayInMilliSeconds");
        if (sendDelayInMilliSecondsStr == null) {
            sendDelayInMilliSecondsStr = "1000";
        }
        log.info("sendDelayInMilliSeconds: " + sendDelayInMilliSecondsStr);
        int sendDelayInMilliSeconds = Integer.parseInt(sendDelayInMilliSecondsStr);

        log.debug("Start sending message");
        ConnectionBundle connectionBundle = SignalRUtil.GetThreadLocalConnectionBundle();
        for (int i = 0; i < connectionBundle.GetConnectionCount(); i++) {
            // Send method just flush the content to the tcp buffer.
            // It's very fast thus normally no need to track its time cost.
            HubConnection hubConnection = connectionBundle.GetHubConnection(i);
            // If the connection is not connected, skip it
            if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                try {
                    String payload = SignalRUtil.GetPayloadInBytes(payloadSizeInBytes);
                    connectionBundle.GetHubConnection(i).send("SendToGroup", connectionBundle.GetGroupName(i), System.currentTimeMillis(), payload);
                    sampleResult.setSuccessful(true);
                } catch (Exception e) {
                    // Catch the exception to allow the loop to continue
                    log.error(e.toString());
                }
            }
        }

        sampleResult.sampleEnd();
        log.debug("finish sending message");
        try {
            Thread.sleep(sendDelayInMilliSeconds);
        } catch (InterruptedException e) {
            log.warn(e.toString());
        }
        return sampleResult;
    }
}
