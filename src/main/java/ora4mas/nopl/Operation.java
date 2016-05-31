package ora4mas.nopl;

import npl.NormativeFailureException;

public interface Operation {
    void exec() throws NormativeFailureException, Exception;
}
