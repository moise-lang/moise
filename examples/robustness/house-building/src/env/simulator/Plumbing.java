package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Plumbing implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.BLACK);
        g.drawString("Plumbing OK", 20, 20);
    }

}
