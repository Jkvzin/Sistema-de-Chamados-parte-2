package com.example.trabalho2;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class ChamadoAdapter extends RecyclerView.Adapter<ChamadoAdapter.ChamadoViewHolder> {

    private ArrayList<Chamado> lista;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Chamado chamadoClicado);
    }

    public ChamadoAdapter(ArrayList<Chamado> lista, Context context, OnItemClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChamadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chamado, parent, false);
        return new ChamadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChamadoViewHolder holder, int position) {
        Chamado chamadoAtual = lista.get(position);

        holder.txtTitulo.setText(chamadoAtual.getTitulo());
        holder.txtData.setText(chamadoAtual.getData());
        holder.txtLocal.setText("Local: " + chamadoAtual.getLocal());
        holder.txtTipo.setText("Tipo: " + chamadoAtual.getTipo());
        holder.txtStatus.setText("Status: " + chamadoAtual.getStatus());

        // Cor do status
        int color;
        switch (chamadoAtual.getStatus()) {
            case "Aberto":
                color = context.getResources().getColor(R.color.status_aberto);
                break;
            case "Em Andamento":
                color = context.getResources().getColor(R.color.status_em_andamento);
                break;
            case "Concluído":
                color = context.getResources().getColor(R.color.status_concluido);
                break;
            default:
                color = context.getResources().getColor(R.color.primary_indigo);
                break;
        }
        holder.txtStatus.setTextColor(color);

        // Miniatura da imagem (se houver)
        String imagemPath = chamadoAtual.getImagemPath();
        if (imagemPath != null && !imagemPath.isEmpty()) {
            File imgFile = new File(imagemPath);
            if (imgFile.exists()) {
                holder.imgThumbnail.setVisibility(View.VISIBLE);
                holder.imgThumbnail.setImageBitmap(
                        BitmapFactory.decodeFile(imagemPath));
            } else {
                holder.imgThumbnail.setVisibility(View.GONE);
            }
        } else {
            holder.imgThumbnail.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(chamadoAtual));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ChamadoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtData, txtLocal, txtTipo, txtStatus;
        ImageView imgThumbnail;

        public ChamadoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtItemTitulo);
            txtData = itemView.findViewById(R.id.txtItemData);
            txtLocal = itemView.findViewById(R.id.txtItemLocal);
            txtTipo = itemView.findViewById(R.id.txtItemTipo);
            txtStatus = itemView.findViewById(R.id.txtItemStatus);
            imgThumbnail = itemView.findViewById(R.id.imgItemThumbnail);
        }
    }
}
