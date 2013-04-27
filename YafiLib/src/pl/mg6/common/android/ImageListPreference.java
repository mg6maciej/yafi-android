package pl.mg6.common.android;

import pl.mg6.yafi.lib.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ImageListPreference extends ListPreferenceEx {
	
	private int layoutResId;
	private int entryId;
	private int imageId;
	private int checkableId;
	private int imagesResId;
	
	public ImageListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageListPreference);
		layoutResId = a.getResourceId(R.styleable.ImageListPreference_layout, 0);
		entryId = a.getResourceId(R.styleable.ImageListPreference_entryId, 0);
		imageId = a.getResourceId(R.styleable.ImageListPreference_imageId, 0);
		checkableId = a.getResourceId(R.styleable.ImageListPreference_checkableId, 0);
		imagesResId = a.getResourceId(R.styleable.ImageListPreference_images, 0);
		a.recycle();
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		ListAdapter adapter = new ImageListPreferenceAdapter(getContext(), getEntryValues(), getValue(), getEntries());
		builder.setAdapter(adapter, this);
	}
	
	private class ImageListPreferenceAdapter extends ArrayAdapter<CharSequence> {
		
		private LayoutInflater layoutInflater;
		
		private CharSequence[] values;
		
		private String selectedValue;
		
		private CharSequence[] texts;
		
		private Drawable[] images;
		
		public ImageListPreferenceAdapter(Context context, CharSequence[] values, String selectedValue, CharSequence[] texts) {
			super(context, 0, values);
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.values = values;
			this.selectedValue = selectedValue;
			this.texts = texts;
			this.images = new Drawable[values.length];
			TypedArray a = context.getResources().obtainTypedArray(imagesResId);
			for (int i = 0; i < values.length; i++) {
				this.images[i] = a.getDrawable(i);
			}
			a.recycle();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = layoutInflater.inflate(layoutResId, parent, false);
			}
			TextView text = (TextView) convertView.findViewById(entryId);
			ImageView image = (ImageView) convertView.findViewById(imageId);
			Checkable checkable = (Checkable) convertView.findViewById(checkableId);

			convertView.setTag(values[position]);
			convertView.setOnClickListener(listener);
			text.setText(texts[position]);
			image.setImageDrawable(images[position]);
			checkable.setChecked(selectedValue.equals(values[position]));
			
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
