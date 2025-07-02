package com.example.comandarapida.activities;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.comandarapida.R;
import com.example.comandarapida.database.DBHelper;

public class CadastroItemActivity extends AppCompatActivity {

    EditText edtDescricao, edtPreco;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_item);

        edtDescricao = findViewById(R.id.edtDescricaoItem);
        edtPreco = findViewById(R.id.edtPrecoItem);
        Button btnSalvar = findViewById(R.id.btnSalvarItem);

        dbHelper = new DBHelper(this);

        btnSalvar.setOnClickListener(v -> {
            String descricao = edtDescricao.getText().toString().trim();
            String precoStr = edtPreco.getText().toString().trim();

            if (descricao.isEmpty() || precoStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double preco = Double.parseDouble(precoStr);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("descricao", descricao);
            values.put("preco", preco);
            db.insert("catalogo_itens", null, values);

            Toast.makeText(this, "Item salvo!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
