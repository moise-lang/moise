package moise.oe;

import java.io.Serializable;


/**
 * Represents the common properties for Role/Mission player.
 *
 * @author Jomi
 */
public abstract class Player implements Serializable {

    protected OEAgent player = null;

    protected Player(OEAgent ag) {
        player = ag;
    }

    public OEAgent getPlayer()  {
        return player;
    }

    protected void setPlayer(OEAgent ag) {
        player = ag;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((player == null) ? 0 : player.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Player other = (Player) obj;
        if (player == null) {
            if (other.player != null)
                return false;
        } else if (!player.equals(other.player))
            return false;
        return true;
    }
}
