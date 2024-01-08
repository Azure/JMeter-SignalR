package azure.signalr;

import com.microsoft.signalr.HttpHubConnectionBuilder;
import com.microsoft.signalr.HubConnection;
import io.reactivex.rxjava3.core.Completable;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionBundle {
    private static final Logger log = LoggerFactory.getLogger(ConnectionBundle.class);
    private static final String InstanceUuid = UUID.randomUUID().toString();
    private static final AtomicInteger connectionBundleCount = new AtomicInteger(0);
    private final int connectionCount;
    private final HubConnection[] hubConnections;
    private final Integer[] groupIds;
    private final String connectionBundleId;
    private final Object lock = new Object();
    private final AtomicBoolean started = new AtomicBoolean();
    private List<SampleResult> sampleResults = new LinkedList<>();
    private int startIntervalInMillionSeconds = 20;

    public ConnectionBundle(HttpHubConnectionBuilder hubConnectionBuilder, int connectionCount, int groupSize) {
        if (connectionCount <= 0) {
            throw new IllegalArgumentException("Connection count must be greater than 0.");
        }
        if (groupSize < 0) {
            throw new IllegalArgumentException("Group size cannot be negative.");
        }
        this.connectionCount = connectionCount;

        // assign a ConnectionBundle ID
        int index = connectionBundleCount.getAndIncrement();
        connectionBundleId = "-cb-" + index;

        // create hubConnections
        hubConnections = new HubConnection[connectionCount];
        for (int i = 0; i < connectionCount; i++) {
            hubConnections[i] = hubConnectionBuilder.build();
        }

        ArrayList<Integer> tmp = new ArrayList<Integer>(connectionCount);
        for (int i = 0; i < connectionCount; i++) {
            tmp.add(groupSize == 0 ? 0 : i / groupSize);
        }
        Collections.shuffle(tmp);
        groupIds = tmp.toArray(new Integer[0]);
    }

    public SampleResult Start() {
        if (!started.compareAndSet(false, true)) {
            log.warn("ConnectionBundle is already started");
            return null;
        }
        log.info("ConnectionBundle start");
        SampleResult connectionBundleSampleResult = new SampleResult();
        connectionBundleSampleResult.sampleStart();
        connectionBundleSampleResult.setSampleLabel("Batch Connect");
        Completable[] completables = new Completable[connectionCount];
        AtomicBoolean success = new AtomicBoolean(false);
        for (int i = 0; i < connectionCount; i++) {
            try {
                Thread.sleep(startIntervalInMillionSeconds);
            } catch (InterruptedException e) {
                log.warn(e.toString());
            }
            completables[i] = hubConnections[i].start();
            int index = i;
            SampleResult subResult = new SampleResult();
            subResult.sampleStart();
            subResult.setSampleLabel("Connect");
            completables[i].subscribe(() -> {
                log.info("Hub connection " + index + " started successfully");
                success.set(true);
                subResult.sampleEnd();
                subResult.setSuccessful(true);
                synchronized (lock) {
                    connectionBundleSampleResult.storeSubResult(subResult, false);
                }
            }, error -> {
                log.error("Hub connection " + index + " failed to start: " + error);
                subResult.sampleEnd();
                subResult.setSuccessful(false);
                synchronized (lock) {
                    connectionBundleSampleResult.storeSubResult(subResult, false);
                }
            });
        }

        try {
            Completable.mergeArrayDelayError(completables)
                    .blockingAwait();
            log.info("ConnectionBundle started successfully");
        } catch (Exception e) {
            log.error("ConnectionBundle failed to start: " + e);
        }

        connectionBundleSampleResult.setSuccessful(success.get());
        connectionBundleSampleResult.sampleEnd();
        return connectionBundleSampleResult;
    }

    public void AddResult(SampleResult sr) {
        synchronized (lock) {
            // 1 sampleResult is about 500B, 1,000,000 sampleResults is about 500M
            if (sampleResults.size() > 1000000) {
                log.error("sampleResults size is greater than 1000000, clear it");
                sampleResults.clear();
            }
            sampleResults.add(sr);
        }
    }

    public void SetStartIntervalInMillionSeconds(int startIntervalInMillionSeconds) {
        if (startIntervalInMillionSeconds < 0) {
            throw new IllegalArgumentException("startIntervalInMillionSeconds must be greater or equal to 0.");
        }
        this.startIntervalInMillionSeconds = startIntervalInMillionSeconds;
    }

    public SampleResult Collect() {
        SampleResult collectSampleResult = new SampleResult();
        collectSampleResult.sampleStart();
        collectSampleResult.setSampleLabel("Collect Metrics");
        List<SampleResult> tmp;
        synchronized (lock) {
            tmp = sampleResults;
            sampleResults = new LinkedList<>();
        }
        for (SampleResult sr : tmp) {
            collectSampleResult.storeSubResult(sr, false);
        }
        collectSampleResult.sampleEnd();
        collectSampleResult.setSuccessful(true);
        return collectSampleResult;
    }

    public int GetConnectionCount() {
        return connectionCount;
    }

    public HubConnection GetHubConnection(int index) {
        if (index < 0 || index >= connectionCount) {
            throw new IndexOutOfBoundsException("ConnectionBundle length is " + connectionCount + " but require " + index);
        }
        return hubConnections[index];
    }

    public String GetGroupName(int index) {
        if (index < 0 || index >= connectionCount) {
            throw new IndexOutOfBoundsException("ConnectionBundle length is " + connectionCount + " but require " + index);
        }
        return InstanceUuid + connectionBundleId + "-" + groupIds[index];
    }
}
