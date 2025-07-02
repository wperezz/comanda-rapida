package com.example.comandarapida.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comandarapida.R;
import com.example.comandarapida.adapters.ClienteAdapter;
import com.example.comandarapida.database.DBHelper;
import com.example.comandarapida.models.Cliente;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;
    List<Cliente> clientes = new ArrayList<>();
    ClienteAdapter adapter;
    List<Cliente> todosClientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        dbHelper = new DBHelper(this);

        RecyclerView recycler = findViewById(R.id.recyclerClientes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteAdapter(clientes, this::abrirComanda);
        recycler.setAdapter(adapter);

        carregarClientes();
        SearchView searchView = findViewById(R.id.searchCliente);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Não usaremos o "submit"
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarClientes(newText);
                return true;
            }
        });

        Button btn = findViewById(R.id.btnAdicionarCliente);
        btn.setOnClickListener(v -> mostrarDialogoNovoCliente());

        findViewById(R.id.btnHistorico).setOnClickListener(v -> startActivity(new Intent(this, HistoricoActivity.class)));

        findViewById(R.id.btnZerarTudo).setOnClickListener(v -> mostrarDialogoSenha());

    }

    @SuppressLint("NotifyDataSetChanged")
    private void filtrarClientes(String texto) {
        clientes.clear();
        for (Cliente c : todosClientes) {
            if (c.nome.toLowerCase().contains(texto.toLowerCase())) {
                clientes.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void carregarClientes() {
        todosClientes.clear();
        clientes.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM clientes", null);
        while (cursor.moveToNext()) {
            Cliente c = new Cliente(cursor.getInt(0), cursor.getString(1));
            todosClientes.add(c);
        }
        cursor.close();
        clientes.addAll(todosClientes);
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogoNovoCliente() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this)
                .setTitle("Nome do Cliente")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = input.getText().toString().trim();
                    if (!nome.isEmpty()) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("nome", nome);
                        db.insert("clientes", null, values);
                        carregarClientes();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirComanda(Cliente cliente) {
        Intent intent = new Intent(this, ComandaActivity.class);
        intent.putExtra("cliente_id", cliente.id);
        intent.putExtra("cliente_nome", cliente.nome);
        startActivity(intent);
    }

    private void mostrarDialogoSenha() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Digite a senha para apagar tudo")
                .setView(input)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    String senha = input.getText().toString();
                    if (senha.equals("admin123")) {
                        apagarTudo();
                    } else {
                        Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void apagarTudo() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("itens", null, null);
        db.delete("clientes", null, null);
        db.delete("itens_fechados", null, null);
        carregarClientes();
        Toast.makeText(this, "Dados apagados com sucesso!", Toast.LENGTH_SHORT).show();
    }

}
