# ATM 

The purpose of this example is to show the basics of exception handling by means of a simple GUI.

To run the code, type `gradlew run`

The GUI represents an ATM:
1) The user types the desired amount as a string;
2) The amount is parsed to a number;
3) The corresponding amount of money is provided.

Two exceptions are specified and can be raised:
1) If the typed amount is not a string, a `nan` exception is raised by the parser. The exception is handled by asking for another amount
2) If the user inserts a string that is not a number three times, a further `amountUnavailable` is raised while handling exception `nan`

The example shows how exceptions can be nested i.e., the handling of an exception may cause the raising of additional ones
