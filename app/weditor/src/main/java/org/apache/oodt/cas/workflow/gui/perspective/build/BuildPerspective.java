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

package org.apache.oodt.cas.workflow.gui.perspective.build;

//JDK imports

import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.perspective.MultiStatePerspective;
import org.apache.oodt.cas.workflow.gui.perspective.view.MultiStateView;
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewChange;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewListener;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.perspective.view.impl.DefaultPropView;
import org.apache.oodt.cas.workflow.gui.perspective.view.impl.DefaultTreeView;
import org.apache.oodt.cas.workflow.gui.perspective.view.impl.GlobalConfigView;
import org.apache.oodt.cas.workflow.gui.perspective.view.impl.GraphView;
import org.apache.oodt.cas.workflow.gui.perspective.view.impl.TreeProjectView;
import org.apache.oodt.cas.workflow.gui.util.GuiUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//OODT imports

/**
 * 
 * The default build perspective.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class BuildPerspective extends MultiStatePerspective {

  private static final long serialVersionUID = 3387632819576057527L;

  private View projectView;
  private View globalConfigView;
  private Class<? extends View> projectViewClass;
  private Class<? extends View> mainViewClass;
  private Class<? extends View> treeViewClass;
  private Class<? extends View> propViewClass;
  private Class<? extends View> globalViewClass;

  private ConcurrentHashMap<ViewState, BuildPanel> stateViews;
  private ViewState activeState;

  public static final int MAIN_VIEW = 1;
  private static Logger LOG = Logger.getLogger(BuildPerspective.class.getName());
  private static final int WIDTH = 1000;
  private static final int HEIGHT = 700;

  private JSplitPane projectSplitPane;

  private boolean findSelectedInTab = false;

  public BuildPerspective() {
    this(TreeProjectView.class, GraphView.class, DefaultTreeView.class,
        DefaultPropView.class, GlobalConfigView.class);
  }

  public BuildPerspective(Class<? extends View> projectViewClass,
      Class<? extends View> mainViewClass, Class<? extends View> treeViewClass,
      Class<? extends View> propViewClass, Class<? extends View> globalViewClass) {
    super("Build");
    this.projectViewClass = projectViewClass;
    this.mainViewClass = mainViewClass;
    this.treeViewClass = treeViewClass;
    this.propViewClass = propViewClass;
    this.globalViewClass = globalViewClass;
    this.stateViews = new ConcurrentHashMap<ViewState, BuildPanel>();
    this.projectView = this.createProjectView();
    this.projectView.setPreferredSize(new Dimension(WIDTH / 10, HEIGHT / 2));
    this.globalConfigView = this.createGlobalConfigView();
    this.globalConfigView
        .setPreferredSize(new Dimension(WIDTH / 10, HEIGHT / 2));
  }

  public void reset() {
    super.reset();
    this.activeState = null;
    this.stateViews.clear();
    this.projectView = this.createProjectView();
    this.globalConfigView = this.createGlobalConfigView();
  }

  public void stateChangeNotify(ViewChange<?> change) {
    if (change instanceof ViewChange.NEW_ACTIVE_STATE) {
      this.activeState = ((ViewChange.NEW_ACTIVE_STATE) change).getObject();
      this.refresh();
    } else if (change instanceof ViewChange.REFRESH_VIEW) {
      this.refresh();
    } else if (change instanceof ViewChange.STATE_NAME_CHANGE) {
      this.refresh();
    } else if (change instanceof ViewChange.VIEW_MODEL) {
      String modelId = ((ViewChange.VIEW_MODEL) change).getObject();
      for (ViewState state : this.stateViews.keySet()) {
        for (ModelGraph graph : state.getGraphs()) {
          ModelGraph found = graph.recursiveFindByModelId(modelId);
          if (found != null && !found.getModel().isRef()) {
            this.activeState = state;
            this.activeState.setSelected(found);
            break;
          }
        }
      }
      this.findSelectedInTab = true;
      this.refresh();
      this.findSelectedInTab = false;
    }
  }

  @Override
  public ViewState getActiveState() {
    return this.activeState;
  }

  public View getActiveView() {
    if (this.getActiveState() != null) {
      return this.stateViews.get(this.getActiveState()).getActiveView();
    } else {
      return null;
    }
  }

  @Override
  public void handleAddState(final ViewState state) {
    this.activeState = state;
    BuildPanel buildPanel = new BuildPanel(state);
    this.stateViews.put(state, buildPanel);
    this.refresh();
  }

  @Override
  public void handleRemoveState(ViewState state) {
    this.stateViews.remove(state);
    if (this.stateViews.size() > 0) {
      this.activeState = this.stateViews.keySet().iterator().next();
    } else {
      this.activeState = null;
    }
    this.refresh();
  }

  @Override
  public void refresh() {
    this.save();
    this.removeAll();
    this.setLayout(new BorderLayout());
    JPanel panel;
    if (this.activeState != null) {
      BuildPanel buildPanel = this.stateViews.get(this.activeState);
      buildPanel.refresh();
      panel = buildPanel;
    } else {
      panel = new JPanel();
    }

    if (this.projectView instanceof MultiStateView) {
      ((MultiStateView) this.projectView).refreshView(this.activeState,
          this.getStates());
    } else {
      this.projectView.refreshView(this.activeState);
    }

    this.globalConfigView.refreshView(this.activeState);

    JPanel globalPanel = new JPanel();
    globalPanel.setLayout(new BorderLayout());
    globalPanel.add(this.projectView, BorderLayout.CENTER);
    globalPanel.add(this.globalConfigView, BorderLayout.SOUTH);

    int dividerLocation = -1;
    if (projectSplitPane != null) {
      dividerLocation = projectSplitPane.getDividerLocation();
    }
    projectSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, globalPanel,
        panel);
    if (dividerLocation != -1) {
      projectSplitPane.setDividerLocation(dividerLocation);
    }
    this.add(projectSplitPane, BorderLayout.CENTER);

    this.revalidate();

  }

  private View createMainView(String name) {
    try {
      return this.mainViewClass.getConstructor(String.class).newInstance(name);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  private View createTreeView() {
    try {
      return this.treeViewClass.getConstructor(String.class).newInstance(
          this.treeViewClass.getSimpleName());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  private View createGlobalConfigView() {
    try {
      return this.globalViewClass.getConstructor(String.class).newInstance(
          this.globalViewClass.getSimpleName());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  private View createProjectView() {
    try {
      View view = this.projectViewClass.getConstructor(String.class)
          .newInstance(this.projectViewClass.getSimpleName());
      view.registerListener(this);
      return view;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  private View createPropView() {
    try {
      return this.propViewClass.getConstructor(String.class).newInstance(
          this.propViewClass.getSimpleName());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  private class BuildPanel extends JPanel implements ViewListener {

    private static final long serialVersionUID = -6120047959962567963L;

    private JTabbedPane tabbedPane;
    private Map<View, ViewState> mainViews;
    private View propView;
    private View treeView;

    private View primaryMainView;

    private JPopupMenu closeTabPopup;

    private ViewState state;

    public BuildPanel(ViewState state) {
      this.state = state;

      mainViews = new ConcurrentHashMap<View, ViewState>();

      propView = createPropView();
      if (propView != null) {
        propView.registerListener(this);
      }

      treeView = createTreeView();
      if (treeView != null) {
        treeView.registerListener(this);
      }

      tabbedPane = new JTabbedPane();

      this.addMainView(createMainView(state.getFile().getName()), state);

      closeTabPopup = new JPopupMenu();
      JMenuItem closeItem = new JMenuItem("Close");
      closeItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          View mainView = (View) BuildPanel.this.tabbedPane
              .getSelectedComponent();
          BuildPanel.this.removeMainView(mainView);
        }

      });
      closeTabPopup.add(closeItem);

      this.tabbedPane.addMouseListener(new MouseListener() {

        public void mouseClicked(MouseEvent e) {
          if (e.getButton() == MouseEvent.BUTTON3
              && !BuildPanel.this.tabbedPane.getSelectedComponent().equals(
                  BuildPanel.this.primaryMainView)) {
            closeTabPopup.show(BuildPanel.this.tabbedPane, e.getX(), e.getY());
          }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

      });

      this.tabbedPane.addChangeListener(new ChangeListener() {

        public void stateChanged(ChangeEvent e) {
          View activeView = (View) BuildPanel.this.tabbedPane
              .getSelectedComponent();
          activeView.notifyListeners();
        }

      });

      treeView.setPreferredSize(new Dimension(WIDTH / 10, HEIGHT / 2));
      propView.setPreferredSize(new Dimension(WIDTH / 10, HEIGHT / 2));
      JSplitPane treePropPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
          treeView, propView);
      treePropPane.setResizeWeight(.25);
      tabbedPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
      JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
          tabbedPane, treePropPane);
      mainSplitPane.setResizeWeight(.75);
      this.setLayout(new BorderLayout());
      this.add(mainSplitPane, BorderLayout.CENTER);
    }

    public void addMainView(View mainView, ViewState state) {
      if (this.mainViews.size() == 0) {
        this.primaryMainView = mainView;
        mainView.setPrimary(true);
      }
      this.mainViews.put(mainView, state);
      this.tabbedPane.addTab(mainView.getName(), mainView);
      this.tabbedPane.setSelectedComponent(mainView);
      mainView.registerListener(this);
    }

    public void removeMainView(View mainView) {
      if (!this.primaryMainView.equals(mainView)) {
        for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
          if (mainView.getName().equals(this.tabbedPane.getTitleAt(i))) {
            this.tabbedPane.removeTabAt(i);
            this.mainViews.remove(mainView);
            return;
          }
        }
      }
    }

    public View getActiveView() {
      return (View) this.tabbedPane.getSelectedComponent();
    }

    public void refresh() {
      if (this.getActiveView() != null) {
        ViewState viewState = null;
        if (this.state.getSelected() != null && findSelectedInTab) {
          TOP: for (Entry<View, ViewState> entry : this.mainViews.entrySet()) {
            for (ModelGraph graph : entry.getValue().getGraphs()) {
              ModelGraph found = graph.recursiveFindByModelId(this.state
                  .getSelected().getModel().getModelId());
              if (found != null && !found.getModel().isRef()) {
                viewState = entry.getValue();
                viewState.setSelected(found);
                this.tabbedPane.setSelectedComponent(entry.getKey());
                break TOP;
              }
            }
          }
        } else {
          viewState = this.mainViews.get(this.getActiveView());
        }
        this.getActiveView().refreshView(viewState);
        this.propView.refreshView(viewState);
        this.treeView.refreshView(viewState);
      }
      this.revalidate();
    }

    public void stateChangeNotify(ViewChange<?> change) {
      if (change instanceof ViewChange.NEW_VIEW) {
        this.addMainView(
            createMainView(((ViewChange.NEW_VIEW) change).getObject()
                .getModel().getModelId()),
            new ViewState(this.state.getFile(), null, Collections
                .singletonList(GuiUtils.find(this.state.getGraphs(),
                    ((ViewChange.NEW_VIEW) change).getObject().getModel()
                        .getId())), this.state.getGlobalConfigGroups()));
        this.refresh();
      }
      BuildPerspective.this.stateChangeNotify(change);
    }

  }

}
