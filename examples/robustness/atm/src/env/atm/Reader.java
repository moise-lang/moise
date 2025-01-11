package atm;

import java.util.Scanner;

public class Reader {

    public String getAmountAsString() {
        System.out.print("Insert the amount to withdraw: ");
        Scanner input = new Scanner(System.in);
        return input.next();
    }

}
