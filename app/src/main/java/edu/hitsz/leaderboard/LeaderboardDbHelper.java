package edu.hitsz.leaderboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "aircraft_war.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "leaderboard";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PLAYER_NAME = "player_name";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_CREATED_AT = "created_at";

    public LeaderboardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PLAYER_NAME + " TEXT NOT NULL, "
                + COLUMN_SCORE + " INTEGER NOT NULL, "
                + COLUMN_DIFFICULTY + " INTEGER NOT NULL, "
                + COLUMN_CREATED_AT + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertRecord(String playerName, int score, int difficulty, String createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_NAME, playerName);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_DIFFICULTY, difficulty);
        values.put(COLUMN_CREATED_AT, createdAt);
        return db.insert(TABLE_NAME, null, values);
    }

    public List<LeaderboardEntry> queryByDifficulty(int difficulty) {
        List<LeaderboardEntry> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COLUMN_DIFFICULTY + "=?",
                new String[]{String.valueOf(difficulty)},
                null,
                null,
                COLUMN_SCORE + " DESC, " + COLUMN_ID + " ASC"
        );
        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String playerName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
                results.add(new LeaderboardEntry(id, playerName, score, difficulty, createdAt));
            }
        } finally {
            cursor.close();
        }
        return results;
    }

    public void deleteRecord(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
