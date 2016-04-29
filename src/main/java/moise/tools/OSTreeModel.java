package moise.tools;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Scheme;
import moise.os.ss.Group;
import moise.os.ss.Role;


/**
 * Tree model for the OS tree
 * @author  jomi
 */
public class OSTreeModel {
    
    DefaultTreeModel       treeModel;
    
    JTree                  jTree;
    
    /** Creates new OSTreeModel */
    public OSTreeModel() {
        setOS(null);
    }
    
    public OSTreeModel(JTree j) {
        setOS(null);
        jTree = j;
    }
    
    public DefaultTreeModel getModel() {
        return treeModel;
    }
    
    public void setOS(OS os) {
        DefaultMutableTreeNode rootNode;
        if (os != null) {
            rootNode= new DefaultMutableTreeNode(os.getId());
        } else {
            rootNode= new DefaultMutableTreeNode("---");
        }
        DefaultMutableTreeNode ssNode = new DefaultMutableTreeNode("SS");
        DefaultMutableTreeNode rolesNode = new DefaultMutableTreeNode("Roles");
        DefaultMutableTreeNode grSpecNode = new DefaultMutableTreeNode("Groups");
        ssNode.add(rolesNode);
        ssNode.add(grSpecNode);
        
        DefaultMutableTreeNode fsNode = new DefaultMutableTreeNode("FS");
        
        DefaultMutableTreeNode dsNode = new DefaultMutableTreeNode("NS");
        
        rootNode.add(ssNode);
        rootNode.add(fsNode);
        rootNode.add(dsNode);
        
        if (os != null) {
            addRoles(os, rolesNode);
            addGr(os.getSS().getRootGrSpec(), grSpecNode);
            
            // os schemas
            for (Scheme sch: os.getFS().getSchemes()) {
                addSch(sch, fsNode);
            }
        }
        
        treeModel = new DefaultTreeModel(rootNode);
    }
    
    public void setJTree(JTree j) {
        jTree = j;
    }
    
    private void addRoles(OS os, DefaultMutableTreeNode place) {
        for (Role r: os.getSS().getRolesDef()) {
            place.add(new DefaultMutableTreeNode( r ));
        }
    }
    
    private void addGr(Group gr, DefaultMutableTreeNode place) {
        if (gr == null) return;
        DefaultMutableTreeNode nGr = new DefaultMutableTreeNode(gr);
        place.add(nGr);
        for (Group g: gr.getSubGroups()) {
            addGr( g, nGr);
        }
    }
    
    private void addSch(Scheme sch, DefaultMutableTreeNode place) {
        DefaultMutableTreeNode nSCH = new DefaultMutableTreeNode(sch);
        place.add(nSCH);
        
        // goals
        DefaultMutableTreeNode nGoals = new DefaultMutableTreeNode("Goals & plans");
        nSCH.add(nGoals);
        for (Goal g: sch.getGoals()) {
            addGoal(g, nGoals);
        }
        
        // missions
        DefaultMutableTreeNode nMis = new DefaultMutableTreeNode("Missions");
        nSCH.add(nMis);
        for (Mission m: sch.getMissions()) {
            nMis.add(new DefaultMutableTreeNode(m));
        }
    }
    
    private void addGoal(Goal g, DefaultMutableTreeNode place) {
        DefaultMutableTreeNode nG = new DefaultMutableTreeNode(g);
        place.add(nG);
        if (g.getPlan() != null) {
            
            for (Goal gs: g.getPlan().getSubGoals()) {
                addGoal(gs, nG);
            }
        }
    }
}
