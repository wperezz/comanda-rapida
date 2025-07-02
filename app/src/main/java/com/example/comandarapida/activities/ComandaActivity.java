package com.example.comandarapida.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comandarapida.R;
import com.example.comandarapida.adapters.ItemAdapter;
import com.example.comandarapida.database.DBHelper;
import com.example.comandarapida.models.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComandaActivity extends AppCompatActivity {

    DBHelper dbHelper;
    List<Item> itens = new ArrayList<>();
    ItemAdapter adapter;
    int clienteId;
    String clienteNome;

    TextView txtTotal;
    TextView txtResumo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comanda);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        dbHelper = new DBHelper(this);
        clienteId = getIntent().getIntExtra("cliente_id", -1);
        clienteNome = getIntent().getStringExtra("cliente_nome");

        txtResumo = findViewById(R.id.txtResumo);

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!itens.isEmpty()) {
                    new AlertDialog.Builder(ComandaActivity.this)
                            .setTitle("Sair sem finalizar?")
                            .setMessage("Esta comanda ainda possui itens.\nTem certeza que deseja sair sem finalizar?")
                            .setPositiveButton("Sim, sair", (dialog, which) -> finish())
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    finish();
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
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

    @SuppressLint("DefaultLocale")
    private void calcularTotal() {
        double total = 0;
        int totalItens = 0;

        for (Item item : itens) {
            total += item.getTotal();
            totalItens += item.quantidade;
        }
        txtTotal.setText(String.format("Total: R$ %.2f", total));
        String resumo = String.format("Total de itens: %d", totalItens);

        txtResumo.setText(resumo);
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
                .setMessage("Deseja finalizar e salvar os itens desta comanda?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // Buscar os itens da comanda atual
                    Cursor cursor = db.rawQuery("SELECT descricao, quantidade, preco FROM itens WHERE cliente_id = ?", new String[]{String.valueOf(clienteId)});
                    while (cursor.moveToNext()) {
                        String desc = cursor.getString(0);
                        int qtd = cursor.getInt(1);
                        double preco = cursor.getDouble(2);

                        ContentValues v = new ContentValues();
                        v.put("cliente_nome", clienteNome);
                        v.put("descricao", desc);
                        v.put("quantidade", qtd);
                        v.put("preco", preco);
                        v.put("data_hora", String.valueOf(System.currentTimeMillis()));
                        db.insert("itens_fechados", null, v);
                    }
                    cursor.close();

                    // Apagar da tabela principal
                    db.delete("itens", "cliente_id = ?", new String[]{String.valueOf(clienteId)});
                    carregarItens();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

}
