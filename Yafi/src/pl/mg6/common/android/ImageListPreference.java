package pl.mg6.common.android;

import pl.mg6.yafi.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class ImageListPreference extends ListPreferenceEx {
	
	private static int[] images = {
		R.drawable.board_colors_default,
		R.drawable.board_colors_red,
		R.drawable.board_colors_green,
		R.drawable.board_colors_blue,
		R.drawable.board_colors_butter_chameleon,
		R.drawable.board_colors_sky_plum,
		R.drawable.board_colors_scarlet_aluminium,
	};

	public ImageListPreference(Context context) {
		super(context);
	}

	public ImageListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		ListAdapter adapter = new ImageListPreferenceAdapter(getContext(), getEntryValues(), getEntries(), getValue());
		builder.setAdapter(adapter, this);
	}
	
	private class ImageListPreferenceAdapter extends ArrayAdapter<CharSequence> {
		
		private LayoutInflater layoutInflater;
		
		private CharSequence[] values;
		
		private CharSequence[] texts;
		
		private String selected;
		
		public ImageListPreferenceAdapter(Context context, CharSequence[] values, CharSequence[] texts, String selected) {
			super(context, 0, values);
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.values = values;
			this.texts = texts;
			this.selected = selected;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.pref_board_colors_item, parent, false);
			}
			ImageView image = (ImageView) convertView.findViewById(R.id.pref_board_colors_item_image);
			TextView text = (TextView) convertView.findViewById(R.id.pref_board_colors_item_text);
			RadioButton radio = (RadioButton) convertView.findViewById(R.id.pref_board_colors_item_radio);

			convertView.setBackgroundResource(android.R.drawable.list_selector_background);
			convertView.setTag(values[position]);
			convertView.setOnClickListener(listener);
			text.setText(texts[position]);
			image.setImageResource(images[position]);
			radio.setChecked(selected.equals(values[position]));
			
			return convertView;
		}
		
		private View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String value = v.getTag().toString();
				setValue(value);
				getDialog().dismiss();
			}
		};
	}
}
