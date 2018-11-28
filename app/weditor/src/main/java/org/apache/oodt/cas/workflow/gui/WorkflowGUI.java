/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.workflow.gui;

//JDK imports
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.workflow.gui.menu.EditMenu;
import org.apache.oodt.cas.workflow.gui.menu.FileMenu;
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.model.repo.XmlWorkflowModelRepository;
import org.apache.oodt.cas.workflow.gui.model.repo.XmlWorkflowModelRepositoryFactory;
import org.apache.oodt.cas.workflow.gui.perspective.MultiStatePerspective;
import org.apache.oodt.cas.workflow.gui.perspective.build.BuildPerspective;
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.toolbox.Tool;
import org.apache.oodt.cas.workflow.gui.toolbox.ToolBox;
import org.apache.oodt.cas.workflow.gui.util.IconLoader;

//Commons import
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * Main driver shell and JFrame for the Workflow Editor GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class WorkflowGUI extends JFrame {

  private static final long serialVersionUID = -8217540440195126377L;

  private ToolBox toolbox;

  private MultiStatePerspective perspective;

  private static AtomicInteger untitledIter = new AtomicInteger(0);

  private JMenuBar menu;

  private File workspace;

  private XmlWorkflowModelRepository repo;

  public WorkflowGUI() throws Exception {

    this.addWindowFocusListener(new WindowFocusListener() {

      public void windowGainedFocus(WindowEvent e) {
        if (menu != null)
          menu.revalidate();
        if (toolbox != null)
          toolbox.revalidate();
        if (perspective != null)
          perspective.refresh();
      }

      public void windowLostFocus(WindowEvent e) {
      }

    });

    this.setLayout(new BorderLayout());
    this.setPreferredSize(new Dimension(1000, 800));

    Vector<Tool> tools = new Vector<Tool>();
    Tool editTool = new Tool(IconLoader.getIcon(IconLoader.EDIT),
        IconLoader.getIcon(IconLoader.EDIT_SELECTED)) {
      private static final long serialVersionUID = 1682845263796161282L;

      @Override
      public void onClick() {
        WorkflowGUI.this.perspective.setMode(View.Mode.EDIT);
      }
    };
    tools.add(editTool);
    Tool deleteTool = new Tool(IconLoader.getIcon(IconLoader.DELETE),
        IconLoader.getIcon(IconLoader.DELETE_SELECTED)) {
      private static final long serialVersionUID = 5050127713254634783L;

      @Override
      public void onClick() {
        WorkflowGUI.this.perspective.setMode(View.Mode.DELETE);
      }
    };
    tools.add(deleteTool);
    Tool moveTool = new Tool(IconLoader.getIcon(IconLoader.MOVE),
        IconLoader.getIcon(IconLoader.MOVE_SELECTED)) {
      private static final long serialVersionUID = 1682845263796161282L;

      @Override
      public void onClick() {
        WorkflowGUI.this.perspective.setMode(View.Mode.MOVE);
      }
    };
    tools.add(moveTool);
    Tool zoomInTool = new Tool(IconLoader.getIcon(IconLoader.ZOOM_IN),
        IconLoader.getIcon(IconLoader.ZOOM_IN_SELECTED)) {
      private static final long serialVersionUID = 1682845263796161282L;

      @Override
      public void onClick() {
        WorkflowGUI.this.perspective.setMode(View.Mode.ZOOM_IN);
      }
    };
    tools.add(zoomInTool);
    Tool zoomOutTool = new Tool(IconLoader.getIcon(IconLoader.ZOOM_OUT),
        IconLoader.getIcon(IconLoader.ZOOM_OUT_SELECTED)) {
      private static final long serialVersionUID = 1682845263796161282L;

      @Override
      public void onClick() {
        WorkflowGUI.this.perspective.setMode(View.Mode.ZOOM_OUT);
      }
    };
    tools.add(zoomOutTool);
    toolbox = new ToolBox(tools);
    toolbox.setSelected(editTool);
    this.add(toolbox, BorderLayout.NORTH);

    this.setJMenuBar(menu = this.generateMenuBar());
    perspective = new BuildPerspective();
    perspective.refresh();
    this.add(perspective, BorderLayout.CENTER);
  }

  private void updateWorkspaceText() {
    if (this.workspace == null)
      this.setTitle(null);
    else
      this.setTitle(StringUtils.leftPad("Workspace: " + this.workspace, 100));
  }

  private void loadProjects() {
    try {
      XmlWorkflowModelRepositoryFactory factory = new XmlWorkflowModelRepositoryFactory();
      factory.setWorkspace(this.workspace.getAbsolutePath());
      repo = factory.createModelRepository();
      repo.loadGraphs(new HashSet<String>(Arrays.asList("sequential",
          "parallel", "task", "condition")));
      for (File file : repo.getFiles()) {
        List<ModelGraph> graphs = new Vector<ModelGraph>();
        for (ModelGraph graph : repo.getGraphs())
          if (graph.getModel().getFile().equals(file))
            graphs.add(graph);
        System.out.println(graphs);
        perspective.addState(new ViewState(file, null, graphs, repo
            .getGlobalConfigGroups()));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public JMenuBar generateMenuBar() {
    JMenuBar bar = new JMenuBar();
    FileMenu fileMenu = new FileMenu();
    bar.add(fileMenu);
    fileMenu.getExit().addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent event) {
        System.exit(1);        
      }
    });
    
    fileMenu.getOpenWorkspace().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        try {
          JFileChooser chooser = new JFileChooser(new File(".")) {
            boolean acceptFile(File f) {
              return f.isDirectory();
            }
          };
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          int value = chooser.showOpenDialog(WorkflowGUI.this);
          if (value == JFileChooser.APPROVE_OPTION) {
            workspace = chooser.getSelectedFile();
            updateWorkspaceText();
            perspective.reset();
            loadProjects();
          }
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
    });
    fileMenu.getImport().addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent event) {
        try {
          if (workspace == null)
            return;
          JFileChooser chooser = new JFileChooser(new File("."));
          int value = chooser.showOpenDialog(WorkflowGUI.this);
          if (value == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            XmlWorkflowModelRepositoryFactory factory = new XmlWorkflowModelRepositoryFactory();
            factory.setWorkspace(workspace.getAbsolutePath());
            View activeView = perspective.getActiveView();

            if (activeView != null) {
              // TODO: add code for import
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

    });
    fileMenu.getNewWorkspace().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = new JFileChooser(new File(".")) {
          boolean acceptFile(File f) {
            return f.isDirectory();
          }
        };
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int value = chooser.showOpenDialog(WorkflowGUI.this);
        if (value == JFileChooser.APPROVE_OPTION) {
          workspace = chooser.getSelectedFile();
          updateWorkspaceText();
          perspective.reset();
          loadProjects();
          perspective.refresh();
        }
      }
    });

    fileMenu.getNewProject().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // TODO: add new project code
      }
    });
    fileMenu.getSave().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
      	try {
      		repo.save();
      	} catch (Exception e) {
      		e.printStackTrace();
      	}
      }
    });
    EditMenu editMenu = new EditMenu();
    bar.add(editMenu);
    editMenu.getUndo().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        try {
          perspective.undo();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    bar.revalidate();
    return bar;
  }

  public static void main(String[] args) {
    UIManager.put("TabbedPane.selected", new Color(238, 238, 238));
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        WorkflowGUI gui;
        try {
          gui = new WorkflowGUI();
          gui.pack();
          gui.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
