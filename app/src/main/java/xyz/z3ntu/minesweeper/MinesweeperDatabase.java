package xyz.z3ntu.minesweeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Written by Luca Weiss (z3ntu)
 * https://github.com/z3ntu
 */
public class MinesweeperDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "minesweeper";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "minesweeperData";
    private static final String KEY_ID = "g_id";
    private static final String KEY_TIMESTAMP = "g_timestamp";
    private static final String KEY_SHOWSTATE = "g_showstate";
    private static final String KEY_GAMESTATE = "g_gamestate";
    private static MinesweeperDatabase sInstance;

    private MinesweeperDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MinesweeperDatabase getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new MinesweeperDatabase(context.getApplicationContext());
        }
        return sInstance;
    }

    public void insertField(int id, Game.ShowState showState, Game.GameState gameState, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_TIMESTAMP, timestamp);
        values.put(KEY_SHOWSTATE, showState.getInt());
        values.put(KEY_GAMESTATE, gameState.getInt());
        db.insertOrThrow(TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public int getLastTimestamp() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_TIMESTAMP + " FROM " + TABLE_NAME + " ORDER BY " + KEY_TIMESTAMP + " DESC LIMIT 1", null);
        int lasttimestamp = -1;
        if (c.moveToFirst()) {
            lasttimestamp = c.getInt(0);
        }
        System.out.println("Lasttimestamp: " + lasttimestamp);
        c.close();
        return lasttimestamp;
    }

    public Object[] getField(int id, int timestamp) {
        // TODO: Maybe get all at once and return them nicely?
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT " + KEY_GAMESTATE + "," + KEY_SHOWSTATE + " FROM " + TABLE_NAME + " WHERE " + KEY_TIMESTAMP + "=" + timestamp + " AND " + KEY_ID + "=" + id, null);
        Game.GameState gamestate = null;
        Game.ShowState showstate = null;

        if (c.moveToFirst()) {
            gamestate = Game.GameState.gameStateFromInt(c.getInt(0));
            showstate = Game.ShowState.showStateFromInt(c.getInt(1));
        }
        Object[] returnValues = new Object[]{gamestate, showstate};

        c.close();
        return returnValues;


    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_ID + " INTEGER," +
                KEY_TIMESTAMP + " INTEGER," +
                KEY_SHOWSTATE + " INTEGER," +
                KEY_GAMESTATE + " INTEGER," +
                "PRIMARY KEY (" + KEY_ID + "," + KEY_TIMESTAMP + ")" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}
