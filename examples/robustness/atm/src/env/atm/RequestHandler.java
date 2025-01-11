package atm;

public class RequestHandler {

    private Reader reader = new Reader();
    private Parser parser = new Parser();

    public int obtainAmount() throws AmountUnavailableException {
        boolean done = false;
        int count = 0;
        int amountInt = 0;
        while (!done && count < 3) {
            String amountString = reader.getAmountAsString();
            try {
                amountInt = parser.parseAmount(amountString);
                done = true;
            } catch (NotANumberException nan) {
                if (++count == 3) {
                    throw new AmountUnavailableException();
                }
            }
        }
        return amountInt;
    }

}
