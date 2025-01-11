package atm;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.tools.GUIArtifact;

public class ATMArtifact extends GUIArtifact {

    private ATMFrame frame;

    private String amountString;
    private int amountInt;

    public void setup() {
        frame = new ATMFrame();

        linkActionEventToOp(frame.okButton, "ok");
        linkKeyStrokeToOp(frame.text, "ENTER", "ok");
        linkWindowClosingEventToOp(frame, "closed");

        frame.setVisible(true);
    }

    @INTERNAL_OPERATION
    void ok(ActionEvent ev) {
        amountString = frame.getText();
        signal("inputReceived");
    }

    @OPERATION
    public void enableInput() {
        frame.enableInput();
    }

    @OPERATION
    public void disableInput() {
        frame.disableInput();
    }

    @OPERATION
    public void getAmountAsString(OpFeedbackParam<String> res) {
        res.set(amountString);
    }

    @OPERATION
    public void parseAmount() {
        if (amountString == null) {
            failed("nan");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = amountString.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = amountString.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    failed("Not a number!","firstNaNIndex", i);

                if (len == 1) // Cannot have lone "+" or "-"
                    failed("nan","firstNaNIndex", i);
                i++;
            }
            multmin = limit / 10;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(amountString.charAt(i++), 10);
                if (digit < 0) {
                    failed("nan","firstNaNIndex", i-1);
                }
                if (result < multmin) {
                    failed("nan","firstNaNIndex", i-1);
                }
                result *= 10;
                if (result < limit + digit) {
                    failed("nan","firstNaNIndex", i-1);
                }
                result -= digit;
            }
        } else {
            failed("nan","firstNaNIndex", -1);
        }
        amountInt = negative ? result : -result;
        defineObsProperty("amountInt", amountInt);
    }
    
    @OPERATION
    public void giveMoney() {
        frame.giveMoney(amountInt);
    }
    
    @OPERATION
    public void finishWithdraw() {
        frame.finish();
        amountString = "";
        amountInt = 0;
    }
    
    @OPERATION
    public void failWithdraw() {
        frame.fail();
    }
    
    @OPERATION
    public void closeATM() {
        frame.dispose();
    }

    class ATMFrame extends JFrame {

        private JLabel label;
        private JButton okButton;
        private JTextField text;
        private JLabel status;

        public ATMFrame() {
            setTitle("JaCaMo ATM ");
            setSize(400, 130);
            JPanel panel = new JPanel();
            setContentPane(panel);
            okButton = new JButton("Send");
            okButton.setSize(80, 50);
            text = new JTextField(20);
            text.setEditable(false);
            label = new JLabel("Amount:");
            status = new JLabel("Status: READY FOR NEXT CLIENT");
            okButton.setEnabled(false);
            panel.add(label);
            panel.add(text);
            panel.add(okButton);
            panel.add(status);
        }

        void enableInput() {
            text.setEditable(true);
            text.requestFocusInWindow();
            text.setText("");
            okButton.setEnabled(true);
            status.setText("Status: INSERT AMOUNT");
        }

        void disableInput() {
            text.setEditable(false);
            okButton.setEnabled(false);
            status.setText("Status: PARSING AMOUNT...");
        }

        String getText() {
            return text.getText();
        }
        
        void giveMoney(int amount) {
            status.setText("Status: PLEASE WITHDRAW " + amount + "\u20ac WITHIN 10 SECONDS");
        }
        
        void finish() {
            status.setText("Status: WITHDRAWAL COMPLETED");
        }
        
        void fail() {
            status.setText("Status: TRY LATER");
        }

    }

}
