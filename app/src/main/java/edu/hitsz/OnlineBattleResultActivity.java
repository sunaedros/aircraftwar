package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.online.OnlineBattleResult;

public class OnlineBattleResultActivity extends AppCompatActivity {

    public static final String EXTRA_P1_NAME = "edu.hitsz.extra.ONLINE_P1_NAME";
    public static final String EXTRA_P1_SCORE = "edu.hitsz.extra.ONLINE_P1_SCORE";
    public static final String EXTRA_P2_NAME = "edu.hitsz.extra.ONLINE_P2_NAME";
    public static final String EXTRA_P2_SCORE = "edu.hitsz.extra.ONLINE_P2_SCORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_result);

        String p1Name = getIntent().getStringExtra(EXTRA_P1_NAME);
        String p2Name = getIntent().getStringExtra(EXTRA_P2_NAME);
        int p1Score = getIntent().getIntExtra(EXTRA_P1_SCORE, 0);
        int p2Score = getIntent().getIntExtra(EXTRA_P2_SCORE, 0);

        TextView winnerText = findViewById(R.id.winner_text);
        TextView playerOneText = findViewById(R.id.player_one_text);
        TextView playerTwoText = findViewById(R.id.player_two_text);

        winnerText.setText(resolveWinner(p1Name, p1Score, p2Name, p2Score));
        playerOneText.setText(OnlineBattleResult.formatScore(p1Name, p1Score));
        playerTwoText.setText(OnlineBattleResult.formatScore(p2Name, p2Score));
        findViewById(R.id.back_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private static String resolveWinner(String p1Name, int p1Score, String p2Name, int p2Score) {
        if (p1Score == p2Score) {
            return "Draw";
        }
        return (p1Score > p2Score ? p1Name : p2Name) + " Wins";
    }
}
