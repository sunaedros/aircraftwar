package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

import edu.hitsz.application.music.MusicManager;
import edu.hitsz.game.GameDifficulty;
import edu.hitsz.game.GameSurfaceView;

public class MainActivity extends AppCompatActivity {

    private GameSurfaceView gameSurfaceView;
    private boolean leaderboardOpened = false;
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
    }

    private boolean handleGameEvent(Message message) {
        if (message.what != GameSurfaceView.MSG_GAME_OVER || leaderboardOpened) {
            return false;
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
}
