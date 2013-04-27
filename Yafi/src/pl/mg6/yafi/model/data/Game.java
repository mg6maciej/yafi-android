package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pl.mg6.common.TimeUtils;

public class Game {
	
	private List<Position> positions;
	
	private UUID uuid;
	
	private int id;
	private String whiteName;
	private String blackName;
	
	private String whiteRating;
	private String blackRating;
	
	private Color toMove;
	
	private int whiteTime;
	private int blackTime;
	private long timestamp;
	private boolean timeRunning;
	
	private String result;
	private String description;
	
	private boolean flip;
	
	private int relation;
	
	public static final int RELATION_UNKNOWN = -666;
	public static final int RELATION_ISOLATED_POSITION = -3;
	public static final int RELATION_OBSERVING_EXAMINED = -2;
	public static final int RELATION_PLAYING_OPPONENT_MOVE = -1;
	public static final int RELATION_OBSERVING = 0;
	public static final int RELATION_PLAYING_MY_MOVE = 1;
	public static final int RELATION_EXAMINING = 2;
	
	public Game() {
		uuid = UUID.randomUUID();
		id = -1;
		positions = new ArrayList<Position>();
	}
	
	public void addPosition(Position pos) {
		if (id == -1) {
			id = pos.getGameId();
		} else if (id != pos.getGameId()) {
			throw new IllegalArgumentException();
		}
		timestamp = pos.getTimestamp();
		if (pos.getBlackName().equals(whiteName)) {
			// switch command
			String rating = whiteRating;
			whiteRating = blackRating;
			blackRating = rating;
		}
		whiteName = pos.getWhiteName();
		blackName = pos.getBlackName();
		toMove = pos.getToMove();
		whiteTime = pos.getWhiteTime();
		blackTime = pos.getBlackTime();
		timeRunning = pos.isTimeRunning();
		flip = pos.isFlip();
		relation = pos.getRelation();
		while (getPositionCount() > 0 && pos.getMoveIndex() <= positions.get(getPositionCount() - 1).getMoveIndex()) {
			positions.remove(getPositionCount() - 1);
		}
		positions.add(pos);
	}
	
	public int getPositionCount() {
		return positions.size();
	}
	
	public Position getPosition(int index) {
		return positions.get(index);
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public int getId() {
		return id;
	}
	
	public String getWhiteName() {
		return whiteName;
	}
	
	public String getBlackName() {
		return blackName;
	}
	
	public String getWhiteRating() {
		return whiteRating;
	}
	
	public void setWhiteRating(String whiteRating) {
		this.whiteRating = whiteRating;
	}
	
	public String getBlackRating() {
		return blackRating;
	}
	
	public void setBlackRating(String blackRating) {
		this.blackRating = blackRating;
	}
	
	public boolean isTimeRunning() {
		return timeRunning;
	}
	
	public String getResult() {
		return result;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isFlip() {
		return flip;
	}
	
	public int getRelation() {
		return relation;
	}
	
	public void setRelation(int relation) {
		this.relation = relation;
	}
	
	public int getCurrentWhiteTime() {
		if (!timeRunning || toMove == Color.BLACK) {
			return whiteTime;
		}
		return whiteTime - (int) (TimeUtils.getTimestamp() - timestamp);
	}

	public int getCurrentBlackTime() {
		if (!timeRunning || toMove == Color.WHITE) {
			return blackTime;
		}
		return blackTime - (int) (TimeUtils.getTimestamp() - timestamp);
	}
	
	public String getCurrentWhiteTimeString() {
		int currentWhiteTime = getCurrentWhiteTime();
		return TimeUtils.formatTime(currentWhiteTime, currentWhiteTime < 60000);
	}
	
	public String getCurrentBlackTimeString() {
		int currentBlackTime = getCurrentBlackTime();
		return TimeUtils.formatTime(currentBlackTime, currentBlackTime < 60000);
	}

	public void setResult(String result, String description) {
		this.result = result;
		this.description = description;
		this.whiteTime = getCurrentWhiteTime();
		this.blackTime = getCurrentBlackTime();
		this.timeRunning = false;
	}

	public void addNote(String note) {
		//TODO: adding system notes
	}
	
	public void addCommunication(Communication c) {
		//TODO: adding user communications
	}
}
