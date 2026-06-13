package dev.pinkeys.lockscreenpin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.Locale;

public class AutoFocusAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = getServiceInfo();
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
                   | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    private boolean isDeviceLocked() {
        android.app.KeyguardManager km =
            (android.app.KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        return km != null && km.isKeyguardLocked();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if (!isDeviceLocked()) return false;
        int kc = event.getKeyCode();

        // Enter: trackpad center, Enter key
        if (kc == KeyEvent.KEYCODE_DPAD_CENTER || kc == KeyEvent.KEYCODE_ENTER) {
            return clickPinEnter();
        }
        // Delete / Backspace
        if (kc == KeyEvent.KEYCODE_DEL || kc == KeyEvent.KEYCODE_FORWARD_DEL) {
            return clickPinDelete();
        }
        // Digit keys (standard + BlackBerry Q20 letter-mapped keys)
        String digit = keyCodeToDigit(kc);
        if (digit != null) return clickPinButton(digit);
        return false;
    }

    private String keyCodeToDigit(int kc) {
        // Standard digit row (KEYCODE_0–9 = 7–16)
        if (kc >= KeyEvent.KEYCODE_0 && kc <= KeyEvent.KEYCODE_9)
            return String.valueOf(kc - KeyEvent.KEYCODE_0);
        // Numpad digits (KEYCODE_NUMPAD_0–9 = 144–153)
        if (kc >= KeyEvent.KEYCODE_NUMPAD_0 && kc <= KeyEvent.KEYCODE_NUMPAD_9)
            return String.valueOf(kc - KeyEvent.KEYCODE_NUMPAD_0);
        // BlackBerry Q20 physical keyboard: letter keys mapped to PIN digits
        // Layout: W(1) E(2) R(3) / S(4) D(5) F(6) / Z(7) X(8) C(9) / Q(0)
        switch (kc) {
            case KeyEvent.KEYCODE_Q: return "0"; // 45
            case KeyEvent.KEYCODE_W: return "1"; // 51
            case KeyEvent.KEYCODE_E: return "2"; // 33
            case KeyEvent.KEYCODE_R: return "3"; // 46
            case KeyEvent.KEYCODE_S: return "4"; // 47
            case KeyEvent.KEYCODE_D: return "5"; // 32
            case KeyEvent.KEYCODE_F: return "6"; // 34
            case KeyEvent.KEYCODE_Z: return "7"; // 54
            case KeyEvent.KEYCODE_X: return "8"; // 52
            case KeyEvent.KEYCODE_C: return "9"; // 31
            default: return null;
        }
    }

    private boolean clickPinButton(String digit) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        try {
            // Try known SystemUI view IDs across Android versions
            String[] ids = {
                "com.android.systemui:id/key" + digit,
                "com.android.systemui:id/pin_key_" + digit,
                "com.android.systemui:id/digit_" + digit
            };
            for (String id : ids) {
                if (clickById(root, id)) return true;
            }
            // Fallback: walk the tree looking for a clickable button with matching text
            return findAndClick(root, digit);
        } finally {
            root.recycle();
        }
    }

    private boolean clickPinDelete() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        try {
            String[] ids = {
                "com.android.systemui:id/delete_button",
                "com.android.systemui:id/key_backspace",
                "com.android.systemui:id/pin_key_delete"
            };
            for (String id : ids) {
                if (clickById(root, id)) return true;
            }
            return findAndClickByDesc(root, new String[]{"delete", "backspace"});
        } finally {
            root.recycle();
        }
    }

    private boolean clickPinEnter() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;
        try {
            String[] ids = {
                "com.android.systemui:id/key_enter",
                "com.android.systemui:id/pin_key_enter",
                "com.android.systemui:id/check_button"
            };
            for (String id : ids) {
                if (clickById(root, id)) return true;
            }
            return findAndClickByDesc(root, new String[]{"enter", "confirm", "ok"});
        } finally {
            root.recycle();
        }
    }

    private boolean clickById(AccessibilityNodeInfo root, String viewId) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes == null || nodes.isEmpty()) return false;
        for (AccessibilityNodeInfo node : nodes) {
            try {
                if (node.isClickable()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            } finally {
                node.recycle();
            }
        }
        return false;
    }

    private boolean findAndClick(AccessibilityNodeInfo node, String digit) {
        if (node.isClickable()) {
            CharSequence txt = node.getText();
            if (txt != null && txt.toString().trim().equals(digit)) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            boolean found = findAndClick(child, digit);
            child.recycle();
            if (found) return true;
        }
        return false;
    }

    private boolean findAndClickByDesc(AccessibilityNodeInfo node, String[] keywords) {
        if (node.isClickable()) {
            CharSequence desc = node.getContentDescription();
            if (desc != null) {
                String s = desc.toString().toLowerCase(Locale.ROOT);
                for (String kw : keywords) {
                    if (s.contains(kw)) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            boolean found = findAndClickByDesc(child, keywords);
            child.recycle();
            if (found) return true;
        }
        return false;
    }
}
