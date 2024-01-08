package azure.signalr.gui;

import azure.signalr.sampler.ConnectionBundleSendSampler;
import org.apache.jmeter.testelement.TestElement;

public class ConnectionBundleSendSamplerGui extends ConnectionBundleSamplerBaseGui {
    @Override
    public String GuiName() {
        return "SignalRConnectionBundleSend";
    }

    @Override
    public TestElement getTestElement() {
        return new ConnectionBundleSendSampler();
    }
}
