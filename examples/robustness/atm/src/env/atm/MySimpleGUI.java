package atm;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.tools.GUIArtifact;

public class MySimpleGUI extends GUIArtifact {

    private MyFrame frame;

    public void setup() {
        frame = new MyFrame();

        linkActionEventToOp(frame.okButton, "ok");
        linkKeyStrokeToOp(frame.text, "ENTER", "updateText");
        linkWindowClosingEventToOp(frame, "closed");

        defineObsProperty("value", getValue());
        frame.setVisible(true);
    }

    @INTERNAL_OPERATION
    void ok(ActionEvent ev) {
        signal("ok");
    }

    @INTERNAL_OPERATION
    void closed(WindowEvent ev) {
        signal("closed");
    }

    @INTERNAL_OPERATION
    void updateText(ActionEvent ev) {
        getObsProperty("value").updateValue(getValue());
    }

    @OPERATION
    void setValue(int value) {
        frame.setText("" + value);
        getObsProperty("value").updateValue(getValue());
    }

    private int getValue() {
        return Integer.parseInt(frame.getText());
    }

    class MyFrame extends JFrame {

        private JButton okButton;
        private JTextField text;

        public MyFrame() {
            setTitle("Simple GUI ");
            setSize(200, 100);
            JPanel panel = new JPanel();
            setContentPane(panel);
            okButton = new JButton("ok");
            okButton.setSize(80, 50);
            text = new JTextField(10);
            text.setText("0");
            text.setEditable(false);
            panel.add(text);
            panel.add(okButton);
        }

        public String getText() {
            return text.getText();
        }

        public void setText(String s) {
            text.setText(s);
        }
    }
}
