package edu.hitsz;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

import edu.hitsz.application.music.MusicManager;
import edu.hitsz.game.GameDifficulty;
import edu.hitsz.game.GameSurfaceView;

public class MainActivity extends AppCompatActivity {

    private GameSurfaceView gameSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GameDifficulty difficulty = GameDifficulty.fromValue(
                getIntent().getIntExtra(GameDifficulty.EXTRA_KEY, GameDifficulty.NORMAL.getValue())
        );
        MusicManager.initialize(getApplicationContext());
        gameSurfaceView = new GameSurfaceView(this, difficulty);
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
}
