package pl.mg6.yafi;

import java.util.HashMap;
import java.util.Map;

import pl.mg6.common.Settings;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.data.Position;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.larvalabs.svgandroid.SVGParser;

public class BoardView extends View {
	
	private static final String TAG = BoardView.class.getSimpleName();
	
	private OnMoveListener listener;
	
	private Position position;
	
	private boolean flip;
	
	private Paint paint;
	private RectF rect;
	
	private Map<Character, Picture> pictures;
	private Map<Character, Bitmap> bitmaps;
	
	private int initFile;
	private int initRank;
	private int destFile;
	private int destRank;
	private float draggingX;
	private float draggingY;
	
	private int inputMethod;
	private int lightSquareColor;
	private int darkSquareColor;
	private boolean premove;
	
	private int state;
	private static final int NONE = 0;
	private static final int INITIAL = 1;
	private static final int DRAGGING = 2;
	private static final int CLICK = 3;
	private static final int CLICK_CLICK = 4;
	private static final int MOVE_SENT = 666;

	public BoardView(Context context) {
		super(context);
		init();
	}

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		rect = new RectF();
		
		pictures = new HashMap<Character, Picture>();
		pictures.put('P', SVGParser.getSVGFromResource(getResources(), R.raw.default_white_pawn).getPicture());
		pictures.put('N', SVGParser.getSVGFromResource(getResources(), R.raw.default_white_knight).getPicture());
		pictures.put('B', SVGParser.getSVGFromResource(getResources(), R.raw.default_white_bishop).getPicture());
		pictures.put('R', SVGParser.getSVGFromResource(getResources(), R.raw.default_white_rook).getPicture());
		pictures.put('Q', SVGParser.getSVGFromResource(getResources(), R.raw.default_white_queen).getPicture());
		pictures.put('K', SVGParser.getSVGFromResource(getResources(), R.raw.default_white_king).getPicture());
		pictures.put('p', SVGParser.getSVGFromResource(getResources(), R.raw.default_black_pawn).getPicture());
		pictures.put('n', SVGParser.getSVGFromResource(getResources(), R.raw.default_black_knight).getPicture());
		pictures.put('b', SVGParser.getSVGFromResource(getResources(), R.raw.default_black_bishop).getPicture());
		pictures.put('r', SVGParser.getSVGFromResource(getResources(), R.raw.default_black_rook).getPicture());
		pictures.put('q', SVGParser.getSVGFromResource(getResources(), R.raw.default_black_queen).getPicture());
		pictures.put('k', SVGParser.getSVGFromResource(getResources(), R.raw.default_black_king).getPicture());
		
		inputMethod = Settings.getBoardInputMethod(getContext());
		String colorsKey = Settings.getBoardColors(getContext());
		if ("red".equals(colorsKey)) {
			lightSquareColor = Color.rgb(255, 158, 158);
			darkSquareColor = Color.rgb(209, 71, 71);
		} else if ("green".equals(colorsKey)) {
			lightSquareColor = Color.rgb(206, 255, 158);
			darkSquareColor = Color.rgb(139, 209, 71);
		} else if ("blue".equals(colorsKey)) {
			lightSquareColor = Color.rgb(158, 201, 255);
			darkSquareColor = Color.rgb(71, 133, 209);
		} else if ("butter_chameleon".equals(colorsKey)) {
			lightSquareColor = Color.rgb(254, 244, 158);
			darkSquareColor = Color.rgb(178, 236, 122);
		} else if ("sky_plum".equals(colorsKey)) {
			lightSquareColor = Color.rgb(174, 201, 228);
			darkSquareColor = Color.rgb(204, 176, 201);
		} else if ("scarlet_aluminium".equals(colorsKey)) {
			lightSquareColor = Color.rgb(238, 238, 236);
			darkSquareColor = Color.rgb(248, 152, 152);
		} else {
			lightSquareColor = Color.rgb(255, 206, 158);
			darkSquareColor = Color.rgb(209, 139, 71);
		}
		
