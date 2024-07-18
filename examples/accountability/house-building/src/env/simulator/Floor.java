package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Floor implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.GRAY);
        g.fillRect(200, size.height-200, 400, 25);
        g.setColor(Color.BLACK);
        g.drawString("Floor OK", 210, size.height-185);
    }

}
