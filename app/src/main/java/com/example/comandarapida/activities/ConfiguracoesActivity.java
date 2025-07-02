package com.example.comandarapida.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.comandarapida.R;

public class ConfiguracoesActivity extends AppCompatActivity {

    EditText edtNovaSenha;
    Button btnSalvarSenha;
    Switch switchTema;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences("comanda_config", MODE_PRIVATE);

        edtNovaSenha = findViewById(R.id.edtNovaSenha);
        btnSalvarSenha = findViewById(R.id.btnSalvarSenha);
        switchTema = findViewById(R.id.switchTema);

        // carregar tema atual
        boolean temaEscuro = prefs.getBoolean("modo_escuro", false);
        switchTema.setChecked(temaEscuro);

        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("modo_escuro", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES :
                    AppCompatDelegate.MODE_NIGHT_NO);
        });

        btnSalvarSenha.setOnClickListener(v -> {
            String novaSenha = edtNovaSenha.getText().toString().trim();
            if (novaSenha.isEmpty()) {
                Toast.makeText(this, "Digite uma nova senha", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putString("senha_zerar", novaSenha).apply();
                Toast.makeText(this, "Senha alterada!", Toast.LENGTH_SHORT).show();
                edtNovaSenha.setText("");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
