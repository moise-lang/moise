package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class InteriorPainting implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.BLACK);
        g.drawString("Interior Paining OK", 20, 80);
    }

}
