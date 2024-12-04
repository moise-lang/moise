package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Doors implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.BLACK);
        g.drawRect(350, size.height-300, 100, 100);
        g.setColor(Color.BLACK);
        g.drawString("Doors OK", 370, size.height-250);
    }

}
