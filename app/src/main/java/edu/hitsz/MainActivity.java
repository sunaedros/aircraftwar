package edu.hitsz;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.game.GameDifficulty;
import edu.hitsz.game.GameSurfaceView;

public class MainActivity extends AppCompatActivity {

    private GameSurfaceView gameSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameDifficulty difficulty = GameDifficulty.fromValue(
                getIntent().getIntExtra(GameDifficulty.EXTRA_KEY, GameDifficulty.NORMAL.getValue())
        );
        gameSurfaceView = new GameSurfaceView(this, difficulty);
        setContentView(gameSurfaceView);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurfaceView.resume();
    }
}
