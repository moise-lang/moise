package moise;

import static org.junit.Assert.*;

import org.junit.Test;

import moise.os.Cardinality;
import moise.os.CardinalitySet;
import moise.os.ss.Role;

public class CardinalitySetTest {

    @Test
    public void testAdd() {
        Cardinality c1 = new Cardinality(1,2);
        Role r1 = new Role("player",null);
        
        CardinalitySet<Role> roles = new CardinalitySet<Role>();
        
        roles.add(r1);
        roles.add(new Role("coach",null), c1);
        
        assertEquals(roles.size(),2);
        
        assertTrue(roles.contains(r1));
        assertTrue(roles.contains("player"));
        assertTrue(roles.contains(new Role("player",null)));

        assertEquals(roles.getCardinality(r1), Cardinality.defaultValue);
        assertEquals(roles.getCardinality(new Role("player",null)), Cardinality.defaultValue);
        assertEquals(roles.getCardinality(new Role("coach",null)), c1);
    }
}
