package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Walls implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.BLACK);
        g.drawRect(200, size.height-450, 400, 250);
        g.setColor(Color.BLACK);
        g.drawString("Walls OK", 210, size.height-430);
    }

}
