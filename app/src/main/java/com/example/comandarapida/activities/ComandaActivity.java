package com.example.comandarapida.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comandarapida.R;
import com.example.comandarapida.adapters.ItemAdapter;
import com.example.comandarapida.database.DBHelper;
import com.example.comandarapida.models.Item;

import java.util.ArrayList;
import java.util.List;

public class ComandaActivity extends AppCompatActivity {

    DBHelper dbHelper;
    List<Item> itens = new ArrayList<>();
    ItemAdapter adapter;
    int clienteId;
    String clienteNome;

    TextView txtTotal;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comanda);

        dbHelper = new DBHelper(this);
        clienteId = getIntent().getIntExtra("cliente_id", -1);
        clienteNome = getIntent().getStringExtra("cliente_nome");

        TextView txtNomeCliente = findViewById(R.id.txtNomeCliente);
        txtNomeCliente.setText("Cliente: " + clienteNome);

        txtTotal = findViewById(R.id.txtTotal);

        RecyclerView recycler = findViewById(R.id.recyclerItens);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(itens);
        recycler.setAdapter(adapter);

        carregarItens();

        findViewById(R.id.btnAdicionarItem).setOnClickListener(v -> mostrarDialogoNovoItem());
        findViewById(R.id.btnFinalizar).setOnClickListener(v -> finalizarConta());
    }

    private void carregarItens() {
        itens.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM itens WHERE cliente_id = ?", new String[]{String.valueOf(clienteId)});
        while (cursor.moveToNext()) {
            itens.add(new Item(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getDouble(4)
            ));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        calcularTotal();
    }

    private void calcularTotal() {
        double total = 0;
        for (Item item : itens) {
            total += item.getTotal();
        }
        txtTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void mostrarDialogoNovoItem() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText edtDescricao = new EditText(this);
        edtDescricao.setHint("Descrição");
        layout.addView(edtDescricao);

        EditText edtQtd = new EditText(this);
        edtQtd.setHint("Quantidade");
        edtQtd.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(edtQtd);

        EditText edtPreco = new EditText(this);
        edtPreco.setHint("Preço");
        edtPreco.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        layout.addView(edtPreco);

        new AlertDialog.Builder(this)
                .setTitle("Novo Item")
                .setView(layout)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String descricao = edtDescricao.getText().toString().trim();
                    int quantidade = Integer.parseInt(edtQtd.getText().toString().trim());
                    double preco = Double.parseDouble(edtPreco.getText().toString().trim());

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("cliente_id", clienteId);
                    values.put("descricao", descricao);
                    values.put("quantidade", quantidade);
                    values.put("preco", preco);
                    db.insert("itens", null, values);

                    carregarItens();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void finalizarConta() {
        new AlertDialog.Builder(this)
                .setTitle("Finalizar Conta")
                .setMessage("Deseja finalizar e apagar os itens deste cliente?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("itens", "cliente_id = ?", new String[]{String.valueOf(clienteId)});
                    carregarItens();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}
