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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ComandaActivity extends AppCompatActivity {

    DBHelper dbHelper;
    List<Item> itens = new ArrayList<>();
    ItemAdapter adapter;
    int clienteId;
    String clienteNome;

    TextView txtResumoComanda;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comanda);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        dbHelper = new DBHelper(this);
        clienteId = getIntent().getIntExtra("cliente_id", -1);
        clienteNome = getIntent().getStringExtra("cliente_nome");

        TextView txtNomeCliente = findViewById(R.id.txtNomeCliente);
        txtNomeCliente.setText("Cliente: " + clienteNome);

        RecyclerView recycler = findViewById(R.id.recyclerItens);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(itens,
                v -> {}, // clique normal não faz nada por enquanto
                v -> {
                    Item item = (Item) v.getTag();
                    mostrarDialogoEditarOuExcluir(item);
                    return true;
                });
        recycler.setAdapter(adapter);

        atualizarResumo(); // chama após carregar a comanda

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

    private void mostrarDialogoEditarOuExcluir(Item item) {
        String[] opcoes = {"Editar quantidade", "Excluir item"};

        new AlertDialog.Builder(this)
                .setTitle("Escolha uma ação")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEditarQuantidade(item);
                    } else if (which == 1) {
                        excluirItem(item);
                    }
                })
                .show();
    }

    private void mostrarDialogoEditarQuantidade(Item item) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nova quantidade");
        input.setText(String.valueOf(item.getQuantidade()));

        new AlertDialog.Builder(this)
                .setTitle("Editar quantidade")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    int novaQtd = Integer.parseInt(input.getText().toString().trim());
                    ContentValues values = new ContentValues();
                    values.put("quantidade", novaQtd);

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.update("itens_comanda", values, "id = ?", new String[]{String.valueOf(item.id)});

                    carregarItens();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirItem(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage("Deseja realmente excluir este item?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("itens_comanda", "id = ?", new String[]{String.valueOf(item.id)});
                    carregarItens();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void atualizarResumo() {
        int totalItens = itens.size();
        int quantidadeTotal = 0;
        double valorTotal = 0.0;

        for (Item item : itens) {
            valorTotal += item.getTotal();
            quantidadeTotal += item.getQuantidade();
        }

        TextView txtQtd = findViewById(R.id.txtResumoQtd);
        TextView txtValor = findViewById(R.id.txtResumoValor);

        txtQtd.setText("Resumo: " + totalItens + " itens • " + quantidadeTotal + " und");
        txtValor.setText(String.format("R$ %.2f", valorTotal));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void carregarItens() {
        itens.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM itens_comanda WHERE cliente_id = ?", new String[]{String.valueOf(clienteId)});
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
        atualizarResumo();
    }

    @SuppressLint("DefaultLocale")
    private void mostrarDialogoNovoItem() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        Spinner spinnerItens = new Spinner(this);
        List<String> listaNomes = new ArrayList<>();
        List<Item> catalogo = new ArrayList<>();

        // Buscar do banco
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, descricao, preco FROM catalogo_itens", null);
        while (cursor.moveToNext()) {
            Item i = new Item(cursor.getInt(0), 0, cursor.getString(1), 1, cursor.getDouble(2));
            catalogo.add(i);
            listaNomes.add(i.descricao + " (R$ " + String.format("%.2f", i.preco) + ")");
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaNomes);
        spinnerItens.setAdapter(adapter);
        layout.addView(spinnerItens);

        EditText edtQtd = new EditText(this);
        edtQtd.setHint("Quantidade");
        edtQtd.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(edtQtd);

        new AlertDialog.Builder(this)
                .setTitle("Adicionar item à comanda")
                .setView(layout)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    int pos = spinnerItens.getSelectedItemPosition();
                    if (pos >= 0 && !edtQtd.getText().toString().trim().isEmpty()) {
                        Item escolhido = catalogo.get(pos);
                        int qtd = Integer.parseInt(edtQtd.getText().toString().trim());

                        ContentValues values = new ContentValues();
                        values.put("cliente_id", clienteId);
                        values.put("descricao", escolhido.descricao);
                        values.put("quantidade", qtd);
                        values.put("preco", escolhido.preco);
                        values.put("data_hora", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                        SQLiteDatabase db2 = dbHelper.getWritableDatabase();

                        // Verificar se o item já existe na comanda do cliente
                        Cursor cursorCheck = db2.rawQuery(
                                "SELECT id, quantidade FROM itens_comanda WHERE cliente_id = ? AND descricao = ? AND preco = ?",
                                new String[]{String.valueOf(clienteId), escolhido.descricao, String.valueOf(escolhido.preco)}
                        );

                        if (cursorCheck.moveToFirst()) {
                            // Item já existe, atualizar quantidade
                            int itemId = cursorCheck.getInt(0);
                            int quantidadeAtual = cursorCheck.getInt(1);
                            int novaQuantidade = quantidadeAtual + qtd;

                            ContentValues updateValues = new ContentValues();
                            updateValues.put("quantidade", novaQuantidade);

                            db2.update("itens_comanda", updateValues, "id = ?", new String[]{String.valueOf(itemId)});
                        } else {
                            db2.insert("itens_comanda", null, values);
                        }
                        cursorCheck.close();
                        carregarItens();

                    }
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
                    Cursor cursor = db.rawQuery("SELECT descricao, quantidade, preco FROM itens_comanda WHERE cliente_id = ?", new String[]{String.valueOf(clienteId)});
                    while (cursor.moveToNext()) {
                        String desc = cursor.getString(0);
                        int qtd = cursor.getInt(1);
                        double preco = cursor.getDouble(2);

                        ContentValues v = new ContentValues();
                        v.put("cliente_nome", clienteNome);
                        v.put("descricao", desc);
                        v.put("quantidade", qtd);
                        v.put("preco", preco);
                        v.put("data_hora", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                        db.insert("itens_fechados", null, v);
                    }
                    cursor.close();

                    // Apagar da tabela principal
                    db.delete("itens_comanda", "cliente_id = ?", new String[]{String.valueOf(clienteId)});
                    carregarItens();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

}
