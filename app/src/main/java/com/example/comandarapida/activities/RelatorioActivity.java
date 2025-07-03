package com.example.comandarapida.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comandarapida.R;
import com.example.comandarapida.database.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RelatorioActivity extends AppCompatActivity {

    private Button btnDataInicio, btnDataFim, btnBuscar;
    private TextView txtResultado;
    private Calendar dataInicio = Calendar.getInstance();
    private Calendar dataFim = Calendar.getInstance();
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("Relatório de Vendas");

        dbHelper = new DBHelper(this);

        btnDataInicio = findViewById(R.id.btnDataInicio);
        btnDataFim = findViewById(R.id.btnDataFim);
        btnBuscar = findViewById(R.id.btnBuscar);
        txtResultado = findViewById(R.id.txtResultado);

        // Inicializar com a data de hoje
        Calendar hoje = Calendar.getInstance();
        dataInicio = hoje;
        dataFim = (Calendar) hoje.clone(); // criar uma cópia para dataFim

        atualizarTextoBotaoData(btnDataInicio, dataInicio);
        atualizarTextoBotaoData(btnDataFim, dataFim);

        btnDataInicio.setOnClickListener(v -> selecionarData(dataInicio, btnDataInicio));
        btnDataFim.setOnClickListener(v -> selecionarData(dataFim, btnDataFim));
        btnBuscar.setOnClickListener(v -> buscarTotais());
    }

    private void atualizarTextoBotaoData(Button botao, Calendar data) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        botao.setText(sdf.format(data.getTime()));
    }

    private void selecionarData(Calendar data, Button btn) {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    data.set(year, month, dayOfMonth);
                    btn.setText(DateFormat.format("dd/MM/yyyy", data));
                },
                data.get(Calendar.YEAR),
                data.get(Calendar.MONTH),
                data.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @SuppressLint("DefaultLocale")
    private void buscarTotais() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String inicio = sdf.format(dataInicio.getTime()) + " 00:00:00";
        String fim = sdf.format(dataFim.getTime()) + " 23:59:59";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT quantidade, preco FROM itens_fechados WHERE data_hora BETWEEN ? AND ?",
                new String[]{inicio, fim});

        int totalQtd = 0;
        double totalValor = 0.0;
        while (cursor.moveToNext()) {
            int qtd = cursor.getInt(0);
            double preco = cursor.getDouble(1);
            totalQtd += qtd;
            totalValor += qtd * preco;
        }
        cursor.close();

        txtResultado.setText(String.format("Fechamento do período:\n%d itens vendidos\nTotal R$ %.2f", totalQtd, totalValor));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

