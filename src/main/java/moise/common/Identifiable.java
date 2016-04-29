package moise.common;

/**
 * interface for moise+ elements which have prefix.id identification
 */
public interface Identifiable {
    public String getId();
    public String getPrefix();
    public String getFullId();
}
