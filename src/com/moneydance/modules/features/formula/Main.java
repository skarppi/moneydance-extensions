/************************************************************\
 *      Copyright (C) 2016 The Infinite Kind, Limited       *
\************************************************************/

package com.moneydance.modules.features.formula;

import com.moneydance.apps.md.controller.FeatureModule;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;

import java.awt.*;
import java.io.ByteArrayOutputStream;

/** Pluggable module used to give users access to a Account List
    interface to Moneydance.
*/

public class Main
  extends FeatureModule
{
  private FormulaWindow formulaWindow = null;

  private MDApi model;

  @Override
  public void init() {
    // the first thing we will do is register this module to be invoked
    // via the application toolbar
    FeatureModuleContext context = getContext();
    try {
      context.registerFeature(this, "open",
        getIcon("open"),
        getName());
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
    }

    model = new MDApi(context,
            () -> (MoneydanceGUI) ((com.moneydance.apps.md.controller.Main) context).getUI());
  }

  @Override
  public void cleanup() {
    closeConsole();
  }
  
  private Image getIcon(String action) {
    try {
      ClassLoader cl = getClass().getClassLoader();
      java.io.InputStream in = 
        cl.getResourceAsStream("/com/moneydance/modules/features/formula/icon.gif");
      if (in != null) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(1000);
        byte buf[] = new byte[256];
        int n = 0;
        while((n=in.read(buf, 0, buf.length))>=0)
          bout.write(buf, 0, n);
        return Toolkit.getDefaultToolkit().createImage(bout.toByteArray());
      }
    } catch (Throwable e) { }
    return null;
  }
  
  /** Process an invokation of this module with the given URI */
  public void invoke(String uri) {
    String command = uri;
    String parameters = "";
    int theIdx = uri.indexOf('?');
    if(theIdx>=0) {
      command = uri.substring(0, theIdx);
      parameters = uri.substring(theIdx+1);
    }
    else {
      theIdx = uri.indexOf(':');
      if(theIdx>=0) {
        command = uri.substring(0, theIdx);
      }
    }

    if(command.equals("open")) {
      showWindow();
    }    
  }

  public String getName() {
    return "Formulas";
  }

  private synchronized void showWindow() {
    if(formulaWindow == null) {
      formulaWindow = new FormulaWindow(model);
      formulaWindow.setVisible(true);
    }
    else {
      formulaWindow.setVisible(true);
      formulaWindow.toFront();
      formulaWindow.requestFocus();
    }
  }

  FeatureModuleContext getUnprotectedContext() {
    return getContext();
  }

  synchronized void closeConsole() {
    if(formulaWindow !=null) {
      formulaWindow.goAway();
      formulaWindow = null;
      System.gc();
    }
  }
}


