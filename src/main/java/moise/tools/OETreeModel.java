package moise.tools;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import moise.oe.GroupInstance;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.SchemeInstance;

/**
 * Tree model for the OS tree
 * @author  jomi
 */
public class OETreeModel {
    
    DefaultTreeModel       treeModel;
    
    JTree                  jTree;
    
    /** Creates new OSTreeModel */
    public OETreeModel() {
        setOE(null);
    }
    
    public OETreeModel(JTree j) {
        setOE(null);
        jTree = j;
    }
    
    public DefaultTreeModel getModel() {
        return treeModel;
    }
    
    public void setOE(OE oe) {
        DefaultMutableTreeNode rootNode;
        if (oe != null) {
            rootNode= new DefaultMutableTreeNode(oe.getOS().getId()+ " OE");
        } else {
            rootNode= new DefaultMutableTreeNode("---");
        }
        DefaultMutableTreeNode agNode  = new DefaultMutableTreeNode("Agents");
        DefaultMutableTreeNode grNode  = new DefaultMutableTreeNode("Groups");
        DefaultMutableTreeNode schNode = new DefaultMutableTreeNode("Schemes");
        
        rootNode.add(agNode);
        rootNode.add(grNode);
        rootNode.add(schNode);
        
        if (oe != null) {
            for (OEAgent ag: oe.getAgents()) {
                DefaultMutableTreeNode agN = new DefaultMutableTreeNode( ag );
                agNode.add(agN);
            }
            
            for (GroupInstance gi: oe.getGroups()) {
                addGr( gi, grNode );
            }

            for (SchemeInstance sch: oe.getSchemes()) {
                DefaultMutableTreeNode schN = new DefaultMutableTreeNode( sch );
                schNode.add(schN);
            }
        }
        
        treeModel = new DefaultTreeModel(rootNode);
    }
    
    public void setJTree(JTree j) {
        jTree = j;
    }
    
    private void addGr(GroupInstance g, DefaultMutableTreeNode place) {
        DefaultMutableTreeNode nG = new DefaultMutableTreeNode(g);
        place.add(nG);
        for (GroupInstance gi: g.getSubGroups()) {
            addGr(gi, nG);
        }
    }

}
