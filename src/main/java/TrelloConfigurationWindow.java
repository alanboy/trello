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
        jfrm.setLayout(new BorderLayout());
        jfrm.setSize(300, 500);
        jfrm.setMinimumSize(new Dimension(250, 400));

        TrelloClient tClient = TrelloClient.GetInstance();
        String listsInConfig = tClient.getListsInConfig();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        try {
            // Create a map to store organization nodes
            Map<String, DefaultMutableTreeNode> orgNodes = new HashMap<>();
            DefaultMutableTreeNode personalNode = new DefaultMutableTreeNode("Personal Boards");
            
            // First, create nodes for all organizations
            for (Organization org : tClient.getMyOrganizations()) {
                DefaultMutableTreeNode orgNode = new DefaultMutableTreeNode(org.getDisplayName());
                orgNodes.put(org.getId(), orgNode);
                root.add(orgNode);
            }
            
            // Add personal boards node
            root.add(personalNode);

            // Now add boards to their respective organizations
            for (Board b : tClient.getMyBoards()) {
                if (b.isClosed()) {
                    continue;
                }

                DefaultMutableTreeNode boardNode = new DefaultMutableTreeNode(b.getName());
                
                // Add lists under the board
                for (org.trello4j.model.List l : tClient.getListsFromBoard(b)) {
                    boolean listIsEnabled = (listsInConfig.indexOf(l.getId()) > 0);
                    boardNode.add(new TrelloNode(l.getName(), l.getId(), listIsEnabled));
                }

                // Add board to its organization or to personal boards
                String orgId = b.getIdOrganization();
                if (orgId != null && orgNodes.containsKey(orgId)) {
                    orgNodes.get(orgId).add(boardNode);
                } else {
                    personalNode.add(boardNode);
                }
            }

            // Remove empty nodes
            if (personalNode.getChildCount() == 0) {
                root.remove(personalNode);
            }
            for (DefaultMutableTreeNode orgNode : orgNodes.values()) {
                if (orgNode.getChildCount() == 0) {
                    root.remove(orgNode);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace(); // Add stack trace for better debugging
        }

        //create the tree by passing in the root node
        JTree tree = new JTree(root);
        tree.setCellRenderer(new MyTreeCellRenderer());
        tree.setRootVisible(true);
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jfrm.add(scrollPane, BorderLayout.CENTER);

        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TrelloNode tn = (TrelloNode) tree.getLastSelectedPathComponent();

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

class TrelloNode extends DefaultMutableTreeNode {
    public boolean selected;
    public String id;

    public TrelloNode(String title, String listId, boolean selected) {
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
        if (value != null && value instanceof TrelloNode) {
            TrelloNode emailMessage = (TrelloNode) value;

            if (emailMessage.isSelected())
                setForeground(new Color(253, 57 ,115));

            return this;
        }

        return this;
    }
}
