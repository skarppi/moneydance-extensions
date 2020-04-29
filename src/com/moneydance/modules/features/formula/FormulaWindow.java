/************************************************************\
 *      Copyright (C) 2016 The Infinite Kind, Limited       *
\************************************************************/

package com.moneydance.modules.features.formula;

import com.moneydance.awt.*;
import com.infinitekind.moneydance.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

/** Window used for Account List interface */

public class FormulaWindow
  extends JFrame
  implements ActionListener
{
  private com.moneydance.modules.features.formula.Main extension;
  private JTextArea accountListArea;
  private JButton clearButton;
  private JButton closeButton;
  private JTextField inputArea;

  public FormulaWindow(Main extension) {
    super("Formulas");
    this.extension = extension;

    accountListArea = new JTextArea();
    
    AccountBook book = extension.getUnprotectedContext().getCurrentAccountBook();
    StringBuffer acctStr = new StringBuffer();
    if(book !=null) {
      addReminders(book.getRootAccount(), acctStr);
    }
    accountListArea.setEditable(false);
    accountListArea.setText(acctStr.toString());
    inputArea = new JTextField();
    inputArea.setEditable(true);
    clearButton = new JButton("Clear");
    closeButton = new JButton("Close");

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(new EmptyBorder(10,10,10,10));
    p.add(new JScrollPane(accountListArea), AwtUtil.getConstraints(0,0,1,1,4,1,true,true));
    p.add(Box.createVerticalStrut(8), AwtUtil.getConstraints(0,2,0,0,1,1,false,false));
    p.add(clearButton, AwtUtil.getConstraints(0,3,1,0,1,1,false,true));
    p.add(closeButton, AwtUtil.getConstraints(1,3,1,0,1,1,false,true));
    getContentPane().add(p);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    enableEvents(WindowEvent.WINDOW_CLOSING);
    closeButton.addActionListener(this);
    clearButton.addActionListener(this);
        
    setSize(500, 400);
    AwtUtil.centerWindow(this);
  }

  public static void addReminders(Account parentAcct, StringBuffer acctStr) {
    int sz = parentAcct.getSubAccountCount();
    for(int i=0; i<sz; i++) {
      Account acct = parentAcct.getSubAccount(i);
      acctStr.append(acct.getFullAccountName());
      acctStr.append("\n");
      addReminders(acct, acctStr);
    }
  }


  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if(src==closeButton) {
      extension.closeConsole();
    }
    if(src==clearButton) {
      accountListArea.setText("");
    }
  }

  public final void processEvent(AWTEvent evt) {
    if(evt.getID()==WindowEvent.WINDOW_CLOSING) {
      extension.closeConsole();
      return;
    }
    if(evt.getID()==WindowEvent.WINDOW_OPENED) {
    }
    super.processEvent(evt);
  }

  void goAway() {
    setVisible(false);
    dispose();
  }
}
