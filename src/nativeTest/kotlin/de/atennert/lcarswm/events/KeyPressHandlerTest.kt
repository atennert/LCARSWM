package de.atennert.lcarswm.events

import de.atennert.lcarswm.KeyManager
import de.atennert.lcarswm.Modifiers
import de.atennert.lcarswm.drawing.UIDrawingMock
import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.monitor.MonitorManagerMock
import de.atennert.lcarswm.system.SystemFacadeMock
import de.atennert.lcarswm.windowactions.WindowCoordinatorMock
import de.atennert.lcarswm.windowactions.WindowFocusHandler
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import xlib.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KeyPressHandlerTest {
    @Test
    fun `return the event type KeyPressHandler`() {
        val systemApi = SystemFacadeMock()
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawing = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawing)

        assertEquals(KeyPress, keyPressHandler.xEventType, "The key press handler should have the correct type")
    }

    @Test
    fun `move active window to the next monitor`() {
        val systemApi = SystemFacadeMock()
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        keyManager.grabInternalKeys(systemApi.rootWindowId)
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySyms.getValue(XK_Up).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        val moveWindowCall = windowCoordinator.functionCalls.removeAt(0)
        assertEquals("moveWindowToNextMonitor", moveWindowCall.name, "The focused window should be moved to the next monitor")
        assertEquals(windowFocusHandler.getFocusedWindow(), moveWindowCall.parameters[0], "The _focused window_ should be moved to the next monitor")

        val uiRedrawCall = uiDrawer.functionCalls.removeAt(0)
        assertEquals("drawWindowManagerFrame", uiRedrawCall.name, "The WM UI needs to be redrawn")
    }

    @Test
    fun `move active window to the previous monitor`() {
        val systemApi = SystemFacadeMock()
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        keyManager.grabInternalKeys(systemApi.rootWindowId)
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySyms.getValue(XK_Down).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        val moveWindowCall = windowCoordinator.functionCalls.removeAt(0)
        assertEquals("moveWindowToPreviousMonitor", moveWindowCall.name, "The focused window should be moved to the previous monitor")
        assertEquals(windowFocusHandler.getFocusedWindow(), moveWindowCall.parameters[0], "The _focused window_ should be moved to the previous monitor")

        val uiRedrawCall = uiDrawer.functionCalls.removeAt(0)
        assertEquals("drawWindowManagerFrame", uiRedrawCall.name, "The WM UI needs to be redrawn")
    }

    @Test
    fun `don't react on move to next monitor without a focusable window`() {
        val systemApi = SystemFacadeMock()
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        keyManager.grabInternalKeys(systemApi.rootWindowId)

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySyms.getValue(XK_Up).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertTrue(windowCoordinator.functionCalls.isEmpty(), "There should be no call to the window coordinator without focused window")

        assertTrue(uiDrawer.functionCalls.isEmpty(), "There should be no call to the UI drawer without focused window")
    }

    @Test
    fun `don't react on move to previous monitor without a focusable window`() {
        val systemApi = SystemFacadeMock()
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        keyManager.grabInternalKeys(systemApi.rootWindowId)

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySyms.getValue(XK_Down).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertTrue(windowCoordinator.functionCalls.isEmpty(), "There should be no call to the window coordinator without focused window")

        assertTrue(uiDrawer.functionCalls.isEmpty(), "There should be no call to the UI drawer without focused window")
    }

    @Test
    fun `toggle focused Window`() {
        val systemApi = object : SystemFacadeMock() {
            override fun keysymToKeycode(keySym: KeySym): KeyCode {
                if (keySym.convert<Int>() == XK_Tab) {
                    return 42.convert()
                }
                return super.keysymToKeycode(keySym)
            }
        }
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        val window1 = systemApi.getNewWindowId()
        val window2 = systemApi.getNewWindowId()
        val window3 = systemApi.getNewWindowId()
        keyManager.grabInternalKeys(systemApi.rootWindowId)
        windowFocusHandler.setFocusedWindow(window1)
        windowFocusHandler.setFocusedWindow(window2)
        windowFocusHandler.setFocusedWindow(window3)
        windowFocusHandler.setFocusedWindow(window1)

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = 42.convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertEquals(window2, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated")

        keyPressHandler.handleEvent(keyPressEvent)
        assertEquals(window3, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated")

        keyPressHandler.handleEvent(keyPressEvent)
        assertEquals(window1, windowFocusHandler.getFocusedWindow(), "The focused window needs to be updated")
    }

    @Test
    fun `don't toggle without focusable windows`() {
        val systemApi = object : SystemFacadeMock() {
            override fun keysymToKeycode(keySym: KeySym): KeyCode {
                if (keySym.convert<Int>() == XK_Tab) {
                    return 42.convert()
                }
                return super.keysymToKeycode(keySym)
            }
        }
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        keyManager.grabInternalKeys(systemApi.rootWindowId)

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = 42.convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.ALT).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the up-key shouldn't trigger a shutdown")

        assertEquals(0, windowCoordinator.functionCalls.size, "There is no window to restack")
    }

    @Test
    fun `toggle screen mode on M`() {
        val systemApi = SystemFacadeMock()
        val keyManager = KeyManager(systemApi)
        val windowCoordinator = WindowCoordinatorMock()
        val windowFocusHandler = WindowFocusHandler()
        val uiDrawer = UIDrawingMock()
        val monitorManager = MonitorManagerMock()
        keyManager.grabInternalKeys(systemApi.rootWindowId)
        windowFocusHandler.setFocusedWindow(systemApi.getNewWindowId())

        val keyPressHandler = KeyPressHandler(LoggerMock(), keyManager, monitorManager, windowCoordinator, windowFocusHandler, uiDrawer)

        val keyPressEvent = nativeHeap.alloc<XEvent>()
        keyPressEvent.type = KeyPress
        keyPressEvent.xkey.keycode = systemApi.keySyms.getValue(XK_M).convert()
        keyPressEvent.xkey.state = keyManager.modMasks.getValue(Modifiers.SUPER).convert()

        val shutdownValue = keyPressHandler.handleEvent(keyPressEvent)

        assertFalse(shutdownValue, "Handling the M-key shouldn't trigger a shutdown.")

        val monitorModeToggleCall = monitorManager.functionCalls.removeAt(0)
        assertEquals("toggleScreenMode", monitorModeToggleCall.name, "The screen mode should be toggled.")

        val realignWindowsCall = windowCoordinator.functionCalls.removeAt(0)
        assertEquals("realignWindows", realignWindowsCall.name, "The windows need to be realigned after toggling the screen mode.")

        val redrawUIcall = uiDrawer.functionCalls.removeAt(0)
        assertEquals("drawWindowManagerFrame", redrawUIcall.name, "The window frame needs to be redrawn on screen mode change.")
    }
}