package dev.pinkeys.lockscreenpin;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends Activity {

    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(64, 64, 64, 64); // window background comes from the Material theme

        TextView title = new TextView(this);
        title.setText("Lockscreen PIN Entry");
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(themeAttrColor(android.R.attr.textColorPrimary, Color.BLACK));

        statusView = new TextView(this);
        statusView.setTextSize(15);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 40, 0, 40);

        Button btn = new Button(this);
        btn.setText("Open Accessibility Settings");
        btn.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        layout.addView(title);
        layout.addView(statusView);
        layout.addView(btn);
        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean enabled = isServiceEnabled();
        if (enabled) {
            statusView.setText("Service is ENABLED");
            statusView.setTextColor(Color.parseColor("#43A047"));
        } else {
            statusView.setText("Service is disabled.\nTap the button below and enable\n\"Lockscreen PIN Entry\".");
            statusView.setTextColor(Color.parseColor("#E53935"));
        }
    }

    /** Resolve a theme color attribute (e.g. textColorPrimary) to an int. */
    private int themeAttrColor(int attr, int fallback) {
        TypedArray a = getTheme().obtainStyledAttributes(new int[]{attr});
        try { return a.getColor(0, fallback); } finally { a.recycle(); }
    }

    private boolean isServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        List<AccessibilityServiceInfo> services =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String pkg = getPackageName();
        for (AccessibilityServiceInfo svc : services) {
            if (pkg.equals(svc.getResolveInfo().serviceInfo.packageName)) return true;
        }
        return false;
    }
}
