package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.game.GameDifficulty;
import edu.hitsz.online.OnlineBattleConfig;

public class OnlineConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_config);

        EditText nameInput = findViewById(R.id.name_input);
        EditText hostInput = findViewById(R.id.host_input);
        EditText portInput = findViewById(R.id.port_input);

        findViewById(R.id.start_online_button).setOnClickListener(v -> {
            String name = valueOrDefault(nameInput, "Player");
            String host = valueOrDefault(hostInput, OnlineBattleConfig.DEFAULT_HOST);
            int port = parsePort(portInput.getText().toString());
            if (port <= 0) {
                Toast.makeText(this, "Invalid server port", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_ONLINE_MODE, true);
            intent.putExtra(OnlineBattleConfig.EXTRA_PLAYER_NAME, name);
            intent.putExtra(OnlineBattleConfig.EXTRA_HOST, host);
            intent.putExtra(OnlineBattleConfig.EXTRA_PORT, port);
            intent.putExtra(GameDifficulty.EXTRA_KEY, GameDifficulty.NORMAL.getValue());
            startActivity(intent);
        });
    }

    private static String valueOrDefault(EditText editText, String fallback) {
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private static int parsePort(String rawPort) {
        try {
            int port = Integer.parseInt(rawPort.trim());
            return port > 0 && port <= 65535 ? port : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
