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
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);

        RecyclerView recycler = findViewById(R.id.recyclerClientes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteAdapter(clientes, this::abrirComanda);
        recycler.setAdapter(adapter);

        carregarClientes();

        Button btn = findViewById(R.id.btnAdicionarCliente);
        btn.setOnClickListener(v -> mostrarDialogoNovoCliente());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void carregarClientes() {
        clientes.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM clientes", null);
        while (cursor.moveToNext()) {
            clientes.add(new Cliente(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
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
}
