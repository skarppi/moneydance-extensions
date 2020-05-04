/************************************************************\
 *      Copyright (C) 2016 The Infinite Kind, Limited       *
\************************************************************/

package com.moneydance.modules.features.formula;

import com.moneydance.apps.md.view.gui.OKButtonListener;
import com.moneydance.apps.md.view.gui.OKButtonPanel;
import com.moneydance.awt.AwtUtil;
import com.moneydance.awt.GridC;
import com.moneydance.modules.features.formula.reminder.ReminderDetails;
import com.moneydance.modules.features.formula.reminder.ReminderList;
import com.moneydance.util.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/** Window used for Account List interface */

public class FormulaWindow
  extends JFrame
  implements OKButtonListener
{
  public FormulaWindow(MDApi api) {
    super("Formulas");

    ReminderDetails reminderDetails = new ReminderDetails(api);

    ReminderList reminderList = new ReminderList(api);
    reminderList.addSelectionListener(reminder ->
      reminderDetails.setReminder(reminder)
    );

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(UiUtil.DLG_VGAP, UiUtil.DLG_HGAP,
            UiUtil.DLG_VGAP, UiUtil.DLG_HGAP));
    p.add(reminderList, GridC.getc(0, 0).wxy(1, 1).fillboth());

    p.add(reminderDetails, GridC.getc(0,1).wxy(1, 1).fillboth());

    p.add(Box.createVerticalStrut(UiUtil.DLG_VGAP), GridC.getc(0,2));

    final OKButtonPanel okPanel = new OKButtonPanel(api.getGUI(), this, OKButtonPanel.QUESTION_OK_CANCEL);
    p.add(okPanel, GridC.getc(0,3).east());
    getContentPane().add(p);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    enableEvents(WindowEvent.WINDOW_CLOSING);

    setSize(500, 400);
    AwtUtil.centerWindow(this);
  }

  public final void processEvent(AWTEvent evt) {
    if(evt.getID()==WindowEvent.WINDOW_CLOSING) {
      goAway();
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

  @Override
  public void buttonPressed(int buttonId) {
    if (buttonId == OKButtonPanel.ANSWER_CANCEL) {
      goAway();
    } else if (buttonId == OKButtonPanel.ANSWER_OK) {
      goAway();
    }
  }
}
