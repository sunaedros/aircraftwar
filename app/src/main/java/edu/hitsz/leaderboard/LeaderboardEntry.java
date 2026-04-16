package edu.hitsz.leaderboard;

public class LeaderboardEntry {

    private final long id;
    private final String playerName;
    private final int score;
    private final int difficulty;
    private final String createdAt;

    public LeaderboardEntry(long id, String playerName, int score, int difficulty, String createdAt) {
        this.id = id;
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
