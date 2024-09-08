package com.example.footymastermind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Game_RecyclerViewAdapter extends RecyclerView.Adapter<Game_RecyclerViewAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;

    Context context;
    ArrayList<GameModel> gameModels;

    public Game_RecyclerViewAdapter(Context context, ArrayList<GameModel> gameModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.gameModels = gameModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public Game_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new Game_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull Game_RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.gameName.setText(gameModels.get(position).getGameName());
        holder.gameCategory.setText(gameModels.get(position).getGameCategory());
        holder.imageView.setImageResource(gameModels.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return gameModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView gameName, gameCategory;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView5);
            gameName = itemView.findViewById(R.id.textView4);
            gameCategory = itemView.findViewById(R.id.textView7);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
