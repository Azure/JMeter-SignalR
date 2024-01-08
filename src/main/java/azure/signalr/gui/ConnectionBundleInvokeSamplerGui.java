package azure.signalr.gui;

import azure.signalr.sampler.ConnectionBundleInvokeSampler;
import org.apache.jmeter.testelement.TestElement;

public class ConnectionBundleInvokeSamplerGui extends ConnectionBundleSamplerBaseGui {
    @Override
    public String GuiName() {
        return "SignalRConnectionBundleInvoke";
    }

    @Override
    public TestElement getTestElement() {
        return new ConnectionBundleInvokeSampler();
    }
}
