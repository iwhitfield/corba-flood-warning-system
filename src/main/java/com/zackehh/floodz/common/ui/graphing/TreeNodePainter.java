package com.zackehh.floodz.common.ui.graphing;

import org.abego.treelayout.TreeLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class TreeNodePainter extends JComponent {

    private final TreeLayout<TreeNode> treeLayout;

    public TreeNodePainter(TreeLayout<TreeNode> treeLayout) {
        this.treeLayout = treeLayout;
        setPreferredSize(treeLayout.getBounds().getBounds().getSize());
    }

    private void paintEdges(Graphics g, TreeNode parent) {
        if (!treeLayout.getTree().isLeaf(parent)) {

            Rectangle2D.Double b1 = treeLayout.getNodeBounds().get(parent);

            int x1 = (int) b1.getCenterX();
            int y1 = (int) b1.getCenterY();

            for (TreeNode child : treeLayout.getTree().getChildren(parent)) {
                Rectangle2D.Double b2 = treeLayout.getNodeBounds().get(child);

                g.drawLine(x1, y1, (int) b2.getCenterX(), (int) b2.getCenterY());

                paintEdges(g, child);
            }
        }
    }

    private void paintBox(Graphics g, TreeNode treeNode) {
        // draw the box in the background
        g.setColor(Color.WHITE);

        Rectangle2D.Double box = treeLayout.getNodeBounds().get(treeNode);

        g.fillRoundRect((int) box.x, (int) box.y, (int) box.width - 1,
                (int) box.height - 1, 10, 10);

        // border
        g.setColor(Color.DARK_GRAY);

        g.drawRoundRect((int) box.x, (int) box.y, (int) box.width - 1,
                (int) box.height - 1, 10, 10);

        // draw the text on top of the box (possibly multiple lines)
        g.setColor(Color.BLACK);

        FontMetrics m = getFontMetrics(getFont());

        g.drawString(
                treeNode.getText(),
                (int) box.x + 10 / 2,
                (int) box.y + m.getAscent() + m.getLeading() + 1
        );
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        paintEdges(g, treeLayout.getTree().getRoot());

        // paint the boxes
        for (TreeNode treeNode : treeLayout.getNodeBounds().keySet()) {
            paintBox(g, treeNode);
        }
    }
}