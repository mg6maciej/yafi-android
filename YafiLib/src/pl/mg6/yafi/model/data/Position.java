package pl.mg6.yafi.model.data;

import pl.mg6.common.TimeUtils;

public class Position {
	
	private long timestamp;
	private String[] placement = new String[8];
	private Color toMove;
	private int enPassantFile;
	private boolean[] casting = new boolean[4];
	private int movesSinceIrreversible;
	private int gameId;
	private String whiteName;
	private String blackName;
	private int relation;
	private int initialTime;
	private int timeIncrement;
	private int whiteStrength;
	private int blackStrength;
	private int whiteTime;
	private int blackTime;
	private int nextMoveNumber;
	private String verboseMove;
	private String timeTaken;
	private String prettyMove;
	private boolean flip;
	private boolean timeRunning;
	private int lag;
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String[] getPlacement() {
		return placement;
	}
	
	public Color getToMove() {
		return toMove;
	}
	
	public int getEnPassantFile() {
		return enPassantFile;
	}
	
	public boolean[] getCasting() {
		return casting;
	}
	
	public int getMovesSinceIrreversible() {
		return movesSinceIrreversible;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public String getWhiteName() {
		return whiteName;
	}
	
	public String getBlackName() {
		return blackName;
	}
	
	public int getRelation() {
		return relation;
	}
	
	public int getInitialTime() {
		return initialTime;
	}
	
	public int getTimeIncrement() {
		return timeIncrement;
	}
	
	public int getWhiteStrength() {
		return whiteStrength;
	}
	
	public int getBlackStrength() {
		return blackStrength;
	}
	
	public int getWhiteTime() {
		return whiteTime;
	}
	
	public int getBlackTime() {
		return blackTime;
	}
	
	public int getNextMoveNumber() {
		return nextMoveNumber;
	}
	
	public int getCurrentMoveNumber() {
		return (toMove == Color.WHITE) ? (nextMoveNumber - 1) : nextMoveNumber;
	}
	
	public String getVerboseMove() {
		return verboseMove;
	}
	
	public String getTimeTaken() {
		return timeTaken;
	}
	
	public String getPrettyMove() {
		return prettyMove;
	}
	
	public boolean isFlip() {
		return flip;
	}
	
	public boolean isTimeRunning() {
		return timeRunning;
	}
	
	public int getLag() {
		return lag;
	}
	
	public int getMoveIndex() {
		return 2 * (nextMoveNumber - 1) + (toMove == Color.WHITE ? 0 : 1);
	}
	
	public char getPieceAt(int file, int rank) {
		return placement[rank].charAt(file);
	}
	
	public static Position fromStyle12(String style12) {
		Position pos = new Position();
		pos.timestamp = TimeUtils.getTimestamp();
		String[] data = style12.split(" ");
		for (int i = 0; i < 8; i++) {
			pos.placement[i] = data[i];
		}
		pos.toMove = "W".equals(data[8]) ? Color.WHITE : Color.BLACK;
		pos.enPassantFile = Integer.parseInt(data[9]);
		for (int i = 0; i < 4; i++) {
			pos.casting[i] = "1".equals(data[i + 10]);
		}
		pos.movesSinceIrreversible = Integer.parseInt(data[14]);
		pos.gameId = Integer.parseInt(data[15]);
		pos.whiteName = data[16].intern();
		pos.blackName = data[17].intern();
		pos.relation = Integer.parseInt(data[18]);
		pos.initialTime = Integer.parseInt(data[19]);
		pos.timeIncrement = Integer.parseInt(data[20]);
		pos.whiteStrength = Integer.parseInt(data[21]);
		pos.blackStrength = Integer.parseInt(data[22]);
		pos.whiteTime = Integer.parseInt(data[23]);
		pos.blackTime = Integer.parseInt(data[24]);
		pos.nextMoveNumber = Integer.parseInt(data[25]);
		pos.verboseMove = data[26];
		pos.timeTaken = data[27];
		pos.prettyMove = data[28];
		pos.flip = "1".equals(data[29]);
		pos.timeRunning = "1".equals(data[30]);
		pos.lag = Integer.parseInt(data[31]);
		return pos;
	}
}
