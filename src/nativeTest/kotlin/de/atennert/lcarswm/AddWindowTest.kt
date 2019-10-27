package de.atennert.lcarswm

import de.atennert.lcarswm.log.LoggerMock
import de.atennert.lcarswm.system.LoggingSystemFacadeMock
import kotlinx.cinterop.convert
import xlib.Atom
import xlib.NormalState
import xlib.Window
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class AddWindowTest {
    @Test
    fun `check window initialization`() {
        val rootWindowId: Window = 2.convert()
        val windowId: Window = 5.convert()
        val frameId: Window = 12.convert()
        val commandList = mutableListOf<String>()

        val systemApi = SystemApiHelper(frameId, commandList)
        val windowManagerState = WindowManagerStateHelper(commandList)

        addWindow(systemApi, LoggerMock(), windowManagerState, rootWindowId, windowId, false)

        assertEquals("createSimpleWindow-$rootWindowId", commandList.removeAt(0), "frame window should be created firstly")
        assertEquals("reparentWindow-$windowId-$frameId", commandList.removeAt(0), "child window should be reparented to frame secondly")
        assertEquals("mapWindow-$frameId", commandList.removeAt(0), "frame window should be mapped thirdly")
        assertEquals("mapWindow-$windowId", commandList.removeAt(0), "child window should be mapped fourthly")
        assertEquals("changeProperty - $windowId:${windowManagerState.wmState}:${windowManagerState.wmState}:$NormalState", commandList.removeAt(0), "normal state needs to be set in windows frame atom")
        assertEquals("addWindow-$windowId", commandList.removeAt(0), "finally, the child window should be added to the window list")

        assertTrue(commandList.isEmpty(), "There should be no unchecked commands")
    }

    class SystemApiHelper(
        private val frameId: Window,
        private val commandList: MutableList<String>
    ) : LoggingSystemFacadeMock() {
        override fun createSimpleWindow(parentWindow: Window, measurements: List<Int>): Window {
            commandList.add("createSimpleWindow-$parentWindow")
            return frameId
        }

        override fun reparentWindow(window: Window, parent: Window, x: Int, y: Int): Int {
            commandList.add("reparentWindow-$window-$parent")
            return 0
        }

        override fun mapWindow(window: Window): Int {
            commandList.add("mapWindow-$window")
            return 0
        }

        override fun changeProperty(
            window: Window,
            propertyAtom: Atom,
            typeAtom: Atom,
            data: UByteArray?,
            format: Int
        ): Int {
            commandList.add("changeProperty - $window:$propertyAtom:$typeAtom:${data?.get(0)}")
            return 0
        }
    }

    class WindowManagerStateHelper(private val commandList: MutableList<String>) : WindowManagerStateMock() {
        override val wmState: Atom = 42.convert()

        override fun addWindow(window: WindowContainer, monitor: Monitor) {
            commandList.add("addWindow-${window.id}")
        }
    }
}