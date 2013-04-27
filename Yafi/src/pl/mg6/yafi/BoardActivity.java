package pl.mg6.yafi;

import java.util.List;
import java.util.UUID;

import pl.mg6.common.Settings;
import pl.mg6.common.android.ViewUtils;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.FreechessUtils;
import pl.mg6.yafi.model.data.Color;
import pl.mg6.yafi.model.data.Game;
import pl.mg6.yafi.model.data.Position;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BoardActivity extends BaseFreechessActivity implements BoardView.OnMoveListener {
	
	private static final int REQUEST_ID_MATCH = 90000;
	private static final int REQUEST_ID_CHAT = 90001;
	private static final int REQUEST_ID_INFO = 90002;
	
	private BoardView boardView;
	private LinearLayout tabs;
	private HorizontalScrollView tabsScrollPortrait;
	private ScrollView tabsScrollLandscape;
	
	private TextView whiteNameField;
	private TextView whiteRatingField;
	private TextView whiteTimeField;
	private TextView blackNameField;
	private TextView blackRatingField;
	private TextView blackTimeField;
	private TextView resultField;
	private TextView descriptionField;
	
	private View reviewOverlay;
	
	private Runnable updateTimesCallback;
	
	private List<UUID> allGamesIds;
	
	private UUID currentGameId;
	private int currentPosition;
	
	private boolean showLag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.board_view);
		boardView = (BoardView) findViewById(R.id.board_view);
		boardView.setOnMoveListener(this);
		tabs = (LinearLayout) findViewById(R.id.board_tabs);
		tabsScrollPortrait = (HorizontalScrollView) findViewById(R.id.board_tabs_scroll_portrait);
		tabsScrollLandscape = (ScrollView) findViewById(R.id.board_tabs_scroll_landscape);
		
		whiteNameField = (TextView) findViewById(R.id.board_white_name);
		whiteRatingField = (TextView) findViewById(R.id.board_white_rating);
		whiteTimeField = (TextView) findViewById(R.id.board_white_time);
		blackNameField = (TextView) findViewById(R.id.board_black_name);
		blackRatingField = (TextView) findViewById(R.id.board_black_rating);
		blackTimeField = (TextView) findViewById(R.id.board_black_time);
		resultField = (TextView) findViewById(R.id.board_result);
		descriptionField = (TextView) findViewById(R.id.board_description);
		
		reviewOverlay = findViewById(R.id.board_review_overlay);
		
		updateTimesCallback = new Runnable() {
			@Override
			public void run() {
				updateTimes();
			}
		};
		
		currentGameId = Settings.loadCurrentGame(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Settings.saveCurrentGame(this, currentGameId);
	}
	
	@Override
	protected void onStartHandlingMessages() {
		super.onStartHandlingMessages();
		allGamesIds = service.getAllGamesIds();
		if (allGamesIds.size() > 0) {
			if (currentGameId == null || !allGamesIds.contains(currentGameId)) {
				currentGameId = allGamesIds.get(0);
			}
			createViews();
			currentPosition = Integer.MAX_VALUE;
			updateViews();
			final View selected = tabs.getChildAt(allGamesIds.indexOf(currentGameId));
			selected.post(new Runnable() {
				@Override
				public void run() {
					positionInCenter(selected);
				}
			});
		} else {
			currentGameId = null;
			Toast.makeText(this, "To start playing, go to \"Sought\" screen\nand choose game posted by other player.", Toast.LENGTH_LONG).show();
		}
	}
	
	public void onFirstClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null && currentPosition > 0) {
			currentPosition = 0;
			Position pos = game.getPosition(currentPosition);
			boardView.setPosition(pos);
		}
	}
	
	public void onPreviousClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null && currentPosition > 0) {
			currentPosition--;
			Position pos = game.getPosition(currentPosition);
			boardView.setPosition(pos);
		}
	}
	
	public void onNextClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null && currentPosition < game.getPositionCount() - 1) {
			currentPosition++;
			Position pos = game.getPosition(currentPosition);
			boardView.setPosition(pos);
		}
	}
	
	public void onLastClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null && currentPosition < game.getPositionCount() - 1) {
			currentPosition = game.getPositionCount() - 1;
			Position pos = game.getPosition(currentPosition);
			boardView.setPosition(pos);
		}
	}
	
	private void createViews() {
		tabs.removeAllViews();
		for (UUID id : allGamesIds) {
			addTab(id);
		}
	}
	
	private void updateViews() {
		Game game = service.getGame(currentGameId);
		Position pos = game.getPosition(game.getPositionCount() - 1);
		if (currentPosition >= game.getPositionCount() - 2) {
			currentPosition = game.getPositionCount() - 1;
			boardView.setPosition(pos);
		}
		
		if (!pos.isFlip()) {
			whiteNameField.setText(game.getWhiteName());
			blackNameField.setText(game.getBlackName());
			whiteRatingField.setText(game.getWhiteRating());
			blackRatingField.setText(game.getBlackRating());
		} else {
			whiteNameField.setText(game.getBlackName());
			blackNameField.setText(game.getWhiteName());
			whiteRatingField.setText(game.getBlackRating());
			blackRatingField.setText(game.getWhiteRating());
		}
		if (game.getResult() != null) {
			resultField.setText(game.getResult());
			descriptionField.setText(game.getDescription());
		} else {
			resultField.setText("none".equals(pos.getPrettyMove()) ? null : pos.getPrettyMove());
			descriptionField.setText(null);
		}
		
		updateTimes();
	}
	
	private void updateTimes() {
		whiteTimeField.removeCallbacks(updateTimesCallback);
		if (service != null) {
			Game game = service.getGame(currentGameId);
			if (game != null) {
				Position pos = game.getPosition(game.getPositionCount() - 1);
				String newWhiteTime;
				String newBlackTime;
				if (!pos.isFlip()) {
					newWhiteTime = game.getCurrentWhiteTimeString();
					newBlackTime = game.getCurrentBlackTimeString();
				} else {
					newWhiteTime = game.getCurrentBlackTimeString();
					newBlackTime = game.getCurrentWhiteTimeString();
				}
				if (showLag) {
					if (pos.getLag() > 0 || game.getPositionCount() > 2 && (pos = game.getPosition(game.getPositionCount() - 3)).getLag() > 0) {
						if (pos.getToMove() == Color.BLACK) {
							newWhiteTime += " (lag:" + pos.getLag() + "ms)";
						} else {
							newBlackTime += " (lag:" + pos.getLag() + "ms)";
						}
					}
					if (game.getPositionCount() > 1) {
						pos = game.getPosition(game.getPositionCount() - 2);
						if (pos.getLag() > 0) {
							if (pos.getToMove() == Color.BLACK) {
								newWhiteTime += " (lag:" + pos.getLag() + "ms)";
							} else {
								newBlackTime += " (lag:" + pos.getLag() + "ms)";
							}
						}
					}
				}
				if (!whiteTimeField.getText().equals(newWhiteTime)) {
					whiteTimeField.setText(newWhiteTime);
				}
				if (!blackTimeField.getText().equals(newBlackTime)) {
					blackTimeField.setText(newBlackTime);
				}
				if (game.isTimeRunning()) {
					whiteTimeField.postDelayed(updateTimesCallback, 45);
				}
			}
		}
	}
	
	private void positionInCenter(View v) {
		for (int i = 0; i < tabs.getChildCount(); i++) {
			tabs.getChildAt(i).setSelected(false);
		}
		v.setSelected(true);
		if (tabsScrollPortrait != null) {
			ViewUtils.centerViewInScroll(v, tabsScrollPortrait);
		} else {
			ViewUtils.centerViewInScroll(v, tabsScrollLandscape);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_GAME_CREATE:
			case FreechessService.MSG_ID_GAME_UPDATE:
				onGameUpdate((UUID) msg.obj);
				return true;
			case FreechessService.MSG_ID_ILLEGAL_MOVE:
				onIllegalMove();
				break;
			case FreechessService.MSG_ID_DRAW_OFFER:
				onDrawOffer();
				break;
			case FreechessService.MSG_ID_ABORT_REQUEST:
				onAbortRequest();
				break;
		}
		return super.handleMessage(msg);
	}
	
	private void onGameUpdate(UUID gameId) {
		if (!allGamesIds.contains(gameId)) {
			allGamesIds.add(gameId);
			addTab(gameId);
		}
		if (currentGameId == null) {
			currentGameId = gameId;
			final View selected = tabs.getChildAt(allGamesIds.indexOf(currentGameId));
			selected.post(new Runnable() {
				@Override
				public void run() {
					positionInCenter(selected);
				}
			});
			currentPosition = Integer.MAX_VALUE;
			updateViews();
		} else if (currentGameId.equals(gameId)) {
			updateViews();
		} else {
			Game game = service.getGame(gameId);
			((Button) tabs.findViewWithTag(gameId)).setText(game.getWhiteName() + "*\n" + game.getBlackName());
		}
	}
	
	private void onIllegalMove() {
		Toast.makeText(this, "Illegal move.", Toast.LENGTH_SHORT).show();
	}
	
	private void onDrawOffer() {
		Toast.makeText(this, "Draw offered.", Toast.LENGTH_SHORT).show();
	}
	
	private void onAbortRequest() {
		Toast.makeText(this, "Abort requested.", Toast.LENGTH_SHORT).show();
	}
	
	private void addTab(UUID id) {
		Button b = new Button(this);
		Game game = service.getGame(id);
		b.setText(game.getWhiteName() + "\n" + game.getBlackName());
		b.setLines(2);
		b.setMinWidth(getResources().getDimensionPixelSize(R.dimen.board_tab_item_min_width));
		b.setTag(id);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentGameId.equals(v.getTag())) {
					v.showContextMenu();
				} else {
					currentGameId = (UUID) v.getTag();
					Game game = service.getGame(currentGameId);
					((Button) v).setText(game.getWhiteName() + "\n" + game.getBlackName());
					currentPosition = Integer.MAX_VALUE;
					updateViews();
					positionInCenter(v);
				}
			}
		});
		b.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				UUID id = (UUID) v.getTag();
				Game game = service.getGame(id);
				String myName = service.getRealUsername();
				if (!myName.equals(game.getWhiteName())) {
					menu.add("Match " + game.getWhiteName());
					menu.add("Chat " + game.getWhiteName());
					menu.add("Informations " + game.getWhiteName());
				}
				if (!myName.equals(game.getBlackName())) {
					menu.add("Match " + game.getBlackName());
					menu.add("Chat " + game.getBlackName());
					menu.add("Informations " + game.getBlackName());
				}
			}
		});
		tabs.addView(b);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String[] title = item.getTitle().toString().split(" ");
		if ("Match".equals(title[0])) {
			doMatch(title[1]);
			return true;
		} else if ("Chat".equals(title[0])) {
			doChat(title[1]);
			return true;
		} else if ("Informations".equals(title[0])) {
			doInfo(title[1]);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void doMatch(String user) {
		Intent intent = new Intent(this, MatchActivity.class);
		intent.putExtra(MatchActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_MATCH);
	}
	
	private void doChat(String user) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_CHAT);
	}
	
	private void doInfo(String user) {
		Intent intent = new Intent(this, InformationsActivity.class);
		intent.putExtra(InformationsActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_INFO);
	}
	
	@Override
	public void onMove(int initFile, int initRank, int destFile, int destRank) {
		service.sendInput(FreechessUtils.moveToString(initFile, initRank, destFile, destRank) + "\n");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.board, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Game currentGame = service.getGame(currentGameId);
		if (currentGame == null || currentGame.getResult() != null || Math.abs(currentGame.getPosition(currentPosition).getRelation()) != 1) {
			menu.findItem(R.id.mi_abort).setEnabled(false);
			menu.findItem(R.id.mi_draw).setEnabled(false);
			menu.findItem(R.id.mi_resign).setEnabled(false);
		} else {
			menu.findItem(R.id.mi_abort).setEnabled(true);
			menu.findItem(R.id.mi_draw).setEnabled(true);
			menu.findItem(R.id.mi_resign).setEnabled(currentGame.getPositionCount() > 2);
		}
		MenuItem review = menu.findItem(R.id.mi_review);
		if (reviewOverlay.getVisibility() == View.VISIBLE) {
			review.setTitle(R.string.hide_controls);
		} else {
			review.setTitle(R.string.show_controls);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_abort:
				service.sendInput("abort\n");
				return true;
			case R.id.mi_draw:
				service.sendInput("draw\n");
				return true;
			case R.id.mi_resign:
				service.sendInput("resign\n");
				return true;
			case R.id.mi_review:
				reviewOverlay.setVisibility(reviewOverlay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
				if (reviewOverlay.getVisibility() == View.GONE) {
					Game game = service.getGame(currentGameId);
					if (game != null && currentPosition < game.getPositionCount() - 1) {
						currentPosition = game.getPositionCount() - 1;
						Position pos = game.getPosition(currentPosition);
						boardView.setPosition(pos);
					}
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
