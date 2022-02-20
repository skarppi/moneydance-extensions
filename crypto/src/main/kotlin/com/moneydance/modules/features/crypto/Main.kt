package com.moneydance.modules.features.crypto

import com.moneydance.apps.md.controller.FeatureModule
import com.moneydance.apps.md.view.gui.MoneydanceGUI
import com.moneydance.modules.features.MDApi
import java.awt.Image
import javax.imageio.ImageIO

/** Pluggable module to add formulas to Moneydance transaction reminders.
 */
class Main : FeatureModule() {

    private var cryptoWindow: CryptoWindow? = null

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
        return "Crypto Importer"
    }

    @Synchronized
    private fun showWindow() {
        if (cryptoWindow == null) {
            val context = context as com.moneydance.apps.md.controller.Main
            val api = MDApi(context, context.ui as MoneydanceGUI)

            cryptoWindow = CryptoWindow(api)
            cryptoWindow!!.isVisible = true
        } else {
            cryptoWindow!!.isVisible = true
            cryptoWindow!!.toFront()
            cryptoWindow!!.requestFocus()
        }
    }

    @Synchronized
    fun closeConsole() {
        cryptoWindow?.goAway()
        cryptoWindow = null
        System.gc()
    }
}