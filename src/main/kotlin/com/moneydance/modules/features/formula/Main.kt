package com.moneydance.modules.features.formula

import com.moneydance.apps.md.controller.FeatureModule
import com.moneydance.apps.md.view.gui.MoneydanceGUI
import java.awt.Image
import javax.imageio.ImageIO

/** Pluggable module to add formulas to Moneydance transaction reminders.
 */
class Main : FeatureModule() {

    private var formulaWindow: FormulaWindow? = null

    override fun init() {
        // the first thing we will do is register this module to be invoked
        // via the application toolbar
        context.registerFeature(
            this,
            "open",
            getIcon("icon"),
            name
        )
    }

    override fun cleanup() {
        closeConsole()
    }

    private fun getIcon(icon: String): Image? {
        val cl = javaClass.classLoader
        val inputStream = cl.getResourceAsStream("/com/moneydance/modules/features/formula/${icon}.gif") ?: return null
        return ImageIO.read(inputStream)
    }

    /** Process an invocation of this module with the given URI  */
    override fun invoke(uri: String) {
        if (uri == "open") {
            showWindow()
        }
    }

    override fun getName(): String {
        return "Formulas"
    }

    @Synchronized
    private fun showWindow() {
        if (formulaWindow == null) {
            val context = context as com.moneydance.apps.md.controller.Main
            val api = MDApi(context, context.ui as MoneydanceGUI)

            formulaWindow = FormulaWindow(api)
            formulaWindow!!.isVisible = true
        } else {
            formulaWindow!!.reload()
            formulaWindow!!.isVisible = true
            formulaWindow!!.toFront()
            formulaWindow!!.requestFocus()
        }
    }

    @Synchronized
    fun closeConsole() {
        formulaWindow?.goAway()
        formulaWindow = null
        System.gc()
    }
}