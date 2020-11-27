package com.dietms.asapadmin.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dietms.asapadmin.R;
import com.dietms.asapadmin.activity.NoticeActivity;
import com.dietms.asapadmin.model.Upload;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NoticeBoardRecyclerAdapter extends RecyclerView.Adapter<NoticeBoardRecyclerAdapter.NoticeBoardViewHolder> {

    private Context context;
    private List<Upload> uploads;
    private static OnItemClickListener listener;

    public NoticeBoardRecyclerAdapter(Context context, List<Upload> uploads) {
        this.context = context;
        this.uploads = uploads;
    }

    @NonNull
    @Override
    public NoticeBoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_noticeboard_single_row, parent, false);
        return new NoticeBoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeBoardViewHolder holder, int position) {
        final Upload uploadCurrent = uploads.get(position);
        holder.txtNoticeTitle.setText(uploadCurrent.getName());
        holder.txtNoticeOrigin.setText(uploadCurrent.getDate());
        Picasso.get().load(uploadCurrent.getImageUrl()).placeholder(R.drawable.ic_image_temp).fit().centerCrop().into(holder.imgNoticeImage);
        holder.imgNoticeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NoticeActivity.class);
                intent.putExtra("Title", uploadCurrent.getName());
                intent.putExtra("Image", uploadCurrent.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    public static class NoticeBoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        public TextView txtNoticeTitle;
        public TextView txtNoticeOrigin;
        public ImageView imgNoticeImage;

        public NoticeBoardViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNoticeTitle = itemView.findViewById(R.id.txtNoticeTitle);
            txtNoticeOrigin = itemView.findViewById(R.id.txtNoticeOrigin);
            imgNoticeImage = itemView.findViewById(R.id.imgNoticeImage);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.setHeaderTitle("Select Action");
//            MenuItem doWhatever = menu.add(Menu.NONE, 1, 1, "Do Whatever");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "Delete");

//            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (item.getItemId()) {
                        case 1:
                            listener.onWhateverClick(position);
                            return true;
                        case 2:
                            listener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onWhateverClick(int position);

        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
