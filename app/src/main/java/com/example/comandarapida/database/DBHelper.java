package com.example.comandarapida.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "comanda.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE clientes (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT)");
        db.execSQL("CREATE TABLE itens (id INTEGER PRIMARY KEY AUTOINCREMENT, cliente_id INTEGER, descricao TEXT, quantidade INTEGER, preco REAL)");
        db.execSQL("CREATE TABLE itens_fechados (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cliente_nome TEXT, " +
                "descricao TEXT, " +
                "quantidade INTEGER, " +
                "preco REAL, " +
                "data_hora TEXT)");
        db.execSQL("CREATE TABLE catalogo_itens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "descricao TEXT, " +
                "preco REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS itens");
        db.execSQL("DROP TABLE IF EXISTS clientes");
        onCreate(db);
    }
}
