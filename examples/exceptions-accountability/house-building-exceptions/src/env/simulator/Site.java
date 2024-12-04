package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Site implements HousePart {

    private SiteStatus status;
    
    enum SiteStatus {OK, FLOODED, REMAINS, DELIMITED};
    
    public Site(SiteStatus status) {
        super();
        this.status = status;
    }
    
    public void draw(Dimension size, Graphics2D g){
        if(status == SiteStatus.OK) {
            g.setColor(Color.GREEN);
        }
        else if(status == SiteStatus.FLOODED) {
            g.setColor(Color.CYAN);
        }
        else if(status == SiteStatus.DELIMITED) {
            g.setColor(Color.YELLOW);
        }
        else if(status == SiteStatus.REMAINS) {
            g.setColor(Color.RED);
        }
        g.fillRect(0, size.height-200, size.width,100);
        g.setColor(Color.BLACK);
        if(status == SiteStatus.OK) {
            g.drawString("Site OK", 20, size.height-185);
        }
        else if(status == SiteStatus.FLOODED) {
            g.drawString("Site FLOODED!", 20, size.height-185);
        }
        else if(status == SiteStatus.DELIMITED) {
            g.drawString("Site DELIMITED!", 20, size.height-185);
        }
        else if(status == SiteStatus.REMAINS) {
            g.drawString("Achaeological remains found!", 20, size.height-185);
        }
    }

}
