package azure.signalr.gui;

import azure.signalr.sampler.ConnectionBundleOpenConnectionSampler;
import org.apache.jmeter.testelement.TestElement;

public class ConnectionBundleOpenConnectionSamplerGui extends ConnectionBundleSamplerBaseGui {
    @Override
    public String GuiName() {
        return "SignalRConnectionBundleOpenConnection";
    }

    @Override
    public TestElement getTestElement() {
        return new ConnectionBundleOpenConnectionSampler();
    }
}
