import com.google.gson.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.logging.log4j.*;
import org.trello4j.*;
import org.trello4j.model.*;

public class TrelloConfigurationWindow {

    static JDialog frame;
    static JLabel label;

    TrelloConfigurationWindow() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TrelloConfigurationWindow.createAndShowGUI();
            }
        });
    }

    public static void createAndShowGUI() {

        JFrame jfrm = new JFrame("Trello Client Configuration");
        jfrm.setLayout(new FlowLayout());
        jfrm.setSize(300, 500);

        TrelloClient tClient = TrelloClient.GetInstance();
        String listsInConfig = tClient.getListsInConfig();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        try {
            for (Board b : tClient.getMyBoards()) {
                DefaultMutableTreeNode vegetableNode = new DefaultMutableTreeNode(b.getName());

                for (org.trello4j.model.List l : tClient.getListsFromBoard(b)) {
                    boolean listIsEnabled = (listsInConfig.indexOf(l.getId()) > 0);
                    vegetableNode.add(new TRelloNode(l.getName(), l.getId(), listIsEnabled));
                }

                root.add(vegetableNode);
            }
        }catch (Exception e) {
            System.out.println(e);
        }

        //create the tree by passing in the root node
        JTree tree = new JTree(root);
        tree.setCellRenderer(new MyTreeCellRenderer());
        tree.setRootVisible(true);
        //tree.setExpandedState(new TreePath(root), true);
        jfrm.add(new JScrollPane(tree));

        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TRelloNode tn = (TRelloNode) tree.getLastSelectedPathComponent();

                TrelloClient tClient = TrelloClient.GetInstance();
                if (tn.isSelected()) {
                    tClient.removeListFromConfig(tn.id);
                } else {
                    tClient.addListToConfig(tn.id);
                }

                tn.setSelected(!tn.isSelected());

                try {
                    TrelloClient.GetInstance().updateOnce();
                } catch(Exception e) {
                    // nothing i can do
                }
            }
        });

        jfrm.setVisible(true);
    }
}

class TRelloNode extends DefaultMutableTreeNode {
    public boolean selected;
    public String id;

    public TRelloNode(String title, String listId, boolean selected) {
        super(title);
        this.id = listId;
        this.selected = selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }
}

 class MyTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

        // Assuming you have a tree of Strings
        if (value != null && value instanceof TRelloNode) {
            TRelloNode emailMessage = (TRelloNode) value;

            if (emailMessage.isSelected())
                setForeground(new Color(253, 57 ,115));

            return this;
        }

        return this;
    }
}

