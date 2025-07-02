package com.example.comandarapida.models;

public class Item {
    public int id;
    public int clienteId;
    public String descricao;
    public int quantidade;
    public double preco;

    public Item(int id, int clienteId, String descricao, int quantidade, double preco) {
        this.id = id;
        this.clienteId = clienteId;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public double getTotal() {
        return quantidade * preco;
    }
}
