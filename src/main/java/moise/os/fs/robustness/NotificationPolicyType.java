package moise.os.fs.robustness;

public enum NotificationPolicyType {
        
    ACCOUNTABILITY {
        @Override
        public String toString() {
            return "accountability";
        }
    }, EXCEPTION {
        @Override
        public String toString() {
            return "exception";
        }
    }
}
