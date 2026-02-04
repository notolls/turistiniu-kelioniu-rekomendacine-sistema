package com.example.projectkrs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectkrs.R;
import com.example.projectkrs.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.author.setText(comment.getAuthor());
        holder.text.setText(comment.getText());
        holder.ratingBar.setRating(comment.getRating());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView author, text;
        RatingBar ratingBar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.commentAuthor);
            text = itemView.findViewById(R.id.commentText);
            ratingBar = itemView.findViewById(R.id.commentRatingBar);
        }
    }
}
