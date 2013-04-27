package pl.mg6.common.android;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceEx extends ListPreference {

	public ListPreferenceEx(Context context) {
		super(context);
	}

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void setValue(String value) {
		super.setValue(value);
		notifyChanged();
	}
	
	@Override
	public CharSequence getSummary() {
        CharSequence entry = getEntry();
        CharSequence summary = super.getSummary();
        if (summary == null || entry == null) {
            return super.getSummary();
        } else {
            return String.format(summary.toString(), entry);
        }
	}
}
