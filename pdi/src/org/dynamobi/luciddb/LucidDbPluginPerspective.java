/**
 * parts (C) Pentaho 2010
 */
package org.dynamobi.luciddb;

import java.io.InputStream;
import java.io.File;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import org.pentaho.di.core.Props;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.SpoonBrowser;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import java.net.MalformedURLException;
import java.net.URL;

import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

import org.dynamobi.luciddb.LucidDbLauncher;
import org.dynamobi.luciddb.LucidDbJetty;

public class LucidDbPluginPerspective implements SpoonPerspective, 
  RowListener {
  
  private static LogChannelInterface
    log = new LogChannel(LucidDbPluginPerspective.class.toString());

  protected java.awt.Panel m_histoHolder;
  
  /** Button to start lucid DB server */
  protected Button startButton;
  /** Button to stop lucid DB server */
  protected Button stopButton;
  
  protected static LucidDbPluginPerspective singleton = null;
  protected Composite UI;

  private static TabSet tabfolder;
  private static PropsUI props = PropsUI.getInstance();
  
  public LucidDbPluginPerspective() {
    if (singleton == null) {
      Shell shell = new Shell();
      shell.setLayout(new GridLayout(2, true));
      shell.setSize(900, 600);

      // holds buttons
      final Composite holder = new Composite(shell, SWT.NONE);
      holder.setLayout(new GridLayout(4, true));

      startButton = new Button(holder, SWT.PUSH);
      startButton.setText("Start LucidDB and Jetty");
      GridData grid = new GridData();
      grid.horizontalSpan = 1;
      grid.verticalAlignment = GridData.FILL;
      startButton.setLayoutData(grid);

      Button startBrowserButton = new Button(holder, SWT.PUSH);
      startBrowserButton.setText("Open Browser Tab");
      grid = new GridData();
      grid.horizontalSpan = 1;
      grid.verticalAlignment = GridData.FILL;
      startBrowserButton.setLayoutData(grid);

      stopButton = new Button(holder, SWT.PUSH);
      stopButton.setText("Stop LucidDB and Jetty");
      grid = new GridData();
      grid.horizontalSpan = 1;
      grid.verticalAlignment = GridData.FILL;
      stopButton.setLayoutData(grid);
      
      // listeners      
      startButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          if (!LucidDbLauncher.launched) {
            String dir;
            try {
              // should be kettle/
              dir = new File(".").getCanonicalPath() +
                "/plugins/spoon/lucidDb/luciddb/bin";
            } catch (Exception ex) {
              log.logError("Could not open current directory", ex);
              return;
            }
            LucidDbLauncher.start(dir);
          }
          if (!LucidDbJetty.launched) {
            LucidDbJetty.start();
          }
        }
      });

      startBrowserButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          startBrowser();
        }
      });

      stopButton.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          if (LucidDbJetty.launched) {
            LucidDbJetty.stop();
          }
          if (LucidDbLauncher.launched) {
            LucidDbLauncher.stop();
          }
        }
      });
      
      tabfolder = new TabSet(holder);
      tabfolder.setChangedFont(GUIResource.getInstance().getFontBold());
      tabfolder.setUnchangedFont(GUIResource.getInstance().getFontGraph());
      props.setLook(tabfolder.getSwtTabset(), Props.WIDGET_STYLE_TAB);

      grid = new GridData();
      grid.horizontalSpan = 4;
      grid.horizontalAlignment = GridData.FILL;
      grid.verticalAlignment = GridData.FILL;
      grid.grabExcessHorizontalSpace = true;
      grid.grabExcessVerticalSpace = true;
      tabfolder.getSwtTabset().setLayoutData(grid);

      // layout for the holder panel
      grid = new GridData();
      grid.horizontalAlignment = GridData.FILL;
      grid.verticalAlignment = GridData.FILL;
      grid.grabExcessHorizontalSpace = true;
      grid.grabExcessVerticalSpace = true;
      holder.setLayoutData(grid);    
      holder.layout();
      
      UI = holder;
      singleton = this;
    }
  }
  
  private static void startBrowser() {
    String u = "http://" + LucidDbJetty.host + ":" + LucidDbJetty.port +
      "/adminui/SQLAdmin.html";
    URL url;
    try {
      url = new URL(u);
    } catch (MalformedURLException ex) {
      log.logError("Bad URL: " + u, ex);
      return;
    }

    String name = "Admin UI";

    try {
      CTabFolder cTabFolder = tabfolder.getSwtTabset();
      SpoonBrowser browser = new SpoonBrowser(cTabFolder, null,
          u, true, true, null);
      TabItem tabItem = new TabItem(tabfolder, name, name);
      tabItem.setImage(GUIResource.getInstance().getImageLogoSmall());
      tabItem.setControl(browser.getComposite());
      tabfolder.setSelected(0);
    } catch (Exception ex) {
      log.logError("Unable to open browser tab.", ex);
    }
  }

  public static LucidDbPluginPerspective getSingleton() {
    if (singleton == null) {
      new LucidDbPluginPerspective();
    }
    return singleton;
  }

  public void addPerspectiveListener(SpoonPerspectiveListener arg) { }
  public EngineMetaInterface getActiveMeta() {
    return null;
  }

  public String getDisplayName(Locale arg) {
    return "LucidDB Admin UI";
  }

  public List<XulEventHandler> getEventHandlers() {
    return null;
  }

  public String getId() {
    return "010-lucidDB";
  }

  public List<XulOverlay> getOverlays() {
    return null;
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/pdi/spoon/icon.png");
  }

  public Composite getUI() {            
    return UI;
  }

  public void setActive(boolean arg0) {
  }

  public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row)
    throws KettleStepException { }

  public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) 
    throws KettleStepException { }
  
  public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) 
    throws KettleStepException { }
  
}
