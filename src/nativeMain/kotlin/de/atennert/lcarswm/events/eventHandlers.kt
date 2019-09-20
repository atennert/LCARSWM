package de.atennert.lcarswm.events

import de.atennert.lcarswm.*
import de.atennert.lcarswm.system.api.PosixApi
import de.atennert.lcarswm.system.api.SystemApi
import de.atennert.lcarswm.windowactions.closeWindow
import kotlinx.cinterop.*
import xlib.*

/**
 * Map of event types to event handlers. DON'T EDIT THE MAPS CONTENT!!!
 */
val EVENT_HANDLERS =
    hashMapOf<Int, Function6<SystemApi, WindowManagerState, XEvent, CPointer<XImage>, Window, List<GC>, Boolean>>(
        Pair(KeyPress, ::handleKeyPress),
        Pair(KeyRelease, ::handleKeyRelease),
        Pair(ButtonPress, { _, _, e, _, _, _ -> logButtonPress(e) }),
        Pair(ButtonRelease, { _, _, e, _, _, _ -> logButtonRelease(e) }),
        Pair(ConfigureRequest, { d, w, e, _, _, _ -> handleConfigureRequest(d, w, e) }),
        Pair(MapRequest, { d, w, e, _, rw, _ -> handleMapRequest(d, w, e, rw) }),
        Pair(MapNotify, { _, _, e, _, _, _ -> logMapNotify(e) }),
        Pair(DestroyNotify, { _, w, e, _, _, _ -> handleDestroyNotify(w, e) }),
        Pair(UnmapNotify, ::handleUnmapNotify),
        Pair(ReparentNotify, { _, _, e, _, _, _ -> logReparentNotify(e) }),
        Pair(CreateNotify, { _, _, e, _, _, _ -> logCreateNotify(e) }),
        Pair(ConfigureNotify, { _, _, e, _, _, _ -> logConfigureNotify(e) })
    )

private fun handleKeyRelease(
    system: SystemApi,
    windowManagerState: WindowManagerState,
    xEvent: XEvent,
    image: CPointer<XImage>,
    rootWindow: Window,
    graphicsContexts: List<GC>
): Boolean {
    val releasedEvent = xEvent.xkey
    val key = releasedEvent.keycode
    println("::handleKeyRelease::Key released: $key")

    when (windowManagerState.keyboardKeys[key]) {
        XK_M -> toggleScreenMode(system, windowManagerState, image, rootWindow, graphicsContexts)
        XK_T -> loadAppFromKeyBinding(system, "Win+T")
        XK_B -> loadAppFromKeyBinding(system, "Win+B")
        XK_I -> loadAppFromKeyBinding(system, "Win+I")
        XF86XK_AudioMute -> loadAppFromKeyBinding(system, "XF86AudioMute")
        XF86XK_AudioLowerVolume -> loadAppFromKeyBinding(system, "XF86AudioLowerVolume")
        XF86XK_AudioRaiseVolume -> loadAppFromKeyBinding(system, "XF86AudioRaiseVolume")
        XK_F4 -> {
            val window = windowManagerState.activeWindow
            if (window != null) {
                closeWindow(window.id, system, windowManagerState)
            }
        }
        XK_Q -> return true
        else -> println("::handleKeyRelease::unknown key: $key")
    }
    return false
}


private fun loadAppFromKeyBinding(posixApi: PosixApi, keyBinding: String) {
    val programConfig = readFromConfig(posixApi, KEY_CONFIG_FILE, keyBinding) ?: return
    println("::loadAppFromKeyBinding::loading app for $keyBinding ${programConfig.size}")
    runProgram(posixApi, programConfig[0], programConfig)
}
