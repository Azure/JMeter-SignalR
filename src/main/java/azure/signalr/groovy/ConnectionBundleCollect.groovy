package azure.signalr.groovy

import azure.signalr.SignalRUtil
import azure.signalr.sampler.ConnectionBundleCollectSampler
import org.apache.jmeter.samplers.SampleResult as SR
import org.slf4j.Logger
import org.slf4j.LoggerFactory

Logger log = LoggerFactory.getLogger(ConnectionBundleCollectSampler.class);

log.debug("Collecting sample result");
SR sr = SignalRUtil.GetThreadLocalConnectionBundle().Collect();
log.debug("Sample result collected");

SignalRUtil.FlushSampleResult(sr, SampleResult)
