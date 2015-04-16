package com.zackehh.floodz.common.ui.modals;

import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.sensor.Sensor;
import com.zackehh.floodz.common.ui.InterfaceUtils;
import com.zackehh.floodz.util.SQLiteClient;
import com.zackehh.floodz.common.ui.graphing.TextInBox;
import com.zackehh.floodz.common.ui.graphing.TextInBoxTreePane;
import com.zackehh.floodz.rmc.RMCDriver;
import org.abego.treelayout.NodeExtentProvider;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class RegionModal implements ModalInterface {

    private final String NO_LMS_FOUND = "No LMSs found!";

    private InterfaceUtils interfaceUtils;
    private JDialog jDialog;
    private RMCDriver rmcDriver;
    private SQLiteClient sqLiteClient;

    public void showModal(RMCDriver rmcDriver) {
        this.interfaceUtils = InterfaceUtils.getInstance();
        this.rmcDriver = rmcDriver;
        this.sqLiteClient = SQLiteClient.getInstance();

        JDialog dialog = new JDialog();
        Container contentPane = dialog.getContentPane();

        TreeForTreeLayout<TextInBox> tree = getRegionTreeMapping(contentPane.getFontMetrics(contentPane.getFont()));

        // setup the tree layout configuration
        DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<>(50, 10);

        // create the layout
        TreeLayout<TextInBox> treeLayout = new TreeLayout<>(tree, new NodeExtentProvider<TextInBox>() {
            @Override
            public double getWidth(TextInBox textInBox) {
                return textInBox.getWidth();
            }

            @Override
            public double getHeight(TextInBox textInBox) {
                return textInBox.getHeight();
            }
        }, configuration);

        ((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));
        contentPane.add(new TextInBoxTreePane(treeLayout));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private TreeForTreeLayout<TextInBox> getRegionTreeMapping(FontMetrics fontMetrics){
        TextInBox root = new TextInBox(rmcDriver.name(), interfaceUtils.getStringLength(rmcDriver.name()));

        DefaultTreeForTreeLayout<TextInBox> tree =
                new DefaultTreeForTreeLayout<>(root);

        for(String lmsName: rmcDriver.getKnownStations()){
            LMS lms = rmcDriver.getConnectedLMS(lmsName);

            if(lms == null){
                continue;
            }

            TextInBox lmsRoot = new TextInBox(lmsName, interfaceUtils.getStringLength(lmsName));

            tree.addChild(root, lmsRoot);

            TextInBox zone = null;
            for(SensorMeta meta : lms.getRegisteredSensors()) {
                if (zone == null || !meta.zone.equals(zone.getText())) {
                    if (zone != null){
                        tree.addChild(lmsRoot, zone);
                        zone = new TextInBox(meta.zone, interfaceUtils.getStringLength(meta.zone));
                    } else {
                        zone = new TextInBox(meta.zone, interfaceUtils.getStringLength(meta.zone));
                        tree.addChild(lmsRoot, zone);
                    }
                }
                tree.addChild(zone, new TextInBox(meta.sensor, interfaceUtils.getStringLength(meta.sensor)));
            }
        }

        return tree;
    }

}