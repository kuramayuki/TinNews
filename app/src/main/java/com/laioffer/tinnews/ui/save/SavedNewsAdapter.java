package com.laioffer.tinnews.ui.save;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.laioffer.tinnews.R;
import com.laioffer.tinnews.databinding.SavedNewsItemBinding;
import com.laioffer.tinnews.model.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SavedNewsAdapter extends RecyclerView.Adapter<SavedNewsAdapter.SavedNewsViewHolder> {

    //my own on click listener


    interface  ItemCallback {
        //onOpenDetails: opening a new fragment for article detail
        void onOpenDetails(Article article);
        //onRemoveFavorite: remove articles in the saved database
        void onRemoveFavorite(Article article);
    }

    //1. Supporting Data
    private List<Article> articles = new ArrayList<>();
    int lastPosition = -1; //for load animation

    public void setArticles(List<Article> newsList) {
        articles.clear();
        articles.addAll(newsList);
        notifyDataSetChanged();
    }

    //bonus: get article position for onSwiped to delete
    public Article getArticleAt(int position) {
        return articles.get(position);
    }

    //after get articles if itemCallBack
    private ItemCallback itemCallback;
    public void setItemCallback(ItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }

    //2. Adapter overrides
    @NonNull
    @Override
    public SavedNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_news_item, parent, false);
        return new SavedNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedNewsViewHolder holder, int position) {
        Article article = articles.get(position);//?the position here is not the same as in recyclerview?
        loadAnimation(holder.cardView, position);//bonus: when loading cardView slid in left
        //holder.cardView.setAnimation(AnimationUtils.loadAnimation(holder.cardView.getContext(), R.anim.fade_scale_animation));
        holder.titleTextView.setText(article.title);
        holder.dateTextView.setText(article.publishedAt);
        holder.authorTextView.setText(article.author);
        holder.descriptionTextView.setText(article.description);
        //check if image is null
        if (article.urlToImage != null) {
            Picasso.get().load(article.urlToImage).into(holder.imageView);
        }
        holder.favoriteIcon.setOnClickListener(v -> itemCallback.onRemoveFavorite(article));
        holder.itemView.setOnClickListener(v -> itemCallback.onOpenDetails(article));
    }

    //bonus: load cardView w/ slide in left animation
    public void loadAnimation(View cardView, int position) {
        if(position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(cardView.getContext(),android.R.anim.slide_in_left);
            cardView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    //3. SavedNewsViewHolder
    public static class SavedNewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        TextView authorTextView;
        TextView descriptionTextView;
        ImageView favoriteIcon;
        ImageView imageView;
        CardView cardView;//bonus: create reference to the whole item cardView for animation

        public SavedNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            SavedNewsItemBinding binding = SavedNewsItemBinding.bind(itemView);
            titleTextView = binding.savedItemTitleTextView;
            dateTextView = binding.savedItemDateTextView;
            authorTextView = binding.savedItemAuthorContent;
            descriptionTextView = binding.savedItemDescriptionContent;
            favoriteIcon = binding.savedItemFavoriteImageView;
            imageView = binding.savedItemImageView;
            cardView = binding.savedItemContainer;
        }
    }
}
