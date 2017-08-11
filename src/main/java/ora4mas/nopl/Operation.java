package ora4mas.nopl;

import npl.NormativeFailureException;
/**
 * Operation used in method template for ora4mas operations on cartago artifact
 *
 * @author jomi
 */
public interface Operation {
    void exec() throws NormativeFailureException, Exception;
}
