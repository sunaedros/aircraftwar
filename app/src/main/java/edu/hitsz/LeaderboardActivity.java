package edu.hitsz;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.hitsz.game.GameDifficulty;
import edu.hitsz.leaderboard.LeaderboardAdapter;
import edu.hitsz.leaderboard.LeaderboardEntry;
import edu.hitsz.leaderboard.LeaderboardRepository;

public class LeaderboardActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "edu.hitsz.extra.LEADERBOARD_SCORE";
    public static final String EXTRA_DIFFICULTY = "edu.hitsz.extra.LEADERBOARD_DIFFICULTY";

    private LeaderboardRepository repository;
    private LeaderboardAdapter adapter;
    private GameDifficulty difficulty;
    private int pendingScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        difficulty = GameDifficulty.fromValue(
                getIntent().getIntExtra(EXTRA_DIFFICULTY, GameDifficulty.NORMAL.getValue())
        );
        pendingScore = getIntent().getIntExtra(EXTRA_SCORE, -1);
        repository = new LeaderboardRepository(this);

        TextView titleText = findViewById(R.id.leaderboard_title);
        titleText.setText(getString(R.string.leaderboard_title, difficulty.getLabel()));

        TextView emptyText = findViewById(R.id.empty_text);
        ListView listView = findViewById(R.id.leaderboard_list);
        listView.setEmptyView(emptyText);
        adapter = new LeaderboardAdapter(this, entry -> {
            repository.deleteRecord(entry.getId());
            loadLeaderboard();
        });
        listView.setAdapter(adapter);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        if (savedInstanceState == null && pendingScore >= 0) {
            promptForPlayerName();
        } else {
            loadLeaderboard();
        }
    }

    private void promptForPlayerName() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.leaderboard_name_hint);

        new AlertDialog.Builder(this)
                .setTitle(R.string.leaderboard_dialog_title)
                .setMessage(getString(R.string.leaderboard_dialog_message, pendingScore))
                .setView(input)
                .setCancelable(false)
                .setPositiveButton(R.string.leaderboard_save, (dialog, which) -> {
                    String playerName = input.getText().toString().trim();
                    if (playerName.isEmpty()) {
                        playerName = getString(R.string.leaderboard_default_name);
                    }
                    repository.saveScore(playerName, pendingScore, difficulty.getValue());
                    loadLeaderboard();
                })
                .show();
    }

    private void loadLeaderboard() {
        List<LeaderboardEntry> entries = repository.loadByDifficulty(difficulty.getValue());
        adapter.submitList(entries);
    }
}
