/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prﾃｼnster
 * Copyright 2013, 2014 Bernd Prﾃｼnster
 *
 *     This file is part of Magnum PI.
 *
 *     Magnum PI is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Magnum PI is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Magnum PI.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.iaik.magnum.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.bind.JAXBException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import at.tugraz.iaik.magnum.client.cg.CallGraph;
import at.tugraz.iaik.magnum.client.cg.CallGraphNode;
import at.tugraz.iaik.magnum.client.conf.ConfFile;
import at.tugraz.iaik.magnum.client.conf.RuntimeConfig;
import at.tugraz.iaik.magnum.client.db.IDBUtil;
import at.tugraz.iaik.magnum.client.gui.utils.LogMessageTableModel;
import at.tugraz.iaik.magnum.client.gui.utils.LogMessageTableModel.TYPE;
import at.tugraz.iaik.magnum.client.gui.utils.PackageConfigTableModel;
import at.tugraz.iaik.magnum.client.gui.utils.TimelineTableModel;
import at.tugraz.iaik.magnum.client.gui.utils.WindowStateManager;
import at.tugraz.iaik.magnum.client.gui.widgets.CustomBorderedInternalFrame;
import at.tugraz.iaik.magnum.client.gui.widgets.HistoryTextField;
import at.tugraz.iaik.magnum.client.gui.widgets.LibSexyTextField;
import at.tugraz.iaik.magnum.client.gui.widgets.MagnumPane;
import at.tugraz.iaik.magnum.client.gui.widgets.MainFrame;
import at.tugraz.iaik.magnum.client.gui.widgets.trace.InvocationTrace;
import at.tugraz.iaik.magnum.client.gui.widgets.tree.ClassTreeCellRenderer;
import at.tugraz.iaik.magnum.client.gui.widgets.tree.ClassTreeModel;
import at.tugraz.iaik.magnum.client.gui.widgets.tree.ClassTreeMouseListener;
import at.tugraz.iaik.magnum.client.gui.widgets.tree.InvocationTreeController;
import at.tugraz.iaik.magnum.client.gui.widgets.tree.InvocationTreeModel;
import at.tugraz.iaik.magnum.client.gui.widgets.tree.MethodTreeNode;
import at.tugraz.iaik.magnum.client.net.Communication;
import at.tugraz.iaik.magnum.client.net.MessageReceivingStateMachine;
import at.tugraz.iaik.magnum.client.util.BWListParser;
import at.tugraz.iaik.magnum.client.util.Continuation;
import at.tugraz.iaik.magnum.client.util.DependencyModule;
import at.tugraz.iaik.magnum.client.util.IMoustacheDecompiler;
import at.tugraz.iaik.magnum.client.util.Injector;
import at.tugraz.iaik.magnum.client.util.LolCat;
import at.tugraz.iaik.magnum.client.util.datatypes.BlackWhiteListContainer;
import at.tugraz.iaik.magnum.client.util.datatypes.DBInvocation;
import at.tugraz.iaik.magnum.client.util.datatypes.SortedList;
import at.tugraz.iaik.magnum.conf.PackageConfig;
import at.tugraz.iaik.magnum.data.cmd.CommandBuilder;
import at.tugraz.iaik.magnum.dataprocessing.EmitListener;
import at.tugraz.iaik.magnum.dataprocessing.IEmitListenerRegistry;
import at.tugraz.iaik.magnum.dataprocessing.IMoustacheClassLoader;
import at.tugraz.iaik.magnum.dataprocessing.XmlSerializer;
import at.tugraz.iaik.magnum.model.ApkModel;
import at.tugraz.iaik.magnum.model.ClassModel;
import at.tugraz.iaik.magnum.model.LogMessageModel;
import at.tugraz.iaik.magnum.model.MethodInvocationModel;
import at.tugraz.iaik.magnum.model.MethodModel;
import at.tugraz.iaik.magnum.model.PackageConfigModel;
import at.tugraz.iaik.magnum.util.JavaNameHelper;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.strobel.assembler.Collection;

public class EvesDropper {

  private MainFrame                    frmEvesDropper;
  private LibSexyTextField             jfFilterNodes, jtfFilterLog, jtfFilterPkgs;
  private HistoryTextField             jtfFilterTimeline;
  private JTable                       jtbLog, jtbHooks, jtbTimeline;
  private JLabel                       jlbState;
  private JTextField                   jtfIP;
  private JButton                      jbuConnect;
  private JTree                        jtClassesMaster;
  private JToolBar                     jtbStatus;
  private JLabel                       jlbStatus;
  private LogMessageTableModel         logTableModel;
  private TimelineTableModel           timelineTableModel, passwordTableModel, 
  									   functionInvocation, differentFunctionInvocation;
  private ClassTreeModel               classTreeModel;
  private InvocationTreeModel          invocationTreeModel;
  private CallGraph                    callGraph;

  private InvocationTreeController     invocationTreeController;
  private JButton                      btnExport;
  private MessageReceivingStateMachine messageReceiver;
  private SortedList<ClassModel>       classList;
  private JDesktopPane                 desktopPane;
  private CustomBorderedInternalFrame  jifLog;
  private CustomBorderedInternalFrame  jifClasses;
  private CustomBorderedInternalFrame  jifCallGraph;
  private CustomBorderedInternalFrame  jifSourceCode;
  private CustomBorderedInternalFrame  jifTimeline;
  private CustomBorderedInternalFrame  jifHooker;
  private CustomBorderedInternalFrame  jifBWList;
  private CustomBorderedInternalFrame  jifImport;
  private JRadioButton                 jrbPureWhiteList;
  private JRadioButton                 jrbRegularMode;
  private JTextArea                    jtaBlackWhiteList;
  private RSyntaxTextArea              tfSourceCode;
  private JTextArea                    jtaInvocationDetail;
  private JScrollPane                  jspHooks;
  private JScrollPane                  jspTimeline;
  private JList				   		   jLstPasswords;
  private JScrollPane				   jspPasswords;
  private JTable 					   jtbPasswordTimeline;
  private JScrollPane				   jspPasswordTimeline;
  private JTable 					   jtbFunctionInvocation;
  private JScrollPane 				   jspFunctionInvocation;
  private JTable					   jtbDifferentFunctionInvocation;
  private JScrollPane				   jspDifferentFunctionInvocation;

  private PackageConfigTableModel      hookTableModel;
  private ClassTreeMouseListener       classTreeMouseListener;

  private ExecutorService              dispatcher;
  private IMoustacheClassLoader        moustacheClassLoader;
  private IEmitListenerRegistry        emitterRegistry;
  private IMoustacheDecompiler         moustacheDecompiler;
  private Communication                networkCommunication;
  private final IDBUtil                dbUtil;

  private InvocationTrace              jungGraph;
  private DecompileWrapper             decompileWrapper;

  private boolean                      globalWBList;
  private DefaultListModel<String>	   passwordList;
  
  private List<DBInvocation> 		   invocationList;
  private DBInvocation 				   origin;

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          Module bindings = new DependencyModule();
          Injector.setup(bindings);
          EvesDropper window = (EvesDropper) Injector.get(EvesDropper.class);
          window.setupWindow();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void setupWindow() {
    frmEvesDropper.setVisible(true);
    loadWindowStates();
  }

