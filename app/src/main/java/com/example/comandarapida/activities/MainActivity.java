package com.example.comandarapida.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comandarapida.R;
import com.example.comandarapida.adapters.ClienteAdapter;
import com.example.comandarapida.database.DBHelper;
import com.example.comandarapida.models.Cliente;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;
    List<Cliente> clientes = new ArrayList<>();
    ClienteAdapter adapter;
    List<Cliente> todosClientes = new ArrayList<>();
    DrawerLayout drawerLayout;
    NavigationView navView;
    ActionBarDrawerToggle toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("comanda_config", MODE_PRIVATE);
        boolean modoEscuro = prefs.getBoolean("modo_escuro", false);
        AppCompatDelegate.setDefaultNightMode(modoEscuro ?
                AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        dbHelper = new DBHelper(this);

        RecyclerView recycler = findViewById(R.id.recyclerClientes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteAdapter(clientes, this::abrirComanda);
        recycler.setAdapter(adapter);

        carregarClientes();

        mostrarResumoDoDia();

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

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_novo_cliente) {
                mostrarDialogoNovoCliente();
            } else if (id == R.id.nav_cadastrar_item) {
                startActivity(new Intent(this, CadastroItemActivity.class));
            } else if (id == R.id.nav_historico) {
                startActivity(new Intent(this, HistoricoActivity.class));
            } else if (id == R.id.nav_zerar_tudo) {
                mostrarDialogoSenha();
            }else if (id == R.id.nav_configuracoes) {
                startActivity(new Intent(this, ConfiguracoesActivity.class));
            }else if (id == R.id.nav_relatorio) {
                startActivity(new Intent(this, RelatorioActivity.class));
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarResumoDoDia(); // atualiza quando volta para tela inicial
    }

    private void mostrarResumoDoDia() {
        Cursor cursor = dbHelper.getResumoDoDia();
        if (cursor.moveToFirst()) {
            int totalQtd = cursor.getInt(cursor.getColumnIndexOrThrow("totalQtd"));
            double totalValor = cursor.getDouble(cursor.getColumnIndexOrThrow("totalValor"));

            TextView resumoDia = findViewById(R.id.txtResumoDia);
            resumoDia.setText("Hoje: " + totalQtd + " und • R$ " + String.format("%.2f", totalValor));
        }
        cursor.close();
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
                    SharedPreferences prefs = getSharedPreferences("comanda_config", MODE_PRIVATE);
                    String senhaSalva = prefs.getString("senha_zerar", "admin123");
                    if (senha.equals(senhaSalva))
                    {
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
        db.delete("itens_comanda", null, null);
        db.delete("clientes", null, null);
        db.delete("itens_fechados", null, null);
        carregarClientes();
        Toast.makeText(this, "Dados apagados com sucesso!", Toast.LENGTH_SHORT).show();
    }

}
