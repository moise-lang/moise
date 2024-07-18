package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Roof implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.RED);
        g.drawLine(200, size.height-450, 400, size.height-500);
        g.drawLine(400, size.height-500, 600, size.height-450);
        g.setColor(Color.BLACK);
        g.drawString("Roof OK", 370, size.height-460);
    }

}
