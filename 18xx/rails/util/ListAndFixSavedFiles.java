package rails.util;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import rails.game.ConfigurationException;
import rails.game.Game;
import rails.game.GameManager;
import rails.game.action.PossibleAction;
import rails.ui.swing.elements.ActionMenuItem;

public class ListAndFixSavedFiles extends JFrame 
implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    private JTextArea reportText;
    private JScrollPane messageScroller;
    private JScrollBar vbar;
    private JPanel messagePanel;
    private ListAndFixSavedFiles messageWindow;
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu;
    private JMenuItem saveItem, loadItem, printItem;
    private JMenuItem findItem, findBackItem, findNextItem, findPrevItem;

    protected static Logger log =
        Logger.getLogger(ListAndFixSavedFiles.class.getPackage().getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
    // TODO Auto-generated method stub
        
        String myConfigFile = System.getProperty("configfile");
        System.out.println("Cmdline configfile setting = " + myConfigFile);

        /* If not, use the default configuration file name */
        if (!Util.hasValue(myConfigFile)) {
            myConfigFile = "my.properties";
        }

        /*
         * Set the system property that tells log4j to use this file. (Note:
         * this MUST be done before updating Config)
         */
        System.setProperty("log4j.configuration", myConfigFile);
        /* Tell the properties loader to read this file. */
        Config.setConfigFile(myConfigFile);
        System.out.println("Configuration file = " + myConfigFile);
        System.out.println("Save directory = " + Config.get("save.directory"));
        
        new ListAndFixSavedFiles ();
        
    }
    
    public ListAndFixSavedFiles () {
        
        super();
        
        messageWindow = this;

        reportText = new JTextArea();
        reportText.setEditable(false);
        reportText.setLineWrap(false);
        reportText.setBackground(Color.WHITE);
        reportText.setOpaque(true);
        reportText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        messagePanel = new JPanel(new GridBagLayout());
        messageScroller =
                new JScrollPane(reportText,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        vbar = messageScroller.getVerticalScrollBar();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        messagePanel.add(messageScroller, gbc);
        
        menuBar = new JMenuBar();
        fileMenu = new JMenu(LocalText.getText("FILE"));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        editMenu = new JMenu(LocalText.getText("EDIT"));
        editMenu.setMnemonic(KeyEvent.VK_E);
 
        loadItem = new ActionMenuItem(LocalText.getText("LOAD"));
        loadItem.setActionCommand("LOAD");
        loadItem.setMnemonic(KeyEvent.VK_L);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                ActionEvent.ALT_MASK));
        loadItem.addActionListener(this);
        loadItem.setEnabled(true);
        fileMenu.add(loadItem);

        saveItem = new ActionMenuItem(LocalText.getText("SAVE"));
        saveItem.setActionCommand("SAVE");
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.ALT_MASK));
        saveItem.addActionListener(this);
        saveItem.setEnabled(true);
        fileMenu.add(saveItem);

        printItem = new ActionMenuItem(LocalText.getText("PRINT"));
        printItem.setActionCommand("PRINT");
        printItem.setMnemonic(KeyEvent.VK_P);
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                ActionEvent.ALT_MASK));
        printItem.addActionListener(this);
        printItem.setEnabled(false);
        fileMenu.add(printItem);

        menuBar.add(fileMenu);
        
        setJMenuBar(menuBar);
        
        setContentPane(messagePanel);

        setSize(400, 400);
        setLocation(600, 400);
        setTitle(LocalText.getText("GameReportTitle"));

        final JFrame frame = this;
        addKeyListener(this);

        
        setVisible(true);

        String saveDirectory = Config.get("save.directory");
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(saveDirectory));

        if (jfc.showOpenDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
            
            File selectedFile = jfc.getSelectedFile();
            String filepath = selectedFile.getPath();
            saveDirectory = selectedFile.getParent();
 
            log.debug("Loading game from file " + filepath);
            String filename = filepath.replaceAll(".*[/\\\\]", "");
    
            try {
                ObjectInputStream ois =
                        new ObjectInputStream(new FileInputStream(
                                new File(filepath)));
    
                // New in 1.0.7: Rails version & save date/time.
                // Allow for older saved file versions.
                
                Object object = ois.readObject();
                if (object instanceof String) {
                    add((String)object+" saved file "+filename);
                    object = ois.readObject();
                } else {
                    add("Reading Rails (pre-1.0.7) saved file "+filename);
                }
                if (object instanceof String) {
                    add("File was saved at "+(String)object);
                    object = ois.readObject();
                }
    
                long versionID = (Long) object;
                add("Saved versionID="+versionID+" (object="+object+")");
                long saveFileVersionID = GameManager.saveFileVersionID;
                String name = (String) ois.readObject();
                add("Saved game="+name);
                Map<String, String> selectedGameOptions =
                        (Map<String, String>) ois.readObject();
                for (String key : selectedGameOptions.keySet()) {
                    add("Option "+key+"="+selectedGameOptions.get(key));
                }
                List<String> playerNames = (List<String>) ois.readObject();
                int i=1;
                for (String player : playerNames) {
                    add("Player "+(i++)+": "+player);
                }
    
                Game game = new Game(name, playerNames, selectedGameOptions);

                if (!game.setup()) {
                    throw new ConfigurationException("Error in setting up " + name);
                }
                
                List<PossibleAction> executedActions =
                        (List<PossibleAction>) ois.readObject();
                i=1;
                for (PossibleAction action : executedActions) {
                    add("Action "+(i++)+": "+action.toString());
                }
                ois.close(); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }
        
    public void add (String text) {
        if (text.length() > 0) {
            reportText.append(text);
            reportText.append("\n");
            scrollDown();
        }
    }

    public void scrollDown () {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageWindow.vbar.setValue(messageWindow.vbar.getMaximum());
            }
        });
    }



    public void actionPerformed(ActionEvent actor) {
        String command = actor.getActionCommand();
        if ("LOAD".equalsIgnoreCase(command)) {
       }
    }
    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

}
