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

## References

1. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Exception handling in multi-agent oriented programming. The Knowledge Engineering Review 40. 2025. https://doi.org/10.1017/S0269888925000050

2. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Accountability in Multi-Agent Organizations: from Conceptual Design to Agent Programming. Journal of Autonomous Agents and Multi-Agent Systems 37.1. 2023. https://doi.org/10.1007/s10458-022-09590-6

3. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Exception Handling as a Social Concern. IEEE Internet Computing. 2022. https://doi.org/10.1109/MIC.2022.3216272

4. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Reimagining Robust Distributed Systems through Accountable MAS. IEEE Internet Computing 25.6. 2021. https://doi.org/10.1109/MIC.2021.3115450

5. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Robustness Based on Accountability in Multiagent Organizations. In Proceedings of the 20th International Conference on Autonomous Agents and MultiAgent Systems. AAMAS '21. IFAAMAS, pp. 142–150. 2021. https://dl.acm.org/doi/10.5555/3463952.3463975
