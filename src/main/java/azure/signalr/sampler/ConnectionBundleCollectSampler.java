package azure.signalr.sampler;

import azure.signalr.SignalRUtil;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionBundleCollectSampler extends AbstractSampler {
    private static final Logger log = LoggerFactory.getLogger(ConnectionBundleCollectSampler.class);

    @Override
    public SampleResult sample(Entry e) {

        log.info("Collecting sample result");
        SampleResult collectSampleResult = SignalRUtil.GetThreadLocalConnectionBundle().Collect();
        log.info("Sample result collected");

        return collectSampleResult;
    }
}
