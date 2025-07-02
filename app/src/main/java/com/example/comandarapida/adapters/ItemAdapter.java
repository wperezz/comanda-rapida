package com.example.comandarapida.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.comandarapida.R;
import com.example.comandarapida.models.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private final List<Item> itens;

    public ItemAdapter(List<Item> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        Item item = itens.get(position);
        holder.txtDescricao.setText(item.descricao);
        holder.txtQtdPreco.setText(item.quantidade + " x R$ " + String.format("%.2f", item.preco));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescricao, txtQtdPreco;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtQtdPreco = itemView.findViewById(R.id.txtQtdPreco);
        }
    }
}
