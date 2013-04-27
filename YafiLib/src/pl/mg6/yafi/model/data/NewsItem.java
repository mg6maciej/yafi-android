package pl.mg6.yafi.model.data;

import java.util.regex.Matcher;

import android.os.Parcel;
import android.os.Parcelable;

import pl.mg6.yafi.model.FreechessUtils;

public class NewsItem implements Comparable<NewsItem>, Parcelable {
	
	private int id;
	
	private String date;
	
	private String title;
	
	private String message;
	
	private String author;
	
	public int getId() {
		return id;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getAuthor() {
		return author;
	}
	
	@Override
	public int compareTo(NewsItem another) {
		if (another == null) {
			return 1;
		}
		return another.id - id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof NewsItem)) {
			return false;
		}
		NewsItem other = (NewsItem) o;
		return id == other.id;
	}
	
	@Override
	public int hashCode() {
		return 31 * id;
	}
	
	public static NewsItem fromListMatcher(Matcher m) {
		NewsItem item = new NewsItem();
		item.id = Integer.parseInt(m.group(1));
		item.date = m.group(2);
		item.title = m.group(3);
		return item;
	}
	
	public static NewsItem fromDetailsMatcher(Matcher m) {
		NewsItem item = new NewsItem();
		item.id = Integer.parseInt(m.group(1));
		item.date = m.group(2);
		item.title = m.group(3);
		item.message = m.group(4);
		item.author = m.group(5);
		m = FreechessUtils.NEWS_DETAILS_SEPARATOR.matcher(item.message);
		item.message = m.replaceAll("");
		return item;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(date);
		dest.writeString(title);
		dest.writeString(message);
		dest.writeString(author);
	}
	
	public static final Parcelable.Creator<NewsItem> CREATOR = new Parcelable.Creator<NewsItem>() {
		
		@Override
		public NewsItem[] newArray(int size) {
			return new NewsItem[size];
		}
		
		@Override
		public NewsItem createFromParcel(Parcel source) {
			NewsItem item = new NewsItem();
			item.id = source.readInt();
			item.date = source.readString();
			item.title = source.readString();
			item.message = source.readString();
			item.author = source.readString();
			return item;
		}
	};
}
