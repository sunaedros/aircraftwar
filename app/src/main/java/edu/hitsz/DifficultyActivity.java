package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.game.GameDifficulty;

public class DifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        wireDifficultyButton(R.id.button_easy, GameDifficulty.EASY);
        wireDifficultyButton(R.id.button_normal, GameDifficulty.NORMAL);
        wireDifficultyButton(R.id.button_hard, GameDifficulty.HARD);
        findViewById(R.id.button_online).setOnClickListener(v ->
                startActivity(new Intent(this, OnlineConfigActivity.class)));
    }

    private void wireDifficultyButton(int buttonId, GameDifficulty difficulty) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(GameDifficulty.EXTRA_KEY, difficulty.getValue());
            startActivity(intent);
        });
    }
}
