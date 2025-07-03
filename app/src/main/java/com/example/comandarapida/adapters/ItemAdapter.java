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
    private final View.OnClickListener onClickListener;
    private final View.OnLongClickListener onLongClickListener;

    public ItemAdapter(List<Item> itens, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        this.itens = itens;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itens.get(position);
        holder.txtDescricao.setText(item.descricao);
        holder.txtQtdPreco.setText(item.quantidade + " x R$ " + String.format("%.2f", item.preco));
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(onClickListener);
        holder.itemView.setOnLongClickListener(onLongClickListener);
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
