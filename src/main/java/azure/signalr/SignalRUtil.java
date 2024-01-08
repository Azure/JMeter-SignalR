package azure.signalr;

import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SignalRUtil {

    private static final int cacheLimit = 1 << 28;
    private static final Logger log = LoggerFactory.getLogger(SignalRUtil.class);
    private static final ThreadLocal<ConnectionBundle> hubConnectionBundleThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<Integer, String> payloadCache = new ConcurrentHashMap<>();
    private static final Object lock = new Object();
    private static int cacheSize;

    // Used to pass the sample result to the JSR223 built-in SampleResult
    public static void FlushSampleResult(SampleResult from, SampleResult to) {
        if (from == null) {
            log.warn("SampleResult is null");
            return;
        }
        to.setEndTime(from.getEndTime());
        to.setSuccessful(from.isSuccessful());
        to.setResponseCode(from.getResponseCode());
        to.setResponseMessage(from.getResponseMessage());
        to.setSampleLabel(from.getSampleLabel());
        for (SampleResult sr : from.getSubResults()) {
            to.storeSubResult(sr, false);
        }
    }

    public static ConnectionBundle GetThreadLocalConnectionBundle() {
        ConnectionBundle connectionBundle = hubConnectionBundleThreadLocal.get();
        if (connectionBundle == null) {
            throw new RuntimeException("ConnectionBundle is not set");
        }
        return connectionBundle;
    }

    public static void SetThreadLocalHubConnectionBundle(ConnectionBundle connectionBundle) {
        if (hubConnectionBundleThreadLocal.get() != null) {
            throw new RuntimeException("ConnectionBundle is already set");
        }
        hubConnectionBundleThreadLocal.set(connectionBundle);
    }

    public static String GetPayloadInBytes(Integer sizeInBytes) {
        String payload = payloadCache.get(sizeInBytes);
        if (payload != null) {
            return payload;
        }
        synchronized (lock) {
            payload = payloadCache.get(sizeInBytes);
            if (payload == null) {
                // Cache at most 256MB payload.
                if (cacheSize > cacheLimit) {
                    payloadCache.clear();
                    cacheSize = 0;
                    log.warn("Payload cache is cleared");
                }
                cacheSize += sizeInBytes;
                payload = generateRandomString(sizeInBytes);
                payloadCache.put(sizeInBytes, payload);
            }
            return payload;
        }
    }

    private static String generateRandomString(int sizeInBytes) {
        final String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeInBytes);

        while (sb.length() < sizeInBytes) {
            int index = random.nextInt(charSet.length());
            sb.append(charSet.charAt(index));
        }

        return sb.toString();
    }
}
