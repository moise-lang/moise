package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.tools.GUIArtifact;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import simulator.Site.SiteStatus;

public class House extends GUIArtifact {

    HouseView view;

    private int attempts = 0;

    @Override
    public void init(){
        view = new HouseView();
        view.setVisible(true);
    }

    // Actions that simulate the building progress

    @OPERATION void prepareSite(){
        if(attempts++ == 0) {
            Random random = new Random();
            if(random.nextInt()%2 == 0) {
                view.addPart(new Site(SiteStatus.FLOODED));
                failed("Bad weather","flooding");
            }
            else {
                view.addPart(new Site(SiteStatus.REMAINS));
                failed("Egyptian remains found","archaeologicalRemains");
            }
        }
        else {
            view.addPart(new Site(SiteStatus.OK));
        }
    }

    @OPERATION void performSiteAnalysis(OpFeedbackParam<Literal> result){
        result.set(ASSyntax.createLiteral("waterQuantity", ASSyntax.parseNumber("20")));
    }

    @OPERATION void fixFlooding(String waterQuantity){
        view.addPart(new Site(SiteStatus.OK));
    }

    @OPERATION void delimitSite(){
        view.addPart(new Site(SiteStatus.DELIMITED));
    }

    @OPERATION void carefullyRemoveRemains(){
        view.removeParts();
    }

    @OPERATION void layFloors(){
        await_time(1000);
        view.addPart(new Floor());
    }

    @OPERATION void buildWalls(){
        await_time(500);
        view.addPart(new Walls());
    }

    @OPERATION void buildRoof(){
        await_time(1000);
        view.addPart(new Roof());
    }

    @OPERATION void fitDoors(){
        await_time(300);
        view.addPart(new Doors());
    }

    @OPERATION void fitWindows(){
        await_time(300);
        view.addPart(new Windows());
    }

    @OPERATION void paintExterior(){
        await_time(2000);
        view.addPart(new ExteriorPainting());
    }

    @OPERATION void installPlumbing(){
        await_time(300);
        view.addPart(new Plumbing());
    }

    @OPERATION void installElectricalSystem(){
        await_time(300);
        view.addPart(new ElectricalSystem());
    }

    @OPERATION void paintInterior(){
        await_time(500);
        view.addPart(new InteriorPainting());
    }


    class HouseView extends JFrame {

        HousePanel           housePanel;
        ArrayList<HousePart> partsToDraw;

        public HouseView(){
            setTitle(" -- Home Sweet Home -- ");
            setSize(800,600);

            partsToDraw = new ArrayList<HousePart>();
            housePanel  = new HousePanel(this);
            setContentPane(housePanel);
        }

        public synchronized void addPart(HousePart part){
            partsToDraw.add(part);
            repaint();
        }

        public synchronized void removeParts(){
            partsToDraw.clear();
            repaint();
        }

        public synchronized ArrayList<HousePart> getParts(){
            return (ArrayList<HousePart>)partsToDraw.clone();
        }
    }

    class HousePanel extends JPanel {

        HouseView view;

        public HousePanel(HouseView view){
            this.view = view;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

               g.setColor(Color.WHITE);
               Dimension size = getSize();
               g.fillRect(0, 0, size.width, size.height);

               for (HousePart part: view.getParts()){
                   part.draw(size,(Graphics2D)g);
               }
        }
    }
}
