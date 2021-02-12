package com.mihir.remoteapplication

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.AudioManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout

class RemoteAccessibilityService : AccessibilityService() {

    var mLayout: FrameLayout? = null

    init {
    }

    companion object {
        val TAG = "RemoteService"

        @JvmStatic
        var accessibilityService: AccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        serviceInfo.flags =
            AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE or AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION
        accessibilityService = this
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)
        configurePowerButton()
        configureVolumeButton()
        configureClickButton()
        configureSwipeButton()
    }

    private fun configurePowerButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.power) as Button
        powerButton.setOnClickListener { performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) }
    }

    private fun configureVolumeButton() {
        val volumeUpButton = mLayout?.findViewById<View>(R.id.volume_up) as Button
        volumeUpButton.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout?.findViewById<View>(R.id.click) as Button
        scrollButton.setOnClickListener {
            val scrollable = findScrollableNode(rootInActiveWindow)
            scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        }
    }

    private fun configureSwipeButton() {
        val swipeButton = mLayout?.findViewById<View>(R.id.swipe) as Button
        swipeButton.setOnClickListener {
            val swipePath = Path()
            swipePath.moveTo(700f, 700f)
            swipePath.lineTo(1000f, 1000f)
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(StrokeDescription(swipePath, 0, 500))
            dispatchGesture(gestureBuilder.build(), null, null)
        }
    }

    private fun configureClickButton() {
        val swipeButton = mLayout?.findViewById<View>(R.id.click) as Button
        swipeButton.setOnClickListener {
            val clickPath = Path()
            clickPath.moveTo(700f, 700f)
            val clickStroke = StrokeDescription(clickPath, 0, 1)
            val clickBuilder = GestureDescription.Builder()
            clickBuilder.addStroke(clickStroke)
            accessibilityService?.dispatchGesture(clickBuilder.build(), null, null)
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        val deque: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()
        root?.let {
            deque.add(it)
            while (!deque.isEmpty()) {
                val node: AccessibilityNodeInfo = deque.removeFirst()
                if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                    return node
                }
                for (i in 0 until node.childCount) {
                    deque.addLast(node.getChild(i))
                }
            }
        }
        return null
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent: $p0")
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt:")
    }
}