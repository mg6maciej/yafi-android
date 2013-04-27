package pl.mg6.yafi;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import pl.mg6.common.Settings;
import pl.mg6.common.android.ViewUtils;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.FreechessUtils;
import pl.mg6.yafi.model.data.Color;
import pl.mg6.yafi.model.data.Game;
import pl.mg6.yafi.model.data.Position;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BoardActivity extends BaseFreechessActivity implements BoardView.OnMoveListener {
	
	private static final int REQUEST_ID_MATCH = 90000;
	private static final int REQUEST_ID_CHAT = 90001;
	private static final int REQUEST_ID_INFO = 90002;
	
	private AlertDialog gameEndDialog;
	private CheckBox showGameEndDialogCheckbox;
	
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
	
	private boolean premove;
	private Queue<String> premoveQueue = new LinkedList<String>();
	
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
		
		premove = Settings.isBoardPremove(this);
	}
	
	@Override
	protected void onPause() {
		dismissGameEndDialog();
		super.onPause();
		Settings.saveCurrentGame(this, currentGameId);
	}
	
	@Override
	protected void onStartHandlingMessages(boolean firstTime) {
		super.onStartHandlingMessages(firstTime);
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
			Toast.makeText(this, R.string.board_no_game_description, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onStopHandlingMessages() {
		super.onStopHandlingMessages();
		boardView.setStateNone();
	}
	
	private void showGameEndDialog(Game game) {
		if (Settings.isShowGameEndDialog(this) && (gameEndDialog == null || !gameEndDialog.isShowing())) {
			gameEndDialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(game.getDescription());
			View body = getLayoutInflater().inflate(R.layout.dont_ask_again, null);
			builder.setView(body);
			if (!game.getDescription().endsWith(" forfeits by disconnection")) {
				builder.setPositiveButton(R.string.rematch, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						service.sendInput("rematch\n");
					}
				});
			}
			if (!"*".equals(game.getResult())) {
				builder.setNeutralButton(R.string.examine, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						service.sendInput("exl\n");
					}
				});
			}
			builder.setNegativeButton(R.string.cancel, null);
			gameEndDialog = builder.create();
			gameEndDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (showGameEndDialogCheckbox.isChecked()) {
						Settings.setShowGameEndDialog(BoardActivity.this, false);
						trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_SHOW_GAME_END_DIALOG, Tracking.LABEL_DIALOG, false);
					}
				}
			});
			gameEndDialog.show();
			showGameEndDialogCheckbox = (CheckBox) body.findViewById(R.id.dont_ask_again_checkbox);
		}
	}
	
	private void dismissGameEndDialog() {
		if (gameEndDialog != null && gameEndDialog.isShowing()) {
			gameEndDialog.dismiss();
		}
		gameEndDialog = null;
	}
	
	public void onFirstClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null) {
			if (game.getRelation() == Game.RELATION_EXAMINING) {
				service.sendInput("backward 999\n");
			} else if (currentPosition > 0) {
				currentPosition = 0;
				Position pos = game.getPosition(currentPosition);
				boardView.setPosition(pos);
			}
		}
	}
	
	public void onPreviousClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null) {
			if (game.getRelation() == Game.RELATION_EXAMINING) {
				service.sendInput("backward\n");
			} else if (currentPosition > 0) {
				currentPosition--;
				Position pos = game.getPosition(currentPosition);
				boardView.setPosition(pos);
			}
		}
	}
	
	public void onNextClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null) {
			if (game.getRelation() == Game.RELATION_EXAMINING) {
				service.sendInput("forward\n");
			} else if (currentPosition < game.getPositionCount() - 1) {
				currentPosition++;
				Position pos = game.getPosition(currentPosition);
				boardView.setPosition(pos);
			}			
		}
	}
	
	public void onLastClick(View view) {
		Game game = service.getGame(currentGameId);
		if (game != null) {
			if (game.getRelation() == Game.RELATION_EXAMINING) {
				service.sendInput("forward 999\n");
			} else if (currentPosition < game.getPositionCount() - 1) {
				currentPosition = game.getPositionCount() - 1;
				Position pos = game.getPosition(currentPosition);
				boardView.setPosition(pos);
			}
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
		//if (pos.getRelation() == Game.RELATION_EXAMINING || currentPosition >= game.getPositionCount() - 2) {
			currentPosition = game.getPositionCount() - 1;
			boardView.setPosition(pos);
			boardView.setFlip(game.isFlip());
		//}
		
		if (!game.isFlip()) {
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
			resultField.setText("none".equals(pos.getPrettyMove()) ? null : pos.getCurrentMoveNumber() + ". " + pos.getPrettyMove());
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
				if (!game.isFlip()) {
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
				UUID gameId = (UUID) msg.obj;
				onGameUpdate(gameId);
				onBoardTabClick(tabs.findViewWithTag(gameId));
				return true;
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
		Game game = service.getGame(gameId);
		if (game == null) {
			return;
		}
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
			while (premoveQueue.size() > 0) {
				service.sendInput(premoveQueue.poll());
				boardView.setMoveSent();
			}
			if (game.getResult() != null && Math.abs(game.getPosition(0).getRelation()) == 1) {
				showGameEndDialog(game);
			}
		} else {
			tabs.findViewWithTag(gameId).findViewById(R.id.board_tab_update).setVisibility(View.VISIBLE);
		}
		int relation = game.getRelation();
		if (!(relation == Game.RELATION_PLAYING_MY_MOVE || relation == Game.RELATION_PLAYING_OPPONENT_MOVE)) {
			View tabClose = tabs.findViewWithTag(gameId).findViewById(R.id.board_tab_close);
			tabClose.setEnabled(true);
		}
	}
	
	private void onIllegalMove() {
		Toast.makeText(this, R.string.illegal_move, Toast.LENGTH_SHORT).show();
	}
	
	private void onDrawOffer() {
		Toast.makeText(this, R.string.draw_offered, Toast.LENGTH_SHORT).show();
	}
	
	private void onAbortRequest() {
		Toast.makeText(this, R.string.abort_requested, Toast.LENGTH_SHORT).show();
	}
	
	private void addTab(UUID id) {
		Game game = service.getGame(id);
		View tab = getLayoutInflater().inflate(R.layout.board_tab, tabs, false);
		tab.setTag(id);
		tab.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				UUID id = (UUID) v.getTag();
				Game game = service.getGame(id);
				String myName = service.getRealUsername();
				final String format = "%s %s";
				if (!myName.equals(game.getWhiteName())) {
					menu.add(String.format(format, getString(R.string.match), game.getWhiteName()));
					menu.add(String.format(format, getString(R.string.chat), game.getWhiteName()));
				}
				menu.add(String.format(format, getString(R.string.informations), game.getWhiteName()));
				if (!myName.equals(game.getBlackName())) {
					menu.add(String.format(format, getString(R.string.match), game.getBlackName()));
					menu.add(String.format(format, getString(R.string.chat), game.getBlackName()));
				}
				if (!game.getWhiteName().equals(game.getBlackName())) {
					menu.add(String.format(format, getString(R.string.informations), game.getBlackName()));
				}
			}
		});
		TextView tabDescription = (TextView) tab.findViewById(R.id.board_tab_desc);
		tabDescription.setText(game.getWhiteName() + "\n" + game.getBlackName());
		int relation = game.getRelation();
		if (relation == Game.RELATION_PLAYING_MY_MOVE || relation == Game.RELATION_PLAYING_OPPONENT_MOVE) {
			View tabClose = tab.findViewById(R.id.board_tab_close);
			tabClose.setEnabled(false);
		}
		int index;
		for (index = 0; index < tabs.getChildCount(); index++) {
			UUID otherId = (UUID) tabs.getChildAt(index).getTag();
			Game other = service.getGame(otherId);
			if (game.getGameTimestamp() > other.getGameTimestamp()) {
				break;
			}
		}
		tabs.addView(tab, index);
	}
	
	public void onBoardTabClick(View view) {
		UUID gameId = (UUID) view.getTag();
		if (currentGameId.equals(gameId)) {
			view.showContextMenu();
		} else {
			currentGameId = gameId;
			Game game = service.getGame(currentGameId);
			view.findViewById(R.id.board_tab_update).setVisibility(View.INVISIBLE);
			currentPosition = Integer.MAX_VALUE;
			updateViews();
			positionInCenter(view);
		}
	}
	
	public void onBoardCloseClick(View view) {
		UUID gameId = (UUID) ((View) view.getParent()).getTag();
		Game game = service.getGame(gameId);
		int relation = Game.RELATION_UNKNOWN;
		if (game != null) {
			relation = game.getRelation();
		}
		if (relation == Game.RELATION_PLAYING_MY_MOVE || relation == Game.RELATION_PLAYING_OPPONENT_MOVE) {
			Toast.makeText(this, "You can't close the board during gameplay.", Toast.LENGTH_SHORT).show();
			return;
		} else if (relation == Game.RELATION_OBSERVING || relation == Game.RELATION_OBSERVING_EXAMINED) {
			service.sendInput("unobserve " + game.getId() + "\n");
		} else if (relation == Game.RELATION_EXAMINING) {
			service.sendInput("unexamine\n");
		}
		removeGame(gameId);
	}
	
	private void removeGame(UUID gameId) {
		allGamesIds.remove(gameId);
		tabs.removeView(tabs.findViewWithTag(gameId));
		service.removeGame(gameId);
		if (allGamesIds.size() == 0) {
			finish();
		} else if (gameId.equals(currentGameId)) {
			onBoardTabClick(tabs.getChildAt(0));
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String[] title = item.getTitle().toString().split(" ");
		if (getString(R.string.match).equals(title[0])) {
			doMatch(title[1]);
			return true;
		} else if (getString(R.string.chat).equals(title[0])) {
			doChat(title[1]);
			return true;
		} else if (getString(R.string.informations).equals(title[0])) {
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
		if (service != null) {
			String move = FreechessUtils.moveToString(initFile, initRank, destFile, destRank) + "\n";
			Game game = service.getGame(currentGameId);
			Position last = game.getPosition(game.getPositionCount() - 1);
			if (last.getRelation() > 0) {
				service.sendInput(move);
			} else if (premove) {
				premoveQueue.offer(move);
			}
		}
	}
	
	@Override
	public void onUnsupportedDrawPicture() {
		trackEvent(Tracking.CATEGORY_STRANGE, Tracking.ACTION_UNSUPPORTED_DRAW_PICTURE, Build.MODEL, Build.VERSION.SDK_INT);
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
		int relation = Game.RELATION_UNKNOWN;
		if (currentGame != null) {
			relation = currentGame.getRelation();
		}
		boolean playing = relation == Game.RELATION_PLAYING_MY_MOVE || relation == Game.RELATION_PLAYING_OPPONENT_MOVE;
		menu.setGroupVisible(R.id.mg_play, playing);
		if (playing) {
			menu.findItem(R.id.mi_resign).setEnabled(currentGame.getPositionCount() > 2);
		}
		menu.setGroupVisible(R.id.mg_observe, relation == Game.RELATION_OBSERVING || relation == Game.RELATION_OBSERVING_EXAMINED);
		menu.setGroupVisible(R.id.mg_examine, relation == Game.RELATION_EXAMINING);
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
		int id = item.getItemId();
		if (id == R.id.mi_abort) {
			service.sendInput("abort\n");
		} else if (id == R.id.mi_draw) {
			service.sendInput("draw\n");
		} else if (id == R.id.mi_resign) {
			service.sendInput("resign\n");
		} else if (id == R.id.mi_unobserve) {
			service.sendInput("unobserve " + service.getGame(currentGameId).getId() + "\n");
		} else if (id == R.id.mi_unexamine) {
			service.sendInput("unexamine\n");
		} else if (id == R.id.mi_flip) {
			Game game = service.getGame(currentGameId);
			game.toggleUserFlip();
			updateViews();
		} else if (id == R.id.mi_review) {
			reviewOverlay.setVisibility(reviewOverlay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			if (reviewOverlay.getVisibility() == View.GONE) {
				Game game = service.getGame(currentGameId);
				if (game != null && currentPosition < game.getPositionCount() - 1) {
					currentPosition = game.getPositionCount() - 1;
					Position pos = game.getPosition(currentPosition);
					boardView.setPosition(pos);
				}
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