		premove = Settings.isBoardPremove(getContext());
	}
	
	public void setStateNone() {
		state = NONE;
	}
	
	public void setMoveSent() {
		state = MOVE_SENT;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		try {
			drawSelf(canvas);
		} catch (UnsupportedOperationException ex) {
			
			int bitmapSize = getWidth() / 8;
			bitmaps = new HashMap<Character, Bitmap>();
			for (Character piece : pictures.keySet()) {
				Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Config.ARGB_8888);
				Canvas c = new Canvas(bitmap);
				c.drawPicture(pictures.get(piece), new Rect(0, 0, bitmapSize, bitmapSize));
				bitmaps.put(piece, bitmap);
			}
			pictures = null;
			invalidate();
			
			if (listener != null) {
				listener.onUnsupportedDrawPicture();
			}
		}
	}
	
	private void drawSelf(Canvas canvas) {
		float squareWidth = getWidth() / 8.0f;
		float squareHeight = getHeight() / 8.0f;
		paint.setColor(darkSquareColor);
		canvas.drawPaint(paint);
		paint.setColor(lightSquareColor);
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				if ((x + y) % 2 == 0) {
					rect.left = x * squareWidth;
					rect.right = rect.left + squareWidth;
					rect.top = y * squareHeight;
					rect.bottom = rect.top + squareHeight;
					canvas.drawRect(rect, paint);
				}
			}
		}
		if (state == DRAGGING || state == CLICK_CLICK) {
			paint.setColor(Color.argb(100, 255, 255, 255));
			rect.left = flip(destFile) * squareWidth;
			rect.right = rect.left + squareWidth;
			rect.top = 0;
			rect.bottom = 8 * squareHeight;
			canvas.drawRect(rect, paint);
			rect.left = 0;
			rect.right = 8 * squareWidth;
			rect.top = flip(destRank) * squareHeight;
			rect.bottom = rect.top + squareHeight;
			canvas.drawRect(rect, paint);
		}
		if (state == INITIAL || state == DRAGGING || state == CLICK || state == CLICK_CLICK) {
			paint.setColor(Color.argb(150, 255, 255, 255));
			rect.left = flip(initFile) * squareWidth;
			rect.right = rect.left + squareWidth;
			rect.top = flip(initRank) * squareHeight;
			rect.bottom = rect.top + squareHeight;
			canvas.drawRect(rect, paint);
		}
		if (position != null) {
			String move = position.getVerboseMove();
			if ("o-o".equals(move) || "o-o-o".equals(move)) {
				int initFile = flip(4);
				int initRank = flip(position.getToMove() == pl.mg6.yafi.model.data.Color.WHITE ? 0 : 7);
				int destFile = flip("o-o".equals(move) ? 6 : 2);
				int destRank = initRank;
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(squareWidth / 20.0f);
				paint.setColor(0x66FF0000);
				paint.setStrokeCap(Paint.Cap.ROUND);
				canvas.drawLine((initFile + 0.5f) * squareWidth, (initRank + 0.5f) * squareHeight,
						(destFile + 0.5f) * squareWidth, (destRank + 0.5f) * squareHeight, paint);
				paint.setStyle(Paint.Style.FILL);
			} else if (!"none".equals(move) && move.charAt(2) != '@') {
				int initFile = flip(move.charAt(2) - 'a');
				int initRank = flip('8' - move.charAt(3));
				int destFile = flip(move.charAt(5) - 'a');
				int destRank = flip('8' - move.charAt(6));
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(squareWidth / 20.0f);
				paint.setColor(0x66FF0000);
				paint.setStrokeCap(Paint.Cap.ROUND);
				canvas.drawLine((initFile + 0.5f) * squareWidth, (initRank + 0.5f) * squareHeight,
						(destFile + 0.5f) * squareWidth, (destRank + 0.5f) * squareHeight, paint);
				paint.setStyle(Paint.Style.FILL);
			}
			if (state == MOVE_SENT) {
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(squareWidth / 20.0f);
				paint.setColor(0x660000FF);
				paint.setStrokeCap(Paint.Cap.ROUND);
				canvas.drawLine((flip(initFile) + 0.5f) * squareWidth, (flip(initRank) + 0.5f) * squareHeight,
						(flip(destFile) + 0.5f) * squareWidth, (flip(destRank) + 0.5f) * squareHeight, paint);
				paint.setStyle(Paint.Style.FILL);
			}
			paint.setAlpha(255);
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					if (state != NONE && flip(initFile) == x && flip(initRank) == y) {
						continue;
					}
					char piece = position.getPieceAt(flip(x), flip(y));
					if (piece != '-') {
						rect.left = x * squareWidth;
						rect.right = rect.left + squareWidth;
						rect.top = y * squareHeight;
						rect.bottom = rect.top + squareHeight;
						if (bitmaps != null) {
							Bitmap bitmap = bitmaps.get(piece);
							canvas.drawBitmap(bitmap, rect.left, rect.top, paint);
						} else {
							Picture picture = pictures.get(piece);
							canvas.drawPicture(picture, rect);
						}
					}
				}
			}
		}
		if (state != NONE) {
			char piece = position.getPieceAt(initFile, initRank);
			if (piece != '-') {
				if (bitmaps != null) {
					if (state == INITIAL) {
						rect.left = flip(initFile) * squareWidth;
						rect.top = flip(initRank) * squareHeight;
					} else if (state == DRAGGING) {
						rect.left = draggingX - 0.5f * squareWidth;
						rect.top = draggingY - 0.5f * squareHeight;
					} else if (state == CLICK) {
						rect.left = flip(initFile) * squareWidth;
						rect.top = flip(initRank) * squareHeight;
					} else if (state == CLICK_CLICK || state == MOVE_SENT) {
						rect.left = flip(destFile) * squareWidth;
						rect.top = flip(destRank) * squareHeight;
					}
					Bitmap bitmap = bitmaps.get(piece);
					canvas.drawBitmap(bitmap, rect.left, rect.top, paint);
				} else {
					if (state == INITIAL) {
						rect.left = (flip(initFile) - 0.5f) * squareWidth;
						rect.right = rect.left + 2.0f * squareWidth;
						rect.top = (flip(initRank) - 0.5f) * squareHeight;
						rect.bottom = rect.top + 2.0f * squareHeight;
					} else if (state == DRAGGING) {
						rect.left = draggingX - squareWidth;
						rect.right = draggingX + squareWidth;
						rect.top = draggingY - squareHeight;
						rect.bottom = draggingY + squareHeight;
					} else if (state == CLICK) {
						rect.left = (flip(initFile) - 0.25f) * squareWidth;
						rect.right = rect.left + 1.5f * squareWidth;
						rect.top = (flip(initRank) - 0.25f) * squareHeight;
						rect.bottom = rect.top + 1.5f * squareHeight;
					} else if (state == CLICK_CLICK || state == MOVE_SENT) {
						rect.left = (flip(destFile) - 0.25f) * squareWidth;
						rect.right = rect.left + 1.5f * squareWidth;
						rect.top = (flip(destRank) - 0.25f) * squareHeight;
						rect.bottom = rect.top + 1.5f * squareHeight;
					}
					Picture picture = pictures.get(piece);
					canvas.drawPicture(picture, rect);
				}
			} else {
				Log.w(TAG, "empty: " + initFile + " " + initRank);
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		//Log.i(TAG, "onMeasure " + widthMeasureSpec + " " + heightMeasureSpec + " " + width + " " + height);
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (position != null && (position.getRelation() > 0 || premove && position.getRelation() == -1)) {
			int action = event.getAction();
			int file = flip((int) (event.getX() * 8.0f / getWidth()));
			int rank = flip((int) (event.getY() * 8.0f / getHeight()));
			if (action == MotionEvent.ACTION_DOWN) {
				char piece = position.getPieceAt(file, rank);
				if (state == NONE || state == MOVE_SENT || state == CLICK && (file != initFile || rank != initRank) && isSamePieceColorAlreadySelected(piece) && position.getRelation() > 0) {
					if (piece != '-') {
						//TODO: handle clicking only own pieces
						state = INITIAL;
						initFile = file;
						initRank = rank;
						invalidate();
						return true;
					}
				} else if (state == CLICK) {
					if (initFile == file && initRank == rank) {
						state = NONE;
					} else {
						state = CLICK_CLICK;
						destFile = file;
						destRank = rank;
					}
					invalidate();
					return true;
				}
			} else if (action == MotionEvent.ACTION_MOVE) {
				if (state == INITIAL && file == initFile && rank == initRank) {
					return true;
				} else if (state == INITIAL || state == DRAGGING) {
					if ((inputMethod & Settings.BOARD_INPUT_METHOD_DRAG_AND_DROP) != 0) {
						state = DRAGGING;
						destFile = file;
						destRank = rank;
						draggingX = event.getX();
						draggingY = event.getY();
					} else {
						state = NONE;
						event.setAction(MotionEvent.ACTION_CANCEL);
					}
				} else if (state == CLICK_CLICK) {
					if (file != destFile || rank != destRank) {
						state = NONE;
						event.setAction(MotionEvent.ACTION_CANCEL);
					}
				}
				invalidate();
				return true;
			} else if (action == MotionEvent.ACTION_UP) {
				if (state == INITIAL) {
					if ((inputMethod & Settings.BOARD_INPUT_METHOD_CLICK_CLICK) != 0) {
						state = CLICK;
					} else {
						state = NONE;
					}
				} else if (state == DRAGGING || state == CLICK_CLICK) {
					if ((destFile != initFile || destRank != initRank) && 0 <= destFile && destFile < 8 && 0 <= destRank && destRank < 8) {
						state = MOVE_SENT;
						notifyMove(initFile, initRank, destFile, destRank);
					} else {
						state = NONE;
					}
				}
				invalidate();
				return true;
			}
		}
		return false;
	}
	
	private boolean isSamePieceColorAlreadySelected(char piece) {
		String white = "pnbrqk";
		String black = "PNBRQK";
		char prevClickedPiece = position.getPieceAt(initFile, initRank);
		return white.indexOf(piece) >= 0 && white.indexOf(prevClickedPiece) >= 0
				|| black.indexOf(piece) >= 0 && black.indexOf(prevClickedPiece) >= 0;
	}
	
	public void setOnMoveListener(OnMoveListener l) {
		this.listener = l;
	}
	
	private void notifyMove(int initFile, int initRank, int destFile, int destRank) {
		if (listener != null) {
			listener.onMove(initFile, initRank, destFile, destRank);
		}
	}
	
	public void setPosition(Position pos) {
		if (state == MOVE_SENT || pos.getPieceAt(initFile, initRank) == '-') {
			state = NONE;
		}
		this.position = pos;
		invalidate();
	}
	
	public void setFlip(boolean flip) {
		this.flip = flip;
		invalidate();
	}
	
	private int flip(int a) {
		return flip ? 7 - a : a;
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable parcelable = super.onSaveInstanceState();
		Bundle bundle = new Bundle();
		bundle.putParcelable("super", parcelable);
		int state;
		if (this.state == MOVE_SENT) {
			state = MOVE_SENT;
		} else if (this.state >= CLICK) {
			state = CLICK;
		} else {
			state = NONE;
		}
		bundle.putInt("state", state);
		bundle.putInt("initFile", initFile);
		bundle.putInt("initRank", initRank);
		bundle.putInt("destFile", destFile);
		bundle.putInt("destRank", destRank);
		return bundle;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable("super"));
		this.state = bundle.getInt("state");
		this.initFile = bundle.getInt("initFile");
		this.initRank = bundle.getInt("initRank");
		this.destFile = bundle.getInt("destFile");
		this.destRank = bundle.getInt("destRank");
	}
	
	public interface OnMoveListener {
		
		void onMove(int initFile, int initRank, int destFile, int destRank);
		
		void onUnsupportedDrawPicture();
	}
}
