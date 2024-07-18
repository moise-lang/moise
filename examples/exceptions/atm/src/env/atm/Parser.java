package atm;

public class Parser {

    public int parseAmount(String amountString) throws NotANumberException {
        
        if (amountString == null) {
            throw new NotANumberException();
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
                    throw new NotANumberException();

                if (len == 1) // Cannot have lone "+" or "-"
                    throw new NotANumberException();
                i++;
            }
            multmin = limit / 10;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(amountString.charAt(i++),10);
                if (digit < 0) {
                    throw new NotANumberException();
                }
                if (result < multmin) {
                    throw new NotANumberException();
                }
                result *= 10;
                if (result < limit + digit) {
                    throw new NotANumberException();
                }
                result -= digit;
            }
        } else {
            throw new NotANumberException();
        }
        return negative ? result : -result;
    }

}
