package pl.mg6.yafi;

import pl.mg6.common.android.AndroidUtils;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.data.NewsItem;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class NewsDetailsActivity extends BaseFreechessActivity {
	
	public static final String EXTRA_NAME_NEWS = "pl.mg6...NewsDetailsActivity.news";
	
	private NewsItem news;
	
	private View loading;
	private TextView date;
	private TextView title;
	private TextView message;
	private TextView author;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_details_view);
		news = getIntent().getParcelableExtra(EXTRA_NAME_NEWS);
		
		loading = findViewById(R.id.news_details_loading);
		date = (TextView) findViewById(R.id.news_details_date);
		title = (TextView) findViewById(R.id.news_details_title);
		message = (TextView) findViewById(R.id.news_details_message);
		author = (TextView) findViewById(R.id.news_details_author);
		
		onNewsItem(news);
	}
	
	@Override
	protected void onStartHandlingMessages(boolean firstTime) {
		super.onStartHandlingMessages(firstTime);
		if (firstTime) {
			service.sendInput("news " + news.getId() + "\n");
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_NEWS_DETAILS:
				onNewsItem((NewsItem) msg.obj);
				loading.setVisibility(View.GONE);
				return true;
		}
		return super.handleMessage(msg);
	}
	
	private void onNewsItem(NewsItem item) {
		date.setText(item.getDate());
		title.setText(item.getTitle());
		message.setText(item.getMessage());
		author.setText(item.getAuthor());
		
		AndroidUtils.linkify(title, message);
	}
}
