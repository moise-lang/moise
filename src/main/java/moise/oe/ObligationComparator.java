package moise.oe;

import java.util.Comparator;

public class ObligationComparator implements Comparator<Permission> {

        public int compare(Permission p1, Permission p2) {
            try {
                return p1.getMission().compareTo(p2.getMission());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
}
