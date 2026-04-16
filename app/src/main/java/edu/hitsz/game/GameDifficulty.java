package edu.hitsz.game;

public enum GameDifficulty {
    EASY("Easy", 1, 4, 0.20, 700, 6, 10, 24, 80),
    NORMAL("Normal", 2, 5, 0.35, 600, 8, 14, 30, 90),
    HARD("Hard", 3, 6, 0.50, 500, 10, 16, 36, 110);

    public static final String EXTRA_KEY = "edu.hitsz.extra.GAME_DIFFICULTY";

    private final String label;
    private final int value;
    private final int enemyMaxNumber;
    private final double eliteProbability;
    private final int cycleDuration;
    private final int minEnemySpeed;
    private final int maxEnemySpeed;
    private final int mobEnemyHp;
    private final int eliteEnemyHp;

    GameDifficulty(String label, int value, int enemyMaxNumber, double eliteProbability, int cycleDuration,
                   int minEnemySpeed, int maxEnemySpeed, int mobEnemyHp, int eliteEnemyHp) {
        this.label = label;
        this.value = value;
        this.enemyMaxNumber = enemyMaxNumber;
        this.eliteProbability = eliteProbability;
        this.cycleDuration = cycleDuration;
        this.minEnemySpeed = minEnemySpeed;
        this.maxEnemySpeed = maxEnemySpeed;
        this.mobEnemyHp = mobEnemyHp;
        this.eliteEnemyHp = eliteEnemyHp;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    public int getEnemyMaxNumber() {
        return enemyMaxNumber;
    }

    public double getEliteProbability() {
        return eliteProbability;
    }

    public int getCycleDuration() {
        return cycleDuration;
    }

    public int getMinEnemySpeed() {
        return minEnemySpeed;
    }

    public int getMaxEnemySpeed() {
        return maxEnemySpeed;
    }

    public int getMobEnemyHp() {
        return mobEnemyHp;
    }

    public int getEliteEnemyHp() {
        return eliteEnemyHp;
    }

    public static GameDifficulty fromValue(int value) {
        for (GameDifficulty difficulty : values()) {
            if (difficulty.value == value) {
                return difficulty;
            }
        }
        return NORMAL;
    }
}
