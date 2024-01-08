package azure.signalr.gui;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public abstract class ConnectionBundleSamplerBaseGui extends AbstractSamplerGui {
    public ConnectionBundleSamplerBaseGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return GuiName();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    public abstract String GuiName();

    public abstract TestElement getTestElement();

    @Override
    public TestElement createTestElement() {
        TestElement element = getTestElement();
        configureTestElement(element);  // Essential because it sets some basic JMeter properties (e.g. the link between sampler and gui class)
        return element;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        element.setName(this.getName());
        element.setEnabled(this.isEnabled());
        element.setComment(this.getComment());
    }
}
