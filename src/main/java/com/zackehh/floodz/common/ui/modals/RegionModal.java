package com.zackehh.floodz.common.ui.modals;

import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.rmc.RMCServer;
import com.zackehh.floodz.common.ui.InterfaceUtils;
import com.zackehh.floodz.common.ui.graphing.TreeNode;
import com.zackehh.floodz.common.ui.graphing.TreePainter;
import org.abego.treelayout.NodeExtentProvider;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import javax.swing.*;
import java.awt.*;

/**
 * A Modal to display a tree representation of the RMC, top down.
 * Cool feature in order to be able to see the (known) state of
 * the sensor system.
 */
@SuppressWarnings("unused") // called via reflection
public class RegionModal {

    /**
     * A reference to the InterfaceUtils instance.
     */
    private final InterfaceUtils interfaceUtils;

    /**
     * A reference to the server behind the main GUI class.
     */
    private final RMCServer rmcServer;

    /**
     * Creates a dialog panel and displays a drawn tree
     * inside. Takes advantage of TreeLayout to do so.
     *
     * @param rmcServer the main driver behind the RMC
     */
    public RegionModal(RMCServer rmcServer){
        // set required globals
        this.interfaceUtils = InterfaceUtils.getInstance();
        this.rmcServer = rmcServer;

        // grab the tree mapping
        TreeForTreeLayout<TreeNode> tree = getRegionTreeMapping();

        // setup the tree layout configuration
        DefaultConfiguration<TreeNode> configuration = new DefaultConfiguration<>(50, 10);

        // create the layout
        TreeLayout<TreeNode> treeLayout = new TreeLayout<>(tree, new NodeExtentProvider<TreeNode>() {
            @Override
            public double getWidth(TreeNode textInBox) {
                return textInBox.getWidth();
            }
            @Override
            public double getHeight(TreeNode textInBox) {
                return textInBox.getHeight();
            }
        }, configuration);

        // create a new dialog
        JDialog dialog = new JDialog();

        // get the container of the dialog
        Container contentPane = dialog.getContentPane();

        // set the main container border
        ((JComponent) contentPane).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // add a painted tree to the container
        contentPane.add(new TreePainter(treeLayout));

        // pack the dialog
        dialog.pack();

        // set the dialog properties, then show
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    /**
     * Creates a TreeForTreeLayout from the stored stations and
     * the current connections to the RMC. This tree should represent
     * an accurate state of the tree at the current point in time.
     * Draws top-down, starting at the RMC and ending on a sensor.
     *
     * @return a TreeForTreeLayout<TreeNode> instance, representing the system
     */
    private TreeForTreeLayout<TreeNode> getRegionTreeMapping(){
        // create a TreeNode from the RMC root node
        TreeNode root = new TreeNode(rmcServer.name(),
                interfaceUtils.getStringLength(rmcServer.name()));

        // create a tree layout from the root
        DefaultTreeForTreeLayout<TreeNode> tree =
                new DefaultTreeForTreeLayout<>(root);

        // for every LMS known to the RMC
        for(String lmsName : rmcServer.getKnownStations()){

            // find the LMS via CORBA
            LMS lms = rmcServer.getConnectedLMS(lmsName);

            // if there isn't one, skip it
            if(lms == null){
                continue;
            }

            // ensure connection
            try {
                lms.ping();
            } catch(Exception e) {
                continue;
            }

            // create a TreeNode for the current LMS name
            TreeNode lmsRoot = new TreeNode(lmsName, interfaceUtils.getStringLength(lmsName));

            // add the LMS node to the root node
            tree.addChild(root, lmsRoot);

            // initialize a TreeNode for a zone
            TreeNode zone = null;

            // for every sensor known to the LMS
            for(SensorMeta meta : lms.getRegisteredSensors()) {
                // if the zone is not set, or we are on a new zone
                if (zone == null || !meta.zone.equals(zone.getText())) {
                    // create a new TreeNode for the new zone
                    zone = new TreeNode(meta.zone, interfaceUtils.getStringLength(meta.zone));
                    // add the zone to the LMS
                    tree.addChild(lmsRoot, zone);
                }
                // add a TreeNode for the sensor to the zone
                tree.addChild(zone, new TreeNode(meta.sensor, interfaceUtils.getStringLength(meta.sensor)));
            }
        }

        // return the tree
        return tree;
    }

}