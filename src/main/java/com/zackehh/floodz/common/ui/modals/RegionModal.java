package com.zackehh.floodz.common.ui.modals;

import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMS;
import com.zackehh.floodz.common.ui.InterfaceUtils;
import com.zackehh.floodz.util.SQLiteClient;
import com.zackehh.floodz.common.ui.graphing.TreeNode;
import com.zackehh.floodz.common.ui.graphing.TreeNodePainter;
import com.zackehh.floodz.rmc.RMCDriver;
import org.abego.treelayout.NodeExtentProvider;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
public class RegionModal implements Modal {

    private final String NO_LMS_FOUND = "No LMSs found!";

    private final InterfaceUtils interfaceUtils;
    private final RMCDriver rmcDriver;
    private final SQLiteClient sqLiteClient;

    private JDialog jDialog;

    public RegionModal(RMCDriver rmcDriver){
        this.interfaceUtils = InterfaceUtils.getInstance();
        this.rmcDriver = rmcDriver;
        this.sqLiteClient = SQLiteClient.getInstance();
    }

    public void showModal() {

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

        JDialog dialog = new JDialog();
        Container contentPane = dialog.getContentPane();
        ((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));
        contentPane.add(new TreeNodePainter(treeLayout));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private TreeForTreeLayout<TreeNode> getRegionTreeMapping(){
        TreeNode root = new TreeNode(rmcDriver.name(), interfaceUtils.getStringLength(rmcDriver.name()));

        DefaultTreeForTreeLayout<TreeNode> tree =
                new DefaultTreeForTreeLayout<>(root);

        for(String lmsName: rmcDriver.getKnownStations()){
            LMS lms = rmcDriver.getConnectedLMS(lmsName);

            if(lms == null){
                continue;
            }

            TreeNode lmsRoot = new TreeNode(lmsName, interfaceUtils.getStringLength(lmsName));

            tree.addChild(root, lmsRoot);

            TreeNode zone = null;
            for(SensorMeta meta : lms.getRegisteredSensors()) {
                if (zone == null || !meta.zone.equals(zone.getText())) {
                    if (zone != null){
                        tree.addChild(lmsRoot, zone);
                        zone = new TreeNode(meta.zone, interfaceUtils.getStringLength(meta.zone));
                    } else {
                        zone = new TreeNode(meta.zone, interfaceUtils.getStringLength(meta.zone));
                        tree.addChild(lmsRoot, zone);
                    }
                }
                tree.addChild(zone, new TreeNode(meta.sensor, interfaceUtils.getStringLength(meta.sensor)));
            }
        }

        return tree;
    }

}