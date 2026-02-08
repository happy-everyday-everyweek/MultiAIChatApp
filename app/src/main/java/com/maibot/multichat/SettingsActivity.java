package com.maibot.multichat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    private EditText apiKeyInput;
    private EditText baseUrlInput;
    private EditText modelNameInput;
    private SeekBar botCountSeekBar;
    private TextView botCountText;
    private Button saveButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }

        prefs = getSharedPreferences("maibot_settings", MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        apiKeyInput = findViewById(R.id.apiKeyInput);
        baseUrlInput = findViewById(R.id.baseUrlInput);
        modelNameInput = findViewById(R.id.modelNameInput);
        botCountSeekBar = findViewById(R.id.botCountSeekBar);
        botCountText = findViewById(R.id.botCountText);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadSettings() {
        String apiKey = prefs.getString("api_key", "");
        String baseUrl = prefs.getString("base_url", "https://api.deepseek.com");
        String modelName = prefs.getString("model_name", "deepseek-chat");
        int botCount = prefs.getInt("bot_count", 3);
        
        apiKeyInput.setText(apiKey);
        baseUrlInput.setText(baseUrl);
        modelNameInput.setText(modelName);
        botCountSeekBar.setProgress(botCount - 1);
        botCountText.setText(botCount + " 个Bot");
    }

    private void setupListeners() {
        botCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int count = progress + 1;
                botCountText.setText(count + " 个Bot");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        String apiKey = apiKeyInput.getText().toString().trim();
        String baseUrl = baseUrlInput.getText().toString().trim();
        String modelName = modelNameInput.getText().toString().trim();
        int botCount = botCountSeekBar.getProgress() + 1;

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入API Key", Toast.LENGTH_SHORT).show();
            return;
        }

        if (baseUrl.isEmpty()) {
            baseUrl = "https://api.deepseek.com";
        }

        if (modelName.isEmpty()) {
            modelName = "deepseek-chat";
        }

        prefs.edit()
            .putString("api_key", apiKey)
            .putString("base_url", baseUrl)
            .putString("model_name", modelName)
            .putInt("bot_count", botCount)
            .apply();

        Toast.makeText(this, "设置已保存，请重启应用生效", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
