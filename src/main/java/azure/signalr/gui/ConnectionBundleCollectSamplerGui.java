package azure.signalr.gui;

import azure.signalr.sampler.ConnectionBundleCollectSampler;
import org.apache.jmeter.testelement.TestElement;


public class ConnectionBundleCollectSamplerGui extends ConnectionBundleSamplerBaseGui {

    @Override
    public String GuiName() {
        return "SignalRConnectionBundleCollect";
    }

    @Override
    public TestElement getTestElement() {
        return new ConnectionBundleCollectSampler();
    }
}
