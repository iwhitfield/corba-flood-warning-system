package com.zackehh.floodz.common.ui.graphing;

import org.abego.treelayout.TreeLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * TreePainter does exactly what it says on the tin. It paints a
 * tree as a JComponent, using an existing TreeLayout to draw
 * TreeNodes onto the component.
 */
public class TreePainter extends JComponent {

    /**
     * The layout holding the tree.
     */
    private final TreeLayout<TreeNode> treeLayout;

    /**
     * Constructor which requires a layout holding a tree.
     *
     * @param treeLayout the layout
     */
    public TreePainter(TreeLayout<TreeNode> treeLayout) {
        this.treeLayout = treeLayout;

        setPreferredSize(treeLayout.getBounds().getBounds().getSize());
    }

    /**
     * Override to call custom drawing of the JComponent. First paints
     * the edges based on the parent node (recursively) and then paints
     * the nodes.
     *
     * @param g the Graphics instance
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // paint the edges of the tree first
        paintEdges(g, treeLayout.getTree().getRoot());

        // paint the boxes of the tree
        for (TreeNode treeNode : treeLayout.getNodeBounds().keySet()) {
            // white background for nodes
            g.setColor(Color.WHITE);

            // get the node bounds
            Rectangle2D.Double box = treeLayout.getNodeBounds().get(treeNode);

            // fill in the bounds
            g.fillRoundRect((int) box.x, (int) box.y, (int) box.width - 1,
                    (int) box.height - 1, 10, 10);

            // add a gray border
            g.setColor(Color.DARK_GRAY);

            // draw a rectangle about the node
            g.drawRoundRect((int) box.x, (int) box.y, (int) box.width - 1,
                    (int) box.height - 1, 10, 10);

            // use a black color
            g.setColor(Color.BLACK);

            // grab the font metrics
            FontMetrics m = getFontMetrics(getFont());

            // write the text to the node
            g.drawString(
                    treeNode.getText(),
                    (int) box.x + 10 / 2,
                    (int) box.y + m.getAscent() + m.getLeading() + 1
            );
        }
    }

    /**
     * Draws the edges of the tree based on a parent. Moves recursively
     * down through the children.
     *
     * @param g the Graphics instance
     * @param parent the parent node
     */
    private void paintEdges(Graphics g, TreeNode parent) {
        // do not add multiple times
        if (treeLayout.getTree().isLeaf(parent)) {
            return;
        }

        // fetch parent bounds
        Rectangle2D.Double b1 = treeLayout.getNodeBounds().get(parent);

        // grab the centers
        int x1 = (int) b1.getCenterX();
        int y1 = (int) b1.getCenterY();

        // loop children
        for (TreeNode child : treeLayout.getTree().getChildren(parent)) {

            // get their bounds
            Rectangle2D.Double b2 = treeLayout.getNodeBounds().get(child);

            // draw an edge to the node center from the parent center
            g.drawLine(x1, y1, (int) b2.getCenterX(), (int) b2.getCenterY());

            // recurse using child
            paintEdges(g, child);
        }
    }

}