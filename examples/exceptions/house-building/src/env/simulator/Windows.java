package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Windows implements HousePart {

    public void draw(Dimension size, Graphics2D g){
        g.setColor(Color.BLACK);
        g.drawRect(250, size.height-400, 80, 80);
        g.drawRect(480, size.height-400, 80, 80);
        g.setColor(Color.BLACK);
        g.drawString("Windows OK", 250, size.height-380);
    }

}
