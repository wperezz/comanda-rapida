package com.example.comandarapida.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comandarapida.R;
import com.example.comandarapida.database.DBHelper;

public class HistoricoActivity extends AppCompatActivity {

    TextView txtHistorico;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtHistorico = findViewById(R.id.txtHistorico);
        dbHelper = new DBHelper(this);

        carregarHistorico();
    }

    private void carregarHistorico() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cliente_nome, descricao, quantidade, preco, data_hora FROM itens_fechados ORDER BY data_hora DESC", null);

        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()) {
            String nome = cursor.getString(0);
            String desc = cursor.getString(1);
            int qtd = cursor.getInt(2);
            double preco = cursor.getDouble(3);
            long millis = cursor.getLong(4);
            builder.append(String.format("Cliente: %s\n", nome));
            builder.append(String.format("Item: %s (%d x R$ %.2f)\n", desc, qtd, preco));
            builder.append(String.format("Data: %s\n\n", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(millis))));
        }
        cursor.close();

        txtHistorico.setText(builder.toString().isEmpty() ? "Nenhum histórico encontrado." : builder.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
