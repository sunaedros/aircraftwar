package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

import edu.hitsz.application.music.MusicManager;
import edu.hitsz.game.GameDifficulty;
import edu.hitsz.game.GameSurfaceView;
import edu.hitsz.online.OnlineBattleClient;
import edu.hitsz.online.OnlineBattleConfig;
import edu.hitsz.online.OnlineBattleState;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ONLINE_MODE = "edu.hitsz.extra.ONLINE_MODE";

    private GameSurfaceView gameSurfaceView;
    private boolean leaderboardOpened = false;
    private boolean onlineMode = false;
    private boolean onlineResultOpened = false;
    private int onlinePlayerId = 0;
    private volatile OnlineBattleClient onlineBattleClient;
    private final Handler gameEventHandler = new Handler(Looper.getMainLooper(), this::handleGameEvent);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GameDifficulty difficulty = GameDifficulty.fromValue(
                getIntent().getIntExtra(GameDifficulty.EXTRA_KEY, GameDifficulty.NORMAL.getValue())
        );
        MusicManager.initialize(getApplicationContext());
        gameSurfaceView = new GameSurfaceView(this, difficulty, gameEventHandler);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.addView(gameSurfaceView);
        onlineMode = getIntent().getBooleanExtra(EXTRA_ONLINE_MODE, false);
        if (onlineMode) {
            setupOnlineBattle();
        }
        MaterialSwitch musicSwitch = findViewById(R.id.music_switch);
        musicSwitch.setChecked(MusicManager.isAudioEnabled());
        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> MusicManager.setAudioEnabled(isChecked));

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurfaceView.pause();
        MusicManager.onHostPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.onHostResume();
        gameSurfaceView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            MusicManager.release();
        }
        if (onlineBattleClient != null) {
            onlineBattleClient.close();
        }
    }

    private boolean handleGameEvent(Message message) {
        if (message.what != GameSurfaceView.MSG_GAME_OVER || leaderboardOpened) {
            return false;
        }
        if (onlineMode) {
            leaderboardOpened = true;
            return true;
        }
        leaderboardOpened = true;
        Intent intent = new Intent(this, LeaderboardActivity.class);
        intent.putExtra(LeaderboardActivity.EXTRA_SCORE, message.getData().getInt(GameSurfaceView.KEY_SCORE));
        intent.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY,
                message.getData().getInt(GameSurfaceView.KEY_DIFFICULTY));
        startActivity(intent);
        finish();
        return true;
    }

    private void setupOnlineBattle() {
        gameSurfaceView.setOnlineWaiting();
        gameSurfaceView.setOnlineScoreObserver(new GameSurfaceView.OnlineScoreObserver() {
            @Override
            public void onScoreChanged(int score) {
                if (onlineBattleClient != null) {
                    onlineBattleClient.sendScore(score);
                }
            }

            @Override
            public void onHeroDead(int finalScore) {
                if (onlineBattleClient != null) {
                    onlineBattleClient.sendDead(finalScore);
                }
            }
        });

        String host = getIntent().getStringExtra(OnlineBattleConfig.EXTRA_HOST);
        if (host == null || host.trim().isEmpty()) {
            host = OnlineBattleConfig.DEFAULT_HOST;
        }
        int port = getIntent().getIntExtra(OnlineBattleConfig.EXTRA_PORT, OnlineBattleConfig.DEFAULT_PORT);
        String playerName = getIntent().getStringExtra(OnlineBattleConfig.EXTRA_PLAYER_NAME);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        onlineBattleClient = new OnlineBattleClient(host, port, playerName, new OnlineBattleClient.Listener() {
            @Override
            public void onWaiting() {
                gameSurfaceView.setOnlineWaiting();
                Toast.makeText(MainActivity.this, "Waiting for opponent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStarted(int playerId, String selfName, String opponentName) {
                onlinePlayerId = playerId;
                gameSurfaceView.setOnlineStarted(opponentName);
                Toast.makeText(MainActivity.this, "Online battle started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onState(OnlineBattleState state) {
                updateOpponentState(state);
            }

            @Override
            public void onGameOver(OnlineBattleState state) {
                openOnlineResult(state);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        onlineBattleClient.connect();
    }

    private void updateOpponentState(OnlineBattleState state) {
        if (onlinePlayerId == 1) {
            gameSurfaceView.setOpponentState(state.getPlayerTwoName(), state.getPlayerTwoScore(), state.isPlayerTwoAlive());
        } else {
            gameSurfaceView.setOpponentState(state.getPlayerOneName(), state.getPlayerOneScore(), state.isPlayerOneAlive());
        }
    }

    private void openOnlineResult(OnlineBattleState state) {
        if (onlineResultOpened) {
            return;
        }
        onlineResultOpened = true;
        Intent intent = new Intent(this, OnlineBattleResultActivity.class);
        intent.putExtra(OnlineBattleResultActivity.EXTRA_P1_NAME, state.getPlayerOneName());
        intent.putExtra(OnlineBattleResultActivity.EXTRA_P1_SCORE, state.getPlayerOneScore());
        intent.putExtra(OnlineBattleResultActivity.EXTRA_P2_NAME, state.getPlayerTwoName());
        intent.putExtra(OnlineBattleResultActivity.EXTRA_P2_SCORE, state.getPlayerTwoScore());
        startActivity(intent);
        finish();
    }
}
