# Bakery

The purpose of this example is to show the basics of accountability.

To run the code, type `gradlew run`

The MAS represents a bakery in which the process of bread selling involves the following steps:
1) The dough is kneaded and, at the same time, the oven is set up for baking.
2) Once the dough and the oven are ready, the bread is actually baked.
3) Finally it is sold to customers.

Agent `sheila` is in charge of selling the bread, `bart` is the baker, while `mike` is in charge for kneading the dough.

Two accounts are specified and can be requested:
1) `sheila` can request the flour type to `bart`
2) `bart` can, in turn, request the flour type to `mike`

The example shows how agents can leverage the information obtained thanks to accountability in their decision making.
E.g., `sheila` sells the bread at a higher price if the used flour is organic.

The example also highlights the structural dimension of accountability.
`bart`, in order to give the requested account to `sheila`, can ask itself request an account to `mike`.

Notification policy `np2` also includes a `context-goal`.
Context goals constrain when an account can be requested by the account taker.
They must be achieved before the account request can be performed.
In this case, `sheila`, before requesting the account to `bart`, must achieve the context goal `getAuthorization`.

## References

1. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Exception handling in multi-agent oriented programming. The Knowledge Engineering Review 40. 2025. https://doi.org/10.1017/S0269888925000050

2. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Accountability in Multi-Agent Organizations: from Conceptual Design to Agent Programming. Journal of Autonomous Agents and Multi-Agent Systems 37.1. 2023. https://doi.org/10.1007/s10458-022-09590-6

3. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Exception Handling as a Social Concern. IEEE Internet Computing. 2022. https://doi.org/10.1109/MIC.2022.3216272

4. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Reimagining Robust Distributed Systems through Accountable MAS. IEEE Internet Computing 25.6. 2021. https://doi.org/10.1109/MIC.2021.3115450

5. M. Baldoni, C. Baroglio, R. Micalizio, S. Tedeschi. Robustness Based on Accountability in Multiagent Organizations. In Proceedings of the 20th International Conference on Autonomous Agents and MultiAgent Systems. AAMAS '21. IFAAMAS, pp. 142–150. 2021. https://dl.acm.org/doi/10.5555/3463952.3463975
