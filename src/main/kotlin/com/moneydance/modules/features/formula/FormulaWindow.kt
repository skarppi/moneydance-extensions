package com.moneydance.modules.features.formula

import javax.swing.JFrame
import com.moneydance.apps.md.view.gui.OKButtonListener
import com.moneydance.modules.features.formula.reminder.ReminderList
import com.moneydance.modules.features.formula.reminder.ReminderDetails
import javax.swing.JPanel
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import com.moneydance.util.UiUtil
import com.moneydance.awt.GridC
import com.moneydance.apps.md.view.gui.OKButtonPanel
import java.awt.event.WindowEvent
import com.moneydance.awt.AwtUtil
import java.awt.AWTEvent
import javax.swing.Box

/** Window used for Formula interface  */
class FormulaWindow(api: MDApi) : JFrame("Formulas"), OKButtonListener {
    private val reminderList = ReminderList(api)

    init {
        val reminderDetails = ReminderDetails(api)
        reminderList.addSelectionListener { selected -> reminderDetails.setReminder(selected) }
        val p = JPanel(GridBagLayout())
        p.border = BorderFactory.createEmptyBorder(
            UiUtil.DLG_VGAP, UiUtil.DLG_HGAP,
            UiUtil.DLG_VGAP, UiUtil.DLG_HGAP
        )
        p.add(reminderList, GridC.getc(0, 0).wxy(1f, 1f).fillboth())
        p.add(reminderDetails, GridC.getc(0, 1).wxy(1f, 1f).fillboth())
        p.add(Box.createVerticalStrut(UiUtil.DLG_VGAP), GridC.getc(0, 2))
        val okPanel = OKButtonPanel(api.gui, this, OKButtonPanel.QUESTION_OK)
        p.add(okPanel, GridC.getc(0, 3).east())
        contentPane.add(p)
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        enableEvents(WindowEvent.WINDOW_CLOSING.toLong())
        setSize(1000, 700)
        AwtUtil.centerWindow(this)
    }

    fun reload() {
        reminderList.reload()
    }

    public override fun processEvent(evt: AWTEvent) {
        if (evt.id == WindowEvent.WINDOW_CLOSING) {
            goAway()
            return
        }
        if (evt.id == WindowEvent.WINDOW_OPENED) {
        }
        super.processEvent(evt)
    }

    fun goAway() {
        isVisible = false
        dispose()
    }

    override fun buttonPressed(buttonId: Int) {
        goAway()
    }
}