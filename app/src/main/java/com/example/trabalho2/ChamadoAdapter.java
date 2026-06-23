package com.example.trabalho2;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            case "Concluido":
            case "Concluído":
                color = context.getResources().getColor(R.color.status_concluido);
                break;
            default:
                color = context.getResources().getColor(R.color.primary);
                break;
        }
        holder.txtStatus.setTextColor(color);

        // Miniatura da imagem (suporta content:// URI e file path)
        carregarImagem(holder.imgThumbnail, chamadoAtual.getImagemPath());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(chamadoAtual));
    }

    private void carregarImagem(ImageView imageView, String path) {
        if (path == null || path.isEmpty()) {
            imageView.setVisibility(View.GONE);
            return;
        }

        try {
            Bitmap bmp = ImageUtils.loadRotatedBitmap(context, path, 180, 180);
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
                imageView.setVisibility(View.VISIBLE);
                return;
            }
        } catch (Exception e) {
            // falha silenciosa ao carregar imagem
        }

        imageView.setVisibility(View.GONE);
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
