package pl.mg6.yafi;

import java.util.ArrayList;
import java.util.List;

import pl.mg6.common.StringUtils;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.data.AdjournedInfo;
import pl.mg6.yafi.model.data.Color;
import pl.mg6.yafi.model.data.FingerInfo;
import pl.mg6.yafi.model.data.HistoryInfo;
import pl.mg6.yafi.model.data.JournalInfo;
import pl.mg6.yafi.model.data.RatingInfo;
import pl.mg6.yafi.model.data.UserTitle;
import pl.mg6.yafi.model.data.VariablesInfo;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InformationsActivity extends BaseFreechessActivity {
	
	public static final String EXTRA_NAME_USERNAME = "pl.mg6...InformationsActivity.username";
	
	private String username;
	
	private static final int STATE_INITIAL = -1;
	private static final int STATE_AFTER_FINGER = 0;
	private static final int STATE_AFTER_VARIABLES = 1;
	private static final int STATE_AFTER_HISTORY = 2;
	private static final int STATE_AFTER_JOURNAL = 3;
	private static final int STATE_AFTER_ADJOURNED = 4;
	private int state;
	
	private static final int[] notesIds = {
		R.id.info_finger_note_1,
		R.id.info_finger_note_2,
		R.id.info_finger_note_3,
		R.id.info_finger_note_4,
		R.id.info_finger_note_5,
		R.id.info_finger_note_6,
		R.id.info_finger_note_7,
		R.id.info_finger_note_8,
		R.id.info_finger_note_9,
		R.id.info_finger_note_10,
	};
	private static final int[] currentRatingIds = {
		R.id.info_finger_rating_blitz_current,
		R.id.info_finger_rating_standard_current,
		R.id.info_finger_rating_lightning_current,
		R.id.info_finger_rating_wild_current,
		R.id.info_finger_rating_bughouse_current,
		R.id.info_finger_rating_crazyhouse_current,
		R.id.info_finger_rating_suicide_current,
		R.id.info_finger_rating_losers_current,
		R.id.info_finger_rating_atomic_current,
	};
	private static final int[] gamesIds = {
		R.id.info_finger_rating_blitz_games,
		R.id.info_finger_rating_standard_games,
		R.id.info_finger_rating_lightning_games,
		R.id.info_finger_rating_wild_games,
		R.id.info_finger_rating_bughouse_games,
		R.id.info_finger_rating_crazyhouse_games,
		R.id.info_finger_rating_suicide_games,
		R.id.info_finger_rating_losers_games,
		R.id.info_finger_rating_atomic_games,
	};
	private static final String[] ratingNames = {
		"Blitz", "Standard", "Lightning",
		"Wild", "Bughouse", "Crazyhouse",
		"Suicide", "Losers", "Atomic",
	};
	private static final String RATING_WITHOUT_BEST = "%s\u00A0rating:\u00A0%s wins:\u00A0%d/%d";
	private static final String RATING_WITH_BEST = "%s\u00A0rating:\u00A0%s (best\u00A0active\u00A0%s) wins:\u00A0%d/%d";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_view);
		username = getIntent().getStringExtra(EXTRA_NAME_USERNAME);
		
		TextView usernameField = (TextView) findViewById(R.id.info_username);
		usernameField.setText("Informations about: " + username);
	}
	
	@Override
	protected void onStartHandlingMessages() {
		super.onStartHandlingMessages();
		if (state != STATE_AFTER_ADJOURNED) {
			service.sendInput("finger " + username + "\n"
					+ "vars " + username + "\n"
					+ "history " + username + "\n"
					+ "journal " + username + "\n"
					+ "stored " + username + "\n");
			state = STATE_INITIAL;
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_FINGER:
				onFinger((FingerInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_VARIABLES:
				onVariables((VariablesInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_HISTORY:
				onHistory((HistoryInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_NO_HISTORY:
				onNoHistory((String) msg.obj);
				return true;
			case FreechessService.MSG_ID_JOURNAL:
				onJournal((JournalInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_NO_JOURNAL:
				onNoJournal((String) msg.obj);
				return true;
			case FreechessService.MSG_ID_PRIVATE_JOURNAL:
				onPrivateJournal();
				return true;
			case FreechessService.MSG_ID_UNREG_JOURNAL:
				onUnregJournal();
				return true;
			case FreechessService.MSG_ID_ADJOURNED:
				onAdjourned((AdjournedInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_NO_ADJOURNED:
				onNoAdjourned((String) msg.obj);
				return true;
		}
		return super.handleMessage(msg);
	}

	private void onFinger(FingerInfo info) {
		if (state == STATE_INITIAL && username.equals(info.getUser())) {
			state = STATE_AFTER_FINGER;
			View finger = ((ViewStub) findViewById(R.id.info_finger_stub)).inflate();
			if (info.getTitles().length > 0) {
				TextView titlesField = (TextView) finger.findViewById(R.id.info_finger_titles);
				String[] titles = new String[info.getTitles().length];
				for (int i = 0; i < titles.length; i++) {
					titles[i] = UserTitle.abbrToText(info.getTitles()[i]);
				}
				titlesField.setText(StringUtils.join(", ", titles));
			}
			List<RatingInfo> ratingInfos = new ArrayList<RatingInfo>();
			ratingInfos.add(info.getBlitz());
			ratingInfos.add(info.getStandard());
			ratingInfos.add(info.getLightning());
			ratingInfos.add(info.getWild());
			ratingInfos.add(info.getBuhouse());
			ratingInfos.add(info.getCrazyhouse());
			ratingInfos.add(info.getSuicide());
			ratingInfos.add(info.getLosers());
			ratingInfos.add(info.getAtomic());
			
			for (int i = 0; i < ratingInfos.size(); i++) {
				RatingInfo ratingInfo = ratingInfos.get(i);
				if (ratingInfo != null) {
					TextView current = (TextView) finger.findViewById(currentRatingIds[i]);
					current.setVisibility(View.VISIBLE);
					String text;
					if (ratingInfo.getBest() == null) {
						text = String.format(RATING_WITHOUT_BEST, ratingNames[i], ratingInfo.getRating(), ratingInfo.getWins(), ratingInfo.getTotal());
					} else {
						text = String.format(RATING_WITH_BEST, ratingNames[i], ratingInfo.getRating(), ratingInfo.getBest(), ratingInfo.getWins(), ratingInfo.getTotal());
					}
					current.setText(text);
					ProgressBar games = (ProgressBar) finger.findViewById(gamesIds[i]);
					games.setMax(ratingInfo.getTotal());
					games.setProgress(ratingInfo.getWins());
					games.setSecondaryProgress(ratingInfo.getWins() + ratingInfo.getDraws());
					games.setVisibility(View.VISIBLE);
				}
			}
			for (int i = 0; i < info.getLineCount(); i++) {
				String line = info.getLine(i);
				TextView lineField = (TextView) finger.findViewById(notesIds[i]);
				lineField.setVisibility(View.VISIBLE);
				lineField.setText(line);
			}
		}
	}

	private void onVariables(VariablesInfo info) {
		if (state == STATE_AFTER_FINGER && username.equals(info.getUser())) {
			findViewById(R.id.info_finger_vars_loading).setVisibility(View.GONE);
			state = STATE_AFTER_VARIABLES;
			View vars = ((ViewStub) findViewById(R.id.info_vars_stub)).inflate();
			TextView interfaceField = (TextView) vars.findViewById(R.id.info_vars_interface);
			interfaceField.setText(info.getClientName());
		}
	}

	private void onHistory(HistoryInfo info) {
		if (state == STATE_AFTER_VARIABLES && username.equals(info.getUser())) {
			findViewById(R.id.info_history_loading).setVisibility(View.GONE);
			state = STATE_AFTER_HISTORY;
			ViewGroup history = (ViewGroup) ((ViewStub) findViewById(R.id.info_history_stub)).inflate();
			for (HistoryInfo.Entry infoEntry : info) {
				View entryView = getLayoutInflater().inflate(R.layout.info_history_entry, history, false);
				entryView.setTag(infoEntry);
				
				TextView whiteNameField = (TextView) entryView.findViewById(R.id.info_history_entry_white_name);
				whiteNameField.setText(infoEntry.getColor() == Color.WHITE ? username : infoEntry.getOpponentName());
				TextView blackNameField = (TextView) entryView.findViewById(R.id.info_history_entry_black_name);
				blackNameField.setText(infoEntry.getColor() == Color.BLACK ? username : infoEntry.getOpponentName());
				int whiteRating = infoEntry.getColor() == Color.WHITE ? infoEntry.getRating() : infoEntry.getOpponentRating();
				if (whiteRating != 0) {
					TextView whiteRatingField = (TextView) entryView.findViewById(R.id.info_history_entry_white_rating);
					whiteRatingField.setText("" + whiteRating);
				}
				int blackRating = infoEntry.getColor() == Color.BLACK ? infoEntry.getRating() : infoEntry.getOpponentRating();
				if (blackRating != 0) {
					TextView blackRatingField = (TextView) entryView.findViewById(R.id.info_history_entry_black_rating);
					blackRatingField.setText("" + blackRating);
				}
				TextView timeField = (TextView) entryView.findViewById(R.id.info_history_entry_time);
				timeField.setText("" + infoEntry.getTime());
				TextView incrementField = (TextView) entryView.findViewById(R.id.info_history_entry_increment);
				incrementField.setText("" + infoEntry.getIncrement());
				String result = infoEntry.getResult() == 0 ? "1/2-1/2" : (infoEntry.getResult() == 1 && infoEntry.getColor() == Color.WHITE || infoEntry.getResult() == -1 && infoEntry.getColor() == Color.BLACK) ? "1-0" : "0-1";
				TextView resultField = (TextView) entryView.findViewById(R.id.info_history_entry_result);
				resultField.setText(result);
				int color = 0x66FFFF00;
				if (infoEntry.getResult() == -1) {
					color = 0x66FF0000;
				} else if (infoEntry.getResult() == 1) {
					color = 0x6600FF00;
				}
				resultField.setBackgroundColor(color);
				
				history.addView(entryView);
			}
		}
	}

	private void onNoHistory(String user) {
		if (state == STATE_AFTER_VARIABLES && username.equals(user)) {
			findViewById(R.id.info_history_loading).setVisibility(View.GONE);
			findViewById(R.id.info_no_history).setVisibility(View.VISIBLE);
			state = STATE_AFTER_HISTORY;
		}
	}

	private void onJournal(JournalInfo info) {
		if (state == STATE_AFTER_HISTORY && username.equals(info.getUser())) {
			findViewById(R.id.info_journal_loading).setVisibility(View.GONE);
			state = STATE_AFTER_JOURNAL;
			ViewGroup journal = (ViewGroup) ((ViewStub) findViewById(R.id.info_journal_stub)).inflate();
			for (JournalInfo.Entry infoEntry : info) {
				View entryView = getLayoutInflater().inflate(R.layout.info_journal_entry, journal, false);
				entryView.setTag(infoEntry);
				
				TextView whiteNameField = (TextView) entryView.findViewById(R.id.info_journal_entry_white_name);
				whiteNameField.setText(infoEntry.getWhiteName());
				TextView blackNameField = (TextView) entryView.findViewById(R.id.info_journal_entry_black_name);
				blackNameField.setText(infoEntry.getBlackName());
				int whiteRating = infoEntry.getWhiteRating();
				if (whiteRating != 0) {
					TextView whiteRatingField = (TextView) entryView.findViewById(R.id.info_journal_entry_white_rating);
					whiteRatingField.setText("" + whiteRating);
				}
				int blackRating = infoEntry.getBlackRating();
				if (blackRating != 0) {
					TextView blackRatingField = (TextView) entryView.findViewById(R.id.info_journal_entry_black_rating);
					blackRatingField.setText("" + blackRating);
				}
				TextView timeField = (TextView) entryView.findViewById(R.id.info_journal_entry_time);
				timeField.setText("" + infoEntry.getTime());
				TextView incrementField = (TextView) entryView.findViewById(R.id.info_journal_entry_increment);
				incrementField.setText("" + infoEntry.getIncrement());
				String result = infoEntry.getResult();
				TextView resultField = (TextView) entryView.findViewById(R.id.info_journal_entry_result);
				resultField.setText(result);
				
				journal.addView(entryView);
			}
		}
	}

	private void onNoJournal(String user) {
		if (state == STATE_AFTER_HISTORY && username.equals(user)) {
			findViewById(R.id.info_journal_loading).setVisibility(View.GONE);
			findViewById(R.id.info_no_journal).setVisibility(View.VISIBLE);
			state = STATE_AFTER_JOURNAL;
		}
	}

	private void onPrivateJournal() {
		if (state == STATE_AFTER_HISTORY) {
			findViewById(R.id.info_journal_loading).setVisibility(View.GONE);
			findViewById(R.id.info_private_journal).setVisibility(View.VISIBLE);
			state = STATE_AFTER_JOURNAL;
		}
	}

	private void onUnregJournal() {
		if (state == STATE_AFTER_HISTORY) {
			findViewById(R.id.info_journal_loading).setVisibility(View.GONE);
			findViewById(R.id.info_unreg_journal).setVisibility(View.VISIBLE);
			state = STATE_AFTER_JOURNAL;
		}
	}

	private void onAdjourned(AdjournedInfo info) {
		if (state == STATE_AFTER_JOURNAL && username.equals(info.getUser())) {
			findViewById(R.id.info_adjourned_loading).setVisibility(View.GONE);
			state = STATE_AFTER_ADJOURNED;
			ViewGroup adjourned = (ViewGroup) ((ViewStub) findViewById(R.id.info_adjourned_stub)).inflate();
			for (AdjournedInfo.Entry infoEntry : info) {
				View entryView = getLayoutInflater().inflate(R.layout.info_adjourned_entry, adjourned, false);
				entryView.setTag(infoEntry);
				
				TextView whiteNameField = (TextView) entryView.findViewById(R.id.info_adjourned_entry_white_name);
				whiteNameField.setText(infoEntry.getColor() == Color.WHITE ? username : infoEntry.getOpponentName());
				TextView blackNameField = (TextView) entryView.findViewById(R.id.info_adjourned_entry_black_name);
				blackNameField.setText(infoEntry.getColor() == Color.BLACK ? username : infoEntry.getOpponentName());
				int whiteStrength = infoEntry.getWhiteStrength();
				TextView whiteStrengthField = (TextView) entryView.findViewById(R.id.info_adjourned_entry_white_strength);
				whiteStrengthField.setText("" + whiteStrength);
				int blackRating = infoEntry.getBlackStrength();
				TextView blackRatingField = (TextView) entryView.findViewById(R.id.info_adjourned_entry_black_strength);
				blackRatingField.setText("" + blackRating);
				TextView timeField = (TextView) entryView.findViewById(R.id.info_adjourned_entry_time);
				timeField.setText("" + infoEntry.getTime());
				TextView incrementField = (TextView) entryView.findViewById(R.id.info_adjourned_entry_increment);
				incrementField.setText("" + infoEntry.getIncrement());
				
				adjourned.addView(entryView);
			}
		}
	}

	private void onNoAdjourned(String user) {
		if (state == STATE_AFTER_JOURNAL && username.equals(user)) {
			findViewById(R.id.info_adjourned_loading).setVisibility(View.GONE);
			findViewById(R.id.info_no_adjourned).setVisibility(View.VISIBLE);
			state = STATE_AFTER_ADJOURNED;
		}
	}
	
	public void onHistoryEntryClick(View view) {
		HistoryInfo.Entry entry = (HistoryInfo.Entry) view.getTag();
		service.sendInput("examine " + username + " " + entry.getId() + "\n");
	}
	
	public void onJournalEntryClick(View view) {
		JournalInfo.Entry entry = (JournalInfo.Entry) view.getTag();
		service.sendInput("examine " + username + " " + entry.getId() + "\n");
	}
	
	public void onAdjournedEntryClick(View view) {
		AdjournedInfo.Entry entry = (AdjournedInfo.Entry) view.getTag();
		service.sendInput("examine " + username + " " + entry.getOpponentName() + "\n");
	}
}