package edu.hitsz.leaderboard;

import android.content.Context;

import java.util.List;

public class LeaderboardRepository {

    private final LeaderboardDbHelper dbHelper;

    public LeaderboardRepository(Context context) {
        this.dbHelper = new LeaderboardDbHelper(context.getApplicationContext());
    }

    public void saveScore(String playerName, int score, int difficulty) {
        dbHelper.insertRecord(playerName, score, difficulty, DateTimeUtil.now());
    }

    public List<LeaderboardEntry> loadByDifficulty(int difficulty) {
        return dbHelper.queryByDifficulty(difficulty);
    }

    public void deleteRecord(long id) {
        dbHelper.deleteRecord(id);
    }
}
