package edu.hitsz.online;

public final class OnlineBattleState {
    private final String playerOneName;
    private final int playerOneScore;
    private final boolean playerOneAlive;
    private final String playerTwoName;
    private final int playerTwoScore;
    private final boolean playerTwoAlive;

    public OnlineBattleState(String playerOneName, int playerOneScore, boolean playerOneAlive,
                             String playerTwoName, int playerTwoScore, boolean playerTwoAlive) {
        this.playerOneName = playerOneName;
        this.playerOneScore = playerOneScore;
        this.playerOneAlive = playerOneAlive;
        this.playerTwoName = playerTwoName;
        this.playerTwoScore = playerTwoScore;
        this.playerTwoAlive = playerTwoAlive;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public int getPlayerOneScore() {
        return playerOneScore;
    }

    public boolean isPlayerOneAlive() {
        return playerOneAlive;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public int getPlayerTwoScore() {
        return playerTwoScore;
    }

    public boolean isPlayerTwoAlive() {
        return playerTwoAlive;
    }
}
