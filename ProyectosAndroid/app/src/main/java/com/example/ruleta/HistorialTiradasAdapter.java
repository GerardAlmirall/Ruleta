package com.example.ruleta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistorialTiradasAdapter extends RecyclerView.Adapter<HistorialTiradasAdapter.TiradaViewHolder> {

    private List<TiradaClase> tiradas;

    public HistorialTiradasAdapter(List<TiradaClase> tiradas) {
        this.tiradas = tiradas;
    }

    @NonNull
    @Override
    public TiradaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_tirada, parent, false);
        return new TiradaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TiradaViewHolder holder, int position) {
        TiradaClase tirada = tiradas.get(position);
        holder.bind(tirada);
    }

    @Override
    public int getItemCount() {
        return tiradas.size();
    }

    public static class TiradaViewHolder extends RecyclerView.ViewHolder {

        private TextView txtResultado;
        private TextView txtPremio;
        private TextView txtApuesta;
        private TextView txtMonedas;

        public TiradaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtResultado = itemView.findViewById(R.id.txtResultado);
            txtPremio = itemView.findViewById(R.id.txtPremio);
            txtApuesta = itemView.findViewById(R.id.txtApuesta);
            txtMonedas = itemView.findViewById(R.id.txtMonedas);
        }

        public void bind(TiradaClase tirada) {
            txtResultado.setText(String.valueOf(tirada.getResultado()));
            txtPremio.setText(String.valueOf(tirada.getPremioSeleccionado()));
            txtApuesta.setText(String.valueOf(tirada.getApuesta()));
            txtMonedas.setText(String.valueOf(tirada.getMonedasTotales()));
        }
    }
}
