package edu.hitsz.online;

public final class OnlineBattleResult {
    private OnlineBattleResult() {
    }

    public static String formatScore(String name, int score) {
        String displayName = name == null || name.trim().isEmpty() ? "Player" : name;
        return displayName + ": " + score;
    }
}
