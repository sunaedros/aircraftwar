package edu.hitsz.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.R;

public class LeaderboardAdapter extends BaseAdapter {

    public interface OnDeleteClickListener {
        void onDelete(LeaderboardEntry entry);
    }

    private final LayoutInflater inflater;
    private final List<LeaderboardEntry> items = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;

    public LeaderboardAdapter(Context context, OnDeleteClickListener deleteClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.deleteClickListener = deleteClickListener;
    }

    public void submitList(List<LeaderboardEntry> entries) {
        items.clear();
        items.addAll(entries);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LeaderboardEntry getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_leaderboard, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LeaderboardEntry entry = getItem(position);
        holder.rankText.setText(String.valueOf(position + 1));
        holder.nameText.setText(entry.getPlayerName());
        holder.scoreText.setText(String.valueOf(entry.getScore()));
        holder.timeText.setText(entry.getCreatedAt());
        holder.deleteText.setOnClickListener(v -> deleteClickListener.onDelete(entry));
        return convertView;
    }

    private static class ViewHolder {
        private final TextView rankText;
        private final TextView nameText;
        private final TextView scoreText;
        private final TextView timeText;
        private final TextView deleteText;

        private ViewHolder(View itemView) {
            rankText = itemView.findViewById(R.id.rank_text);
            nameText = itemView.findViewById(R.id.player_name_text);
            scoreText = itemView.findViewById(R.id.score_text);
            timeText = itemView.findViewById(R.id.time_text);
            deleteText = itemView.findViewById(R.id.delete_text);
        }
    }
}