  /**
   * @wbp.parser.entryPoint
   */
  @Inject
  public EvesDropper(MessageReceivingStateMachine messageReceiver, IMoustacheClassLoader moustacheClassLoader,
      Communication networkCommunication, IEmitListenerRegistry emitterRegistry,
      IMoustacheDecompiler moustacheDecompiler, final IDBUtil dbUtil) throws IOException {
    this.globalWBList = false;
    this.messageReceiver = messageReceiver;
    this.moustacheClassLoader = moustacheClassLoader;
    this.networkCommunication = networkCommunication;
    this.emitterRegistry = emitterRegistry;
    this.moustacheDecompiler = moustacheDecompiler;
    this.dbUtil = dbUtil;
    this.decompileWrapper = new DecompileWrapper();

    networkCommunication.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        int blinkenlight = jlbState.getForeground().getRGB();

        final Color c = new Color(blinkenlight + (128 << 8));
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            jlbState.setForeground(c);
          }
        });
      }
    });

    initialize();

    dispatcher = Executors.newFixedThreadPool(2);

    new Thread(new DBQueryExecutor()).start();
  }

  private void initCallGraphGui() {
    jungGraph = new InvocationTrace(callGraph, dbUtil, decompileWrapper);
    jifCallGraph.setMinimumSize(new Dimension(500, 200));
    JScrollPane jspCg = new JScrollPane(jungGraph);
    jspCg.setViewportView(jungGraph);
    jspCg.setAutoscrolls(true);
    jifCallGraph.add(jspCg, BorderLayout.CENTER);

  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {

    System.out.println("Eve's Dropper running");
    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
    }

    jlbState = new JLabel(" \u2022 ");
    jtfIP = new JTextField();
    jbuConnect = new JButton("Connect");

    frmEvesDropper = new MainFrame();
    frmEvesDropper.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        saveWindowStates();
        dbUtil.exit();
      }
    });
    frmEvesDropper.setTitle("Eve's Dropper");
    frmEvesDropper.setBounds(100, 100, 800, 500);
    frmEvesDropper.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.out.println("Bye!");
        System.exit(0);
      }
    });
    BufferedImage icon_magnum;
    try {
      icon_magnum = ImageIO.read(EvesDropper.class.getResource(GuiConstants.PATH_RES_ICONS
          + GuiConstants.NAME_ICON_MAGNUM));
      frmEvesDropper.setIconImage(icon_magnum);
    } catch (IOException e2) {
      e2.printStackTrace();
    }

    jtbStatus = new JToolBar();
    frmEvesDropper.getContentPane().add(jtbStatus, BorderLayout.SOUTH);

    jlbStatus = new JLabel("Not Connected");
    jlbStatus.setIcon(GuiConstants.ICON_DISCONECTED);

    jtbStatus.add(jlbStatus);
    jtbStatus.add(new JSeparator(JSeparator.VERTICAL));
    btnExport = new JButton("Export");
    btnExport.setEnabled(false);
    btnExport.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doExport();
      }
    });

    JPanel jpConnect = new JPanel();
    frmEvesDropper.getContentPane().add(jpConnect, BorderLayout.NORTH);
    jpConnect.setLayout(new BorderLayout(3, 0));

    jlbState.setForeground(Color.RED);
    jlbState.setFont(new Font("Dialog", Font.BOLD, 24));
    JPanel jpTool = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    jpTool.add(btnExport);
    jpTool.add(jlbState);
    jpConnect.add(jpTool, BorderLayout.WEST);

    jtfIP.setHorizontalAlignment(SwingConstants.TRAILING);
    jtfIP.setText(ConfFile.get(ConfFile.KEY_IP));
    jpConnect.add(jtfIP, BorderLayout.CENTER);
    jtfIP.setColumns(10);

    jbuConnect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (networkCommunication != null && networkCommunication.isConnected())
          return;

        String[] addr = jtfIP.getText().split(":");

        try {
          onConnect(addr);
        } catch (NumberFormatException e1) {
          setConnectionState(false, e1.getMessage());
        } catch (UnknownHostException e1) {
          setConnectionState(false, e1.getMessage());
        } catch (IOException e1) {
          setConnectionState(false, e1.getMessage());
        }
      }
    });
    jpConnect.add(jbuConnect, BorderLayout.EAST);

    desktopPane = new MagnumPane();
    desktopPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.DARK_GRAY, Color.DARK_GRAY));
    frmEvesDropper.getContentPane().add(desktopPane, BorderLayout.CENTER);

    callGraph = new CallGraph();

    setupJIFLog();
    setupJIFClasses();
    setupJIFCallGraph();
    setupJIFSourceCode();
    setupJIFTimeline();
    setupJIFHooker();
    setupJIFBWList();
    setupJIFImport();

    createJIFButton(jifLog);
    createJIFButton(jifHooker);
    createJIFButton(jifClasses);
    createJIFButton(jifCallGraph);
    createJIFButton(jifSourceCode);
    createJIFButton(jifTimeline);
    createJIFButton(jifBWList);
    createJIFButton(jifImport);

  }

  private void onHook(String pkg, boolean status) {
    try {
      if (status)
        System.out.println("Hooking " + pkg);
      else
        System.out.println("Unhooking " + pkg);
      networkCommunication.write(CommandBuilder.buildForHookUnhookCmd(pkg, status));
    } catch (ConnectException ex) {
      ex.printStackTrace();
    }
  }

  private void doExport() {

    try {
      JFileChooser jfcExport = new JFileChooser(ConfFile.get(ConfFile.KEY_PATH));
      int action = jfcExport.showSaveDialog(frmEvesDropper);
      if (action == JFileChooser.APPROVE_OPTION) {

        File destFile = new File(jfcExport.getSelectedFile().getCanonicalPath() + ".magnum.zip");

        File tmpExport = File.createTempFile("magnum-export", ".tmp");
        tmpExport.delete();
        tmpExport.mkdir();

        String exportPath = tmpExport.getAbsolutePath() + File.separator + "magnum.xml";
        XmlSerializer.exportModel(classList, exportPath);
        dbUtil.export(tmpExport.getCanonicalPath());
        if (globalWBList)
          try {
            File bw;
            if (jrbPureWhiteList.isSelected())
              bw = new File(tmpExport.getAbsolutePath() + File.separator + "whitelist.xml");
            else
              bw = new File(tmpExport.getAbsolutePath() + File.separator + "blacklist.xml");

            FileWriter w = new FileWriter(bw);
            w.write(jtaBlackWhiteList.getText());
            w.close();
          } catch (Exception e) {

          }
        
        //export Apk file
        Set<JarFile> jarFiles = moustacheClassLoader.getJarFiles();
        
        for ( JarFile jar: jarFiles)
        {
        	File file = new File(jar.getName());
        	File tmpApk = new File(tmpExport.getAbsolutePath() + File.separator + file.getName());
        	Files.copy(file.toPath(), tmpApk.toPath());
        }
        

        
        File[] files = tmpExport.listFiles();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(destFile));
        FileInputStream tmpIn;
        for (File f : files) {
          zout.putNextEntry(new ZipEntry(f.getName()));
          tmpIn = new FileInputStream(f);
          byte[] data = new byte[(int) f.length()];
          tmpIn.read(data);

          zout.write(data);
          tmpIn.close();
          zout.closeEntry();
        }
        zout.close();
        tmpExport.delete();

        ConfFile.set(ConfFile.KEY_PATH, destFile.getParent());
      }
      JOptionPane.showMessageDialog(frmEvesDropper, "Export Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (JAXBException | IOException e1) {
      e1.printStackTrace();
      JOptionPane.showMessageDialog(frmEvesDropper, e1.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setupJIFLog() {

    logTableModel = new LogMessageTableModel();

    jifLog = new CustomBorderedInternalFrame("Log");

    try {
      jifLog.setFrameIcon(GuiConstants.ICON_LOG);
    } catch (Exception e) {
    }
    jifLog.getContentPane().setLayout(new BorderLayout());
    jifLog.setResizable(true);
    jifLog.setMaximizable(true);
    jifLog.setIconifiable(true);
    jifLog.setBounds(0, 301, 128, 109);
    desktopPane.add(jifLog);

    jtfFilterLog = new LibSexyTextField();
    jtfFilterLog.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        onLogTextChanged();
      }
    });
    jtfFilterLog.addClearListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onLogTextChanged();
      }
    });
    jifLog.getContentPane().add(jtfFilterLog, BorderLayout.NORTH);
    jtfFilterLog.setColumns(10);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setAutoscrolls(true);
    jifLog.getContentPane().add(scrollPane, BorderLayout.CENTER);

    jtbLog = new JTable();
    scrollPane.setViewportView(jtbLog);
    jtbLog.setFillsViewportHeight(true);
    jtbLog.setModel(logTableModel);

    jtbLog.getColumnModel().getColumn(0).setPreferredWidth(20);
    jtbLog.getColumnModel().getColumn(1).setPreferredWidth(200);
    jtbLog.getColumnModel().getColumn(2).setPreferredWidth(800);
    jtbLog.getColumnModel().getColumn(0).setMinWidth(20);
    jtbLog.getColumnModel().getColumn(0).setMaxWidth(20);
    jtbLog.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int sel = jtbLog.getSelectedRow();
        if (sel < 0 || e.getButton() != MouseEvent.BUTTON3)
          return;
        final String logMsg = (String) jtbLog.getValueAt(sel, 2);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem jmiUnhook = new JMenuItem("Unhook...");
        jmiUnhook.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            String classToUnhook = JOptionPane.showInputDialog(jifHooker, "Class to Unhook", logMsg);
            if (classToUnhook != null) {
              RuntimeConfig conf = (RuntimeConfig) Injector.get(RuntimeConfig.class);
              HashSet<String> unhook = new HashSet<>();
              unhook.add(classToUnhook);
              try {
                networkCommunication.write(CommandBuilder.buildForHookUnhookClassCommand(conf.getCurrentPackage(),
                    unhook, false));
              } catch (ConnectException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
            }
          }
        });
        menu.add(jmiUnhook);
        System.out.println("POPUP");
        menu.show(jtbLog, e.getX(), e.getY());
      }
    });

    jifLog.setVisible(true);
  }

  private void setupJIFClasses() {

    classTreeModel = new ClassTreeModel(new DefaultMutableTreeNode("Classes"));
    DefaultMutableTreeNode invocationTreeRootNode = new DefaultMutableTreeNode("Invocations");
    invocationTreeModel = new InvocationTreeModel(invocationTreeRootNode);
    invocationTreeController = new InvocationTreeController(invocationTreeModel, invocationTreeRootNode);

    jifClasses = new CustomBorderedInternalFrame("Classes");
    try {
      jifClasses.setFrameIcon(GuiConstants.ICON_CLASSES);
    } catch (Exception e) {
    }
    jifClasses.getContentPane().setLayout(new BorderLayout());
    jifClasses.setMaximizable(true);
    jifClasses.setIconifiable(true);
    jifClasses.setResizable(true);
    jifClasses.setBounds(12, 12, 386, 277);
    desktopPane.add(jifClasses);

    jfFilterNodes = new LibSexyTextField();
    jfFilterNodes.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        onNodeTextChanged(jtClassesMaster);
      }
    });
    jfFilterNodes.addClearListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        onNodeTextChanged(jtClassesMaster);
      }
    });

    jifClasses.getContentPane().add(jfFilterNodes, BorderLayout.NORTH);
    jfFilterNodes.setColumns(10);

    JSplitPane jspMasterDetail = new JSplitPane();
    jspMasterDetail.setContinuousLayout(true);
    jspMasterDetail.setResizeWeight(0.5);
    jifClasses.getContentPane().add(jspMasterDetail, BorderLayout.CENTER);

    JScrollPane scrollPane_2 = new JScrollPane();
    jspMasterDetail.setRightComponent(scrollPane_2);

    JTree jtDetail = new JTree();
    jtDetail.setModel(invocationTreeController);
    jtDetail.setCellRenderer(new ClassTreeCellRenderer());
    scrollPane_2.add(jtDetail);
    scrollPane_2.setViewportView(jtDetail);

    JScrollPane scrollPane_1 = new JScrollPane();

    jspMasterDetail.setLeftComponent(scrollPane_1);
    jtClassesMaster = new JTree();
    jtClassesMaster.setModel(classTreeModel);
    jtClassesMaster.setCellRenderer(new ClassTreeCellRenderer());
    scrollPane_1.add(jtClassesMaster);
    scrollPane_1.setViewportView(jtClassesMaster);
    jspMasterDetail.setDividerLocation(0.5);

    jtClassesMaster.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        Object leaf = e.getPath().getLastPathComponent();

        if (leaf instanceof MethodTreeNode) {
          MethodModel methodModel = ((MethodTreeNode) leaf).getMethodModel();
          String methodName = methodModel.getUniqueMethodName();

          invocationTreeController.selectMethod(methodName);

          decompileWrapper.decompile(methodName);
        }
      }
    });

    classTreeMouseListener = new ClassTreeMouseListener(jtClassesMaster);
    jtClassesMaster.addMouseListener(classTreeMouseListener);
    jifClasses.setVisible(true);
  }

  private void setupJIFCallGraph() {

    jifCallGraph = new CustomBorderedInternalFrame("Execution Trace");

    try {
      jifCallGraph.setFrameIcon(GuiConstants.ICON_CG);
    } catch (Exception e) {
    }

    jifCallGraph.getContentPane().setLayout(new BorderLayout());
    jifCallGraph.setResizable(true);
    jifCallGraph.setMaximizable(true);
    jifCallGraph.setIconifiable(true);
    jifCallGraph.setBounds(410, 12, 373, 277);

    desktopPane.add(jifCallGraph);

    initCallGraphGui();

    jifCallGraph.setVisible(true);
  }

  private void setupJIFSourceCode() {
    jifSourceCode = new CustomBorderedInternalFrame("Source Code");
    try {
      jifSourceCode.setFrameIcon(GuiConstants.ICON_SRC);
    } catch (Exception e) {
    }
    jifSourceCode.getContentPane().setLayout(new BorderLayout());
    jifSourceCode.setResizable(true);
    jifSourceCode.setMaximizable(true);
    jifSourceCode.setIconifiable(true);
    jifSourceCode.setBounds(350, 12, 373, 277);
    jifSourceCode.setLayout(new BorderLayout());
    desktopPane.add(jifSourceCode);

    tfSourceCode = new RSyntaxTextArea(20, 80);
    tfSourceCode.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);

    JScrollPane sourceCodeScrollPane = new JScrollPane(tfSourceCode);
    jifSourceCode.add(sourceCodeScrollPane, BorderLayout.CENTER);

    jifSourceCode.setVisible(true);
  }

  private void setupJIFTimeline() {

    timelineTableModel = new TimelineTableModel();
    timelineTableModel.addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == 0 && e.getFirstRow() > -1) {
          DBInvocation invocation = timelineTableModel.getInvocationAt(e.getFirstRow());
          try {
            dbUtil.updateInvocationInteresting(invocation.getId(), invocation.isInteresting());
          } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
      }
    });

    jifTimeline = new CustomBorderedInternalFrame("Timeline");
    jifTimeline.setClosable(false);
    try {
      jifTimeline.setFrameIcon(GuiConstants.ICON_LOG);
    } catch (Exception e) {
    }

    jifTimeline.getContentPane().setLayout(new BorderLayout());
    jifTimeline.setResizable(true);
    jifTimeline.setIconifiable(true);
    jifTimeline.setMaximizable(true);
    jifTimeline.setClosable(false);
    jifTimeline.setBounds(50, 300, 400, 300);
    desktopPane.add(jifTimeline);

    jtbTimeline = new JTable();
    jspTimeline = new JScrollPane(jtbTimeline);
    jspTimeline.setViewportView(jtbTimeline);
    jtbTimeline.setFillsViewportHeight(true);
    jtbTimeline.setModel(timelineTableModel);

    jtaInvocationDetail = new JTextArea();
    jtaInvocationDetail.setEditable(false);
    jtaInvocationDetail.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    jtaInvocationDetail.setLineWrap(false);
    JScrollPane jspInvocationDetail = new JScrollPane(jtaInvocationDetail);
    jspInvocationDetail.setViewportView(jtaInvocationDetail);

    JSplitPane jspTimelineInvocation = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspTimeline, jspInvocationDetail);
    jspTimelineInvocation.setResizeWeight(0.5d);

    jtbTimeline.getColumnModel().getColumn(0).setPreferredWidth(20);
    jtbTimeline.getColumnModel().getColumn(1).setPreferredWidth(150);
    jtbTimeline.getColumnModel().getColumn(2).setPreferredWidth(150);
    jtbTimeline.getColumnModel().getColumn(3).setPreferredWidth(200);
    jtbTimeline.getColumnModel().getColumn(4).setPreferredWidth(200);
    jtbTimeline.getColumnModel().getColumn(0).setMinWidth(20);
    jtbTimeline.getColumnModel().getColumn(0).setMaxWidth(20);

    jtbTimeline.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int sel = jtbTimeline.getSelectedRow();
        jtaInvocationDetail.setText("");
        StringBuilder bld = new StringBuilder("Class:   ").append((String) jtbTimeline.getValueAt(sel, 1)).append("\n");
        bld.append("Method:  ").append((String) jtbTimeline.getValueAt(sel, 2)).append("\n");
        bld.append("Args:    ").append((String) jtbTimeline.getValueAt(sel, 3)).append("\n\n");
        bld.append("Returns: ").append((String) jtbTimeline.getValueAt(sel, 4)).append("\n\n");
        bld.append("Time:    ").append((String) jtbTimeline.getValueAt(sel, 5)).append("\n\n");
        jtaInvocationDetail.setText(bld.toString());

        synchronized (callGraph) {
          callGraph.clear();
          long id = (long) timelineTableModel.getInvocationAt(sel).getId();
          dbUtil.createInvocationTrace(null, callGraph, id, true);
          dbUtil.createInvocationTrace(null, callGraph, id, false);
          callGraph.selectNode(id);
          jungGraph.draw();
        }
      }
    });
    
    jtbTimeline.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
        	
        	final int column = jtbTimeline.columnAtPoint(e.getPoint());
            int r = jtbTimeline.rowAtPoint(e.getPoint());
            
            if (r >= 0 && r < jtbTimeline.getRowCount()) {
            	jtbTimeline.setRowSelectionInterval(r, r);
            } else {
            	jtbTimeline.clearSelection();
            }

            int rowindex = jtbTimeline.getSelectedRow();
            if (rowindex < 0 && column < jtbTimeline.getColumnCount())
                return;
            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                JPopupMenu popupmenu = new JPopupMenu();
                final String content = (String)( (TimelineTableModel)jtbTimeline.getModel()).getValueAt(rowindex, column);
                
                JMenuItem hideItem = new JMenuItem("Hide all: " + content);
                hideItem.addActionListener(new ActionListener() {
            		@Override
            		public void actionPerformed(ActionEvent e) {
            			try {
							dbUtil.hideTimeLineItems(jtbTimeline.getColumnName(column), content, 1);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            		}
            		});
                
                JMenuItem selectItem = new JMenuItem("Select Only: " + content);
                selectItem.addActionListener(new ActionListener() {
            		@Override
            		public void actionPerformed(ActionEvent e) {
            			try {
							dbUtil.hideTimeLineItems(jtbTimeline.getColumnName(column), content, 0);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            		}
            		});
                
                JMenuItem restoreItem = new JMenuItem("Restore");
                restoreItem.addActionListener(new ActionListener() {
            		@Override
            		public void actionPerformed(ActionEvent e) {
            			
            			try {
							dbUtil.hideTimeLineItems(jtbTimeline.getColumnName(column), content, 2);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            		}
            		});
                
                popupmenu.add(hideItem);
                popupmenu.add(selectItem);
                popupmenu.addSeparator();
                popupmenu.add(restoreItem);
                popupmenu.addSeparator();
                
            	if(column == 3) {
            		List<String> paramList = JavaNameHelper.getParameters(content);
            		
            		if(paramList.size() > 0)
            		{
            			JMenuItem jmenuDes = new JMenuItem("Add to Password");
            			jmenuDes.setEnabled(false);
            			popupmenu.add(jmenuDes);
        				popupmenu.addSeparator();
            			
            			for(final String item : paramList) {
            				JMenuItem jmenuItem = new JMenuItem(item);
            				jmenuItem.addActionListener(new ActionListener() {
        	            		@Override
        	            		public void actionPerformed(ActionEvent e) {
        	            			 if(!passwordList.contains(item))
        	            				  passwordList.addElement(item);
        	            			 }
        	            		});
            				
            				popupmenu.add(jmenuItem);
            			}
            		}
            		
            	} else if(column == 4) {
            		
            		List<String> paramList = JavaNameHelper.getParameters(content);
            		
            		if(paramList.size() > 0)
            		{
            			JMenuItem jmenuDes = new JMenuItem("Add to Password");
            			jmenuDes.setEnabled(false);
            			JMenuItem jmenuItem = new JMenuItem(paramList.get(0));
        				jmenuItem.addActionListener(new ActionListener() {
    	            		@Override
    	            		public void actionPerformed(ActionEvent e) {
    	            			 if(!passwordList.contains(content))
    	            				  passwordList.addElement(content);
    	            			 }
    	            		});
        				popupmenu.add(jmenuDes);
        				popupmenu.addSeparator();
        				popupmenu.add(jmenuItem);
            		}
            	}
                
                
                
                
                popupmenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    });

    jtfFilterTimeline = new HistoryTextField();
    jtfFilterTimeline.setText(GuiConstants.DEFAULT_QUERY);
    jtfFilterTimeline.invoke();
    jtfFilterTimeline.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER)
          jtfFilterTimeline.invoke();
      }
    });
    jtfFilterTimeline.addClearListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jtfFilterTimeline.setText(GuiConstants.DEFAULT_QUERY);
        jtfFilterTimeline.invoke();
      }
    });

    JPanel jpSQL = new JPanel(new BorderLayout());
    JTextField jtfSQLSelect = new JTextField("SELECT * FROM invocations");
    jtfSQLSelect.setEditable(false);
    jtfSQLSelect.setFocusable(false);
    jpSQL.add(jtfSQLSelect, BorderLayout.WEST);
    jpSQL.add(jtfFilterTimeline, BorderLayout.CENTER);

    jifTimeline.add(jpSQL, BorderLayout.NORTH);
    jifTimeline.add(jspTimelineInvocation, BorderLayout.CENTER);

    jifTimeline.setVisible(true);
  }

  private void setupJIFHooker() {

    hookTableModel = new PackageConfigTableModel();
    hookTableModel.addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == 0 && e.getFirstRow() > -1) {
          onHook((String) hookTableModel.getValueAt(e.getFirstRow(), 2),
              (Boolean) hookTableModel.getValueAt(e.getFirstRow(), 0));
        }
      }
    });

    jifHooker = new CustomBorderedInternalFrame("Hooks");

    try {
      jifHooker.setFrameIcon(GuiConstants.ICON_HOOKS);
    } catch (Exception e) {
    }
    jifHooker.setResizable(true);
    jifHooker.setIconifiable(true);
    jifHooker.setMaximizable(true);
    jifHooker.setBounds(140, 300, 260, 100);
    desktopPane.add(jifHooker);

    jtbHooks = new JTable();
    jspHooks = new JScrollPane(jtbHooks);
    jspHooks.setViewportView(jtbHooks);
    jtbHooks.setFillsViewportHeight(true);
    jtbHooks.setModel(hookTableModel);
    jtbHooks.getColumnModel().getColumn(0).setPreferredWidth(20);
    jtbHooks.getColumnModel().getColumn(1).setPreferredWidth(200);
    jtbHooks.getColumnModel().getColumn(2).setPreferredWidth(800);
    jtbHooks.getColumnModel().getColumn(0).setMinWidth(20);
    jtbHooks.getColumnModel().getColumn(0).setMaxWidth(20);

    jifHooker.getContentPane().add(jspHooks, BorderLayout.CENTER);

    jtfFilterPkgs = new LibSexyTextField();
    jtfFilterPkgs.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        onPkgTextChanged();
      }
    });
    jtfFilterPkgs.addClearListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onPkgTextChanged();
      }
    });
    jifHooker.add(jtfFilterPkgs, BorderLayout.NORTH);

    jifHooker.setVisible(true);
  }

  private void setupJIFBWList() {
    jifBWList = new CustomBorderedInternalFrame("Black-/Whitelist");
    try {
      jifBWList.setFrameIcon(GuiConstants.ICON_BW);
    } catch (Exception e) {
    }
    jifBWList.setLayout(new BorderLayout());
    jifBWList.setResizable(true);
    jifBWList.setMaximizable(true);
    jifBWList.setIconifiable(true);
    jifBWList.setBounds(0, 12, 373, 277);
    ButtonGroup grpMode = new ButtonGroup();
    jrbPureWhiteList = new JRadioButton("Pure Whitelist");
    jrbRegularMode = new JRadioButton("Blacklist");
    grpMode.add(jrbPureWhiteList);
    grpMode.add(jrbRegularMode);
    jrbRegularMode.setSelected(true);
    jrbPureWhiteList.setSelected(false);

    JPanel jpTop = new JPanel(new BorderLayout());
    final JTextField jtfPath = new JTextField();
    jtfPath.setEnabled(false);
    JButton jbuBrowse = new JButton("Browse");
    jbuBrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();
        int res = jfc.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
          try {
            jtaBlackWhiteList.setText("");
            jtfPath.setText(jfc.getSelectedFile().getCanonicalPath());
            BufferedReader br = new BufferedReader(new FileReader(jfc.getSelectedFile()));
            String aLine = null;
            while ((aLine = br.readLine()) != null) {
              jtaBlackWhiteList.append(aLine);
              jtaBlackWhiteList.append("\n");
            }
            br.close();
          } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }

        }
      }
    });
    jpTop.add(jbuBrowse, BorderLayout.WEST);
    jpTop.add(jtfPath, BorderLayout.CENTER);
    jifBWList.add(jpTop, BorderLayout.NORTH);
    JPanel jpBot = new JPanel(new BorderLayout());
    jpBot.add(jrbPureWhiteList, BorderLayout.WEST);
    jpBot.add(jrbRegularMode, BorderLayout.CENTER);
    jifBWList.add(jpBot, BorderLayout.SOUTH);
    jtaBlackWhiteList = new JTextArea();
    JScrollPane jspBWList = new JScrollPane(jtaBlackWhiteList);
    jifBWList.add(jspBWList, BorderLayout.CENTER);
    desktopPane.add(jifBWList);
    jifBWList.setVisible(true);
    
  }
  
  private void setupJIFImport() {
	  jifImport = new CustomBorderedInternalFrame("Import");
	  try {
		  jifImport.setFrameIcon(GuiConstants.ICON_BW);
	  } catch (Exception e) {
	  }
	  jifImport.setLayout(new BorderLayout());
	  jifImport.setResizable(true);
	  jifImport.setMaximizable(true);
	  jifImport.setIconifiable(true);
	  jifImport.setClosable(false);
	  jifImport.setBounds(50, 300, 400, 300);
	  
	  passwordList = new DefaultListModel<String>();
	  passwordTableModel = new TimelineTableModel();
	  functionInvocation = new TimelineTableModel();
	  differentFunctionInvocation = new TimelineTableModel();
	  
	  invocationList = new ArrayList<DBInvocation>();
	  origin = null;
	  
	  JButton jbuImport = new JButton("Import");
	  
	  jbuImport.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	    	  // import data
	    	  try {
	    		  Path folder = importUnzip();
	    		  if(folder == null) return;
	    		  
	            	// Import Apk files
	            	System.out.println("Import apk files");
	            	File outputDir = new File(folder.toString());
	            	
	            	File []jarFiles = 
	            			outputDir.listFiles(new FilenameFilter() { 
	            				public boolean accept(File outputDir, String filename)
	            				{ return filename.endsWith(".jar"); }
	            			} );
	            	
	            	if(jarFiles.length > 0)
	            		moustacheClassLoader.ApkFileImport(jarFiles[0]);
	            	
	            	System.out.println("Import apk done");
	            	
	            	
	            	System.out.println("Import database");
	            	File dbFile = new File(folder.toString() + File.separator + "magnum.sqlite");
	            	
	            	if(!dbFile.exists())
	            	{
	            		System.out.println("Databasefile doesn't exist");
	            		return;
	            	}
	            	dbUtil.dbImport(dbFile.getAbsolutePath());
	            	System.out.println("Import Done");
	            	
	            	findPasswords();
	            	
	            	
	            
	            } catch (IOException e1) {
	              // TODO Auto-generated catch block
	              e1.printStackTrace();
	            }
	          }
	  });
	  
	  
	  JButton jbuFindPasswords = new JButton("Find Passwords");
	  
	  jbuFindPasswords.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	    	  findPasswords();
	      }
	  });
	  
	  jLstPasswords = new JList<String>(passwordList);
	  jspPasswords = new JScrollPane(jLstPasswords);
	  jLstPasswords.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	  //jLstPasswords.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	  jLstPasswords.setVisible(true);
	  jLstPasswords.setVisibleRowCount(20);
	  
	  jLstPasswords.getSelectionModel().addListSelectionListener(
			  new ListSelectionListener() {
				  public void valueChanged(ListSelectionEvent e) {
					  String value = (String)jLstPasswords.getSelectedValue();
					  passwordTableModel.setContent(dbUtil.findPasswords(value));
					  doRepaint(jspPasswordTimeline);
				  }
			  });
	   
	  
	  jtbPasswordTimeline = new JTable();
	  jspPasswordTimeline = new JScrollPane(jtbPasswordTimeline);
	  jspPasswordTimeline.setViewportView(jtbPasswordTimeline);
	  jtbPasswordTimeline.setFillsViewportHeight(true);
	  jtbPasswordTimeline.setModel(passwordTableModel);
	  
	  jtbPasswordTimeline.getColumnModel().getColumn(0).setPreferredWidth(20);
	  jtbPasswordTimeline.getColumnModel().getColumn(1).setPreferredWidth(150);
	  jtbPasswordTimeline.getColumnModel().getColumn(2).setPreferredWidth(130);
	  jtbPasswordTimeline.getColumnModel().getColumn(3).setPreferredWidth(200);
	  jtbPasswordTimeline.getColumnModel().getColumn(4).setPreferredWidth(200);
	  jtbPasswordTimeline.getColumnModel().getColumn(0).setMinWidth(20);
	  jtbPasswordTimeline.getColumnModel().getColumn(0).setMaxWidth(20);
	  jtbPasswordTimeline.setVisible(true);
	  
	  jtbPasswordTimeline.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
	        int sel = jtbPasswordTimeline.getSelectedRow();
	        
	        invocationList.clear();

	        synchronized (callGraph) {
	          callGraph.clear();
	          origin = passwordTableModel.getInvocationAt(sel);
	          long id = (long) origin.getId();
	          dbUtil.createInvocationTrace(invocationList, callGraph, id, true);
	          dbUtil.createInvocationTrace(invocationList, callGraph, id, false);
	          callGraph.selectNode(id);
	          jungGraph.draw();
	          
	          sortDBInvocationList(invocationList);
	          functionInvocation.setContent(invocationList);
	          doRepaint(jspFunctionInvocation);
	        }
	        
          if(dbUtil.isConnectionDiffSet())
          {
        	  List<DBInvocation> diffList = findInvocationDiff(invocationList, origin);
        	  differentFunctionInvocation.setContent(diffList);
        	  doRepaint(jspDifferentFunctionInvocation);
          }

	        
	      }
	    });
	  
	  jtbPasswordTimeline.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseReleased(MouseEvent e) {
	        	
	        	final int column = jtbPasswordTimeline.columnAtPoint(e.getPoint());
	            int r = jtbPasswordTimeline.rowAtPoint(e.getPoint());
	            
	            if (r >= 0 && r < jtbTimeline.getRowCount()) {
	            	jtbPasswordTimeline.setRowSelectionInterval(r, r);
	            } else {
	            	jtbPasswordTimeline.clearSelection();
	            }

	            int rowindex = jtbPasswordTimeline.getSelectedRow();
	            if (rowindex < 0 && column < jtbPasswordTimeline.getColumnCount())
	                return;
	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
	            	
	            	final String content = (String)( (TimelineTableModel)jtbPasswordTimeline.getModel()).getValueAt(rowindex, column);
	            	
	            	if(column == 3) {
	            		List<String> paramList = JavaNameHelper.getParameters(content);
	            		
	            		if(paramList.size() > 0)
	            		{
	            			JPopupMenu popupmenu = new JPopupMenu();
	            			JMenuItem jmenuDes = new JMenuItem("Add to Password");
	            			jmenuDes.setEnabled(false);
	            			popupmenu.add(jmenuDes);
            				popupmenu.addSeparator();
	            			
	            			for(final String item : paramList) {
	            				JMenuItem jmenuItem = new JMenuItem(item);
	            				jmenuItem.addActionListener(new ActionListener() {
	        	            		@Override
	        	            		public void actionPerformed(ActionEvent e) {
	        	            			 if(!passwordList.contains(item))
	        	            				  passwordList.addElement(item);
	        	            			 }
	        	            		});
	            				
	            				popupmenu.add(jmenuItem);
	            			}
	            			popupmenu.show(e.getComponent(), e.getX(), e.getY());
	            		}
	            		
	            	} else if(column == 4) {
	            		
	            		List<String> paramList = JavaNameHelper.getParameters(content);
	            		
	            		if(paramList.size() > 0)
	            		{
	            			JPopupMenu popupmenu = new JPopupMenu("Add to Password");
	            			JMenuItem jmenuDes = new JMenuItem("Add to Password");
	            			jmenuDes.setEnabled(false);
	            			JMenuItem jmenuItem = new JMenuItem(paramList.get(0));
            				jmenuItem.addActionListener(new ActionListener() {
        	            		@Override
        	            		public void actionPerformed(ActionEvent e) {
        	            			 if(!passwordList.contains(content))
        	            				  passwordList.addElement(content);
        	            			 }
        	            		});
            				popupmenu.add(jmenuDes);
            				popupmenu.addSeparator();
            				popupmenu.add(jmenuItem);
            				popupmenu.show(e.getComponent(), e.getX(), e.getY());
	            		}
	            	} 
	
	            }
	        }
	  });
	  
	  JButton jbuImport2 = new JButton("Import 2nd DB");
	  jbuImport2.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	    	  // import data
	    	  try {
	    		Path folder = importUnzip();
	    		
	    		if(folder == null) return;
            	
            	System.out.println("Import database");
            	File dbFile = new File(folder.toString() + File.separator + "magnum.sqlite");
            	
            	if(!dbFile.exists())
            	{
            		System.out.println("Databasefile doesn't exist");
            		return;
            	}
            	dbUtil.dbImportDifferentDB(dbFile.getAbsolutePath());
            	System.out.println("Import Done");	
	            
	            } catch (IOException e1) {
	              // TODO Auto-generated catch block
	              e1.printStackTrace();
	            }
	          }
	            
	  	});
	  
	  
	  jtbFunctionInvocation = new JTable();
	  jspFunctionInvocation = new JScrollPane(jtbFunctionInvocation, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  jspFunctionInvocation.setViewportView(jtbFunctionInvocation);
	  jtbFunctionInvocation.setFillsViewportHeight(true);
	  jtbFunctionInvocation.setModel(functionInvocation);
	  
	  jtbFunctionInvocation.getColumnModel().getColumn(0).setPreferredWidth(20);
	  jtbFunctionInvocation.getColumnModel().getColumn(1).setPreferredWidth(150);
	  jtbFunctionInvocation.getColumnModel().getColumn(2).setPreferredWidth(130);
	  jtbFunctionInvocation.getColumnModel().getColumn(3).setPreferredWidth(200);
	  jtbFunctionInvocation.getColumnModel().getColumn(4).setPreferredWidth(200);
	  jtbFunctionInvocation.getColumnModel().getColumn(0).setMinWidth(20);
	  jtbFunctionInvocation.getColumnModel().getColumn(0).setMaxWidth(20);
	  jtbFunctionInvocation.setVisible(true);
	  jtbFunctionInvocation.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	  
	  
	  jtbDifferentFunctionInvocation = new JTable();
	  jspDifferentFunctionInvocation = new JScrollPane(jtbDifferentFunctionInvocation, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  jspDifferentFunctionInvocation.setViewportView(jtbDifferentFunctionInvocation);
	  jtbDifferentFunctionInvocation.setFillsViewportHeight(true);
	  jtbDifferentFunctionInvocation.setModel(differentFunctionInvocation);
	  
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(0).setPreferredWidth(20);
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(1).setPreferredWidth(150);
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(2).setPreferredWidth(130);
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(3).setPreferredWidth(200);
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(4).setPreferredWidth(200);
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(0).setMinWidth(20);
	  jtbDifferentFunctionInvocation.getColumnModel().getColumn(0).setMaxWidth(20);
	  jtbDifferentFunctionInvocation.setVisible(true);
	  jtbDifferentFunctionInvocation.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	  
	  
	  
	  JPanel jpTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
	  jpTop.add(jbuImport);
	  jpTop.add(jbuFindPasswords);
	  jpTop.add(jbuImport2);
	  jifImport.add(jpTop, BorderLayout.NORTH);
	  
	  JPanel jpBot = new JPanel();
	  GroupLayout groupLayout = new GroupLayout(jpBot);
	  jpBot.setLayout(groupLayout);
	  groupLayout.setAutoCreateGaps(true);
	  groupLayout.setAutoCreateContainerGaps(true);
	  
	  groupLayout.setHorizontalGroup(groupLayout
			  .createParallelGroup(GroupLayout.Alignment.LEADING)
			  	   .addGroup(groupLayout.createSequentialGroup()
			  		   .addComponent(jspPasswords, 0, GroupLayout.DEFAULT_SIZE, 200)
			  		   .addComponent(jspPasswordTimeline))
			  	);
	  
	  groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
			    .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			        .addComponent(jspPasswords)
			        .addComponent(jspPasswordTimeline)));
	  
	  jifImport.add(jpBot, BorderLayout.CENTER);
	  
	  // Bottom
	  JPanel jpBottom = new JPanel(new BorderLayout());
	  GroupLayout bottomGroupLayout = new GroupLayout(jpBottom);
	  jpBottom.setLayout(bottomGroupLayout);
	  bottomGroupLayout.setAutoCreateGaps(true);
	  bottomGroupLayout.setAutoCreateContainerGaps(true);
	  
	  bottomGroupLayout.setHorizontalGroup(bottomGroupLayout
			  .createParallelGroup(GroupLayout.Alignment.LEADING)
			  	   .addGroup(bottomGroupLayout.createSequentialGroup()
			  		   .addComponent(jspFunctionInvocation)
			  		   .addComponent(jspDifferentFunctionInvocation))
			  	);
	  
	  bottomGroupLayout.setVerticalGroup(bottomGroupLayout.createSequentialGroup()
			    .addGroup(bottomGroupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			        .addComponent(jspFunctionInvocation)
			        .addComponent(jspDifferentFunctionInvocation)));
	  
	  
	  jpBottom.setPreferredSize(new Dimension(100, 350));
	  //jpBottom.add(jspFunctionInvocation, BorderLayout.WEST);
	  //jpBottom.add(jspDifferentFunctionInvocation, BorderLayout.EAST);
	  jifImport.add(jpBottom, BorderLayout.SOUTH);
	  
	  desktopPane.add(jifImport);
	  jifImport.setVisible(true);
	  
	  
  }

  private void onConnect(String[] addr) throws IOException {
    classList = new SortedList<>();

    initializeEmitters();
    new Thread(dbUtil).start();

    networkCommunication.connect(addr[0], Integer.parseInt(addr[1]));

    final Future<Boolean> future = dispatcher.submit(messageReceiver);
    networkCommunication.write(CommandBuilder.buildforRequestPackageConfigCommand());
    jtaBlackWhiteList.setEnabled(false);

    try {
      BlackWhiteListContainer container = new BWListParser().parse(jtaBlackWhiteList.getText());
      networkCommunication.write(CommandBuilder.buildForBWListCmd(jrbPureWhiteList.isSelected(), container.packages,
          container.packageWildcards, container.classes, container.classesWildcards, container.methods,
          container.methodWildcards));
      jtaBlackWhiteList.setDisabledTextColor(new Color(0, 100, 0));
      globalWBList = true;
    } catch (Exception e1) {
      e1.printStackTrace();
      networkCommunication.write(CommandBuilder.buildForBWListCmd(false, new HashSet<String>(), new HashSet<String>(),
          new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashSet<String>()));
      jtaBlackWhiteList.setDisabledTextColor(Color.RED);
    }
    dispatcher.execute(new Runnable() {
      @Override
      public void run() {
        try {
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        } finally {
          onDisconnect();
        }
      }
    });

    setConnectionState(true);

    try {
      LolCat.connectIfNecessary(addr[0]);
    } catch (Exception exx) {
    }
    new Thread() {
      public void run() {
        try {
          BufferedReader lolcat = LolCat.getReader();
          String aLine = null;
          while ((aLine = lolcat.readLine()) != null) {

            TYPE type = TYPE.I;
            try {
              type = TYPE.valueOf(Character.toString(aLine.charAt(0)));
            } catch (Exception e) {
            }
            
            if (aLine.length() >= 1)
            {
	            	String[] line = aLine.charAt(1) == '/' ? aLine.substring(2).split(":", 2) : aLine.split(":", 2);
	            
	            if (!line[0].contains("StrictMode")) {
	              if (line.length < 2)
	                logTableModel.addLogMessage(type, "", aLine);
	              else
	                logTableModel.addLogMessage(type, line[0], line[1]);
	            }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    }.start();
  }

  private void onDisconnect() {
    messageReceiver.finishAndShutdown();
    setConnectionState(false);
  }

  private void onLogTextChanged() {
    if (jtfFilterLog.getText().length() == 0)
      logTableModel.filterBy(null);
    else
      logTableModel.filterBy(jtfFilterLog.getText());
    doRepaint(jtbHooks);
  }

  private void doRepaint(final JComponent comp) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        comp.revalidate();
        comp.updateUI(); // TODO: INVESTIGATE
      }
    });
  }

  private void onPkgTextChanged() {
    if (jtfFilterPkgs.getText().length() == 0)
      hookTableModel.filterBy(null);
    else
      hookTableModel.filterBy(jtfFilterPkgs.getText());
    doRepaint(jtbHooks);
  }

  private void onNodeTextChanged(JTree tree) {
    if (jfFilterNodes.getText().length() == 0) {
      tree.setModel(classTreeModel);
    } else {
      tree.setModel(classTreeModel.filterBy(jfFilterNodes.getText()));
    }
    doRepaint(tree);
  }

  private void setConnectionState(boolean state, String info) {
    jtfIP.setEditable(!state);
    btnExport.setEnabled(state);
    jbuConnect.setEnabled(!state);
    jlbState.setForeground(state ? Color.GREEN : Color.RED);
    jlbStatus.setText(info);
    jlbStatus.setIcon(state ? GuiConstants.ICON_CONNECTED : GuiConstants.ICON_DISCONECTED);
  }

  private void setConnectionState(boolean state) {
    setConnectionState(state, state ? ("Connected to " + jtfIP.getText()) : "Not Connected");

    if (state) {
      ConfFile.set(ConfFile.KEY_IP, jtfIP.getText());
      ConfFile.store();
    }
  }

  private void loadWindowStates() {
    Component[] components = { frmEvesDropper, jifCallGraph, jifClasses, jifSourceCode, jifLog, jifHooker, jifTimeline,
        jifBWList, jifImport };
    WindowStateManager.load(components);
  }

  private void saveWindowStates() {
    ConfFile.store();
  }

  private void createJIFButton(final CustomBorderedInternalFrame jif) {
    JButton jbuJif = new JButton(jif.getTitle(), jif.getFrameIcon());
    jbuJif.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        jif.toFront();

      }
    });
    jtbStatus.add(jbuJif);
  }

  private void initializeEmitters() {
    emitterRegistry.registerListener(ClassModel.class, new EmitListener<ClassModel>() {
      @Override
      public void update(ClassModel classModel) {
        classList.add(classModel);
        classTreeModel.addClassModel(classModel);
      }
    });

    emitterRegistry.registerListener(LogMessageModel.class, new EmitListener<LogMessageModel>() {
      @Override
      public void update(LogMessageModel modelObject) {
        logTableModel.addLogMessage(TYPE.I, "Log", modelObject.getMessage());
      }
    });

    emitterRegistry.registerListener(PackageConfigModel.class, new EmitListener<PackageConfigModel>() {
      @Override
      public void update(PackageConfigModel modelObject) {
        // jpHooks.removeAll();
        hookTableModel.clear();
        TreeSet<String> sortedPkg = new TreeSet<>();
        sortedPkg.addAll(modelObject.getPackageConfig().keySet());
        RuntimeConfig conf = (RuntimeConfig) Injector.get(RuntimeConfig.class);

        conf.setPackageConfig(modelObject.getPackageConfig());

        for (String pkg : sortedPkg) {
          PackageConfig pConf = modelObject.getPackageConfig().get(pkg);
          hookTableModel.addPackageStatus(pConf.isHooked(), pConf.getAppName(), pConf.getPkg());
          if (pConf.isHooked())// work around service bug
            onHook(pConf.getPkg(), true);
          for (String clazz : pConf.getUnhookedClasses()) {
            ClassModel unhookedClass = new ClassModel(clazz, 0, "[Unhooked!] " + clazz);
            classList.add(unhookedClass);
            classTreeModel.addClassModel(unhookedClass);
          }
        }
        jtbHooks.repaint(100);
      }
    });

    emitterRegistry.registerListener(MethodInvocationModel.class, new EmitListener<MethodInvocationModel>() {
      @Override
      public void update(MethodInvocationModel invocationObject) {
        // TODO: everything done here?
        invocationTreeModel.addInvocation(invocationObject);
        dbUtil.write(invocationObject);
      }
    });

    emitterRegistry.registerListener(ApkModel.class, new EmitListener<ApkModel>() {
      @Override
      public void update(ApkModel modelObject) {
        RuntimeConfig conf = (RuntimeConfig) Injector.get(RuntimeConfig.class);
        conf.setCurrentPackage(modelObject.getPkgName());
        try {
          moustacheClassLoader.initializeForApk(modelObject);
          logTableModel.addLogMessage(TYPE.I, "dex2jar", "Converted dex2jar");

          // callGraph = new CallGraph();

          logTableModel.addLogMessage(TYPE.I, "jung", "Created CallGraph");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public class DecompileWrapper {
    public void decompile(final String methodName, final boolean show) {
      Continuation<String> decompileContinuation = new Continuation<String>() {

        @Override
        public void onContinue(String src) {
          tfSourceCode.setText(src);
          if (show)
            jifSourceCode.toFront();
        }

        @Override
        public void onError() {
          System.out.println("Could not decompile");
          logTableModel.addLogMessage(TYPE.W, "Procyon", "Could not decompile `" + methodName + "`");
        }
      };

      moustacheDecompiler.decompile(methodName, decompileContinuation);
    }

    public void decompile(final String methodName) {
      decompile(methodName, false);
    }
  };
  
  public void findPasswords()
  {
	  Pair<String, String> pair1 = new Pair<String, String>("javax.crypto.spec.SecretKeySpec", "<init>");
	  List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
	  list.add(pair1);
	  
	  List<DBInvocation> dbInvocationPre = new ArrayList<DBInvocation>();
	  
	  for( Pair<String, String> mPair : list) {
		  try {
			  dbInvocationPre.addAll(dbUtil.findPasswords(mPair.getKey(), mPair.getValue()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  
		  
	  }
	  System.out.println(dbInvocationPre.size() + " Passwords found");
	  

	  //List<DBInvocation> dbInvocationPost = new ArrayList<DBInvocation>();
	  
	  for(DBInvocation dbInvocation : dbInvocationPre) {
		  //System.out.println(dbInvocation.getParamString());
		  List<String> paramList = JavaNameHelper.getParameters(dbInvocation.getParamString());
		  String password = paramList.get(0);
		  
		  if(!passwordList.contains(password))
			  passwordList.addElement(password);
		  
		  
		  //dbInvocationPost.addAll(dbUtil.findPasswords(password));
		  
	  } 
	  
	
	  
  }
  
private Path importUnzip() throws IOException {
	
	Path folder = null;
	
	JFileChooser jfc = new JFileChooser();
    int res = jfc.showOpenDialog(null);
    if (res == JFileChooser.APPROVE_OPTION) {
      	
      	folder = Files.createTempDirectory("magnum");
      	
      	//get the zip file content
      	ZipInputStream zis = 
      		new ZipInputStream(
      				new FileInputStream(jfc.getSelectedFile().getCanonicalPath()));
      	
      	//get the zipped file list entry
      	ZipEntry ze = zis.getNextEntry();
  
      	while(ze != null) {
   
      	   String fileName = ze.getName();
             File newFile = new File(folder.toString() + File.separator + fileName);
   
             System.out.println("file unzip : "+ newFile.getAbsoluteFile());
   
              //create all non exists folders
              //else you will hit FileNotFoundException for compressed folder
              new File(newFile.getParent()).mkdirs();
   
              FileOutputStream fos = new FileOutputStream(newFile);             
   
              int len;
              byte []buffer = new byte[1024];
              while ((len = zis.read(buffer)) > 0) {
         		fos.write(buffer, 0, len);
              }
   
              fos.close();   
              ze = zis.getNextEntry();
      	}
   
        zis.closeEntry();
      	zis.close();
   
      	System.out.println("Unzip done");
		
	}
    
    return folder;
}

private List<DBInvocation> findInvocationDiff(List<DBInvocation> trace, DBInvocation origin) {
	
	List<DBInvocation> invocationList = dbUtil.getDiffInvocationBy(origin);
	List<DBInvocation> invocationReturn = new ArrayList<DBInvocation>();
	
	for(DBInvocation invocation : invocationList) {
		
		List<DBInvocation> invocationTrace = new ArrayList<DBInvocation>();
		CallGraph dummyGraph = new CallGraph();
		boolean isEqual = true;
		
        long id = (long) invocation.getId();
        dbUtil.createInvocationTraceDiff(invocationTrace, dummyGraph, id, true);
        dbUtil.createInvocationTraceDiff(invocationTrace, dummyGraph, id, false);
        sortDBInvocationList(invocationTrace);
        
        if(trace.size() == invocationTrace.size()) {
        	// Trace equal
        	for(int i = 0; i < trace.size(); i++) {
        		if(trace.get(i).getClassName().equals(invocationTrace.get(i).getClassName()) &&
        				trace.get(i).getMethodName().equals(invocationTrace.get(i).getMethodName()) && 
        				trace.get(i).getUniqueMethodName().equals(invocationTrace.get(i).getUniqueMethodName())) {
        			
        		}
        		else
        		{
        			isEqual = false;
        		}
        	}
        	
        }
        else
        {
        	isEqual = false;
        }
        
        
        if(isEqual)
        {
        	invocationReturn.addAll(invocationTrace);
        	invocationReturn.add(new DBInvocation(0, false, "", "", "", "", new Timestamp(0), ""));
        }
	}
	
	return invocationReturn;
}

private static void sortDBInvocationList(List<DBInvocation> list) {
    
	Collections.sort(list, new Comparator<DBInvocation>() {

		@Override
		public int compare(DBInvocation arg0, DBInvocation arg1) {
			return  arg0.getTimestamp().compareTo(arg1.getTimestamp());
		}
    });
}

  private class DBQueryExecutor implements Runnable {
    public void run() {
      while (true) {
        try {
          Thread.sleep(300);
          Rectangle r = jspTimeline.getVisibleRect();
          timelineTableModel.setContent(dbUtil.query(jtfFilterTimeline.getLastText()));
          jspTimeline.scrollRectToVisible(r);
          doRepaint(jtbTimeline);
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

}
