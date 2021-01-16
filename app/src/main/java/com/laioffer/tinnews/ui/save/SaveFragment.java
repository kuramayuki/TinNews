package com.laioffer.tinnews.ui.save;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.laioffer.tinnews.R;
import com.laioffer.tinnews.databinding.FragmentSaveBinding;
import com.laioffer.tinnews.model.Article;
import com.laioffer.tinnews.repository.NewsRepository;
import com.laioffer.tinnews.repository.NewsViewModelFactory;

import java.util.Collections;
import java.util.List;


public class SaveFragment extends Fragment {

    private FragmentSaveBinding binding;
    private SaveViewModel viewModel;

    public SaveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSaveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SavedNewsAdapter savedNewsAdapter = new SavedNewsAdapter();
        binding.newsSavedRecyclerView.setAdapter(savedNewsAdapter);
        binding.newsSavedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        savedNewsAdapter.setItemCallback(new SavedNewsAdapter.ItemCallback() {
            @Override
            public void onOpenDetails(Article article) {
                Log.d("onOpenDetails", article.toString());
                SaveFragmentDirections.ActionNavigationSaveToNavigationDetails direction = SaveFragmentDirections.actionNavigationSaveToNavigationDetails(article);

                NavHostFragment.findNavController(SaveFragment.this).navigate(direction);
            }

            @Override
            public void onRemoveFavorite(Article article, int position) {
                viewModel.deleteSavedArticle(article);
                binding.newsSavedRecyclerView.getAdapter().notifyItemRemoved(position);
                binding.newsSavedRecyclerView.getAdapter().notifyItemRangeChanged(position, savedNewsAdapter.getItemCount());
                Log.i("Xue delete pos", "delete Position " + position);
                //toast is currently hardcode
                //will implement deleteAsyncTask to make delete has a LiveData return
                //which indicated whether the deletion is completed from room database
                Toast.makeText(getActivity(), "News Unsaved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateArticles(List<Article> articles) {
                viewModel.updateSavedArticles(articles);
            }
        });

        NewsRepository repository = new NewsRepository(getContext()); //requireContext() vs getContext() ?
        viewModel = new ViewModelProvider(this, new NewsViewModelFactory(repository)).get(SaveViewModel.class);
        viewModel.getAllSavedArticles()
                .observe(
                        getViewLifecycleOwner(),
                        savedArticles -> {
                            if (savedArticles != null) {
                                Log.d("SaveFragment", savedArticles.toString());
                                savedNewsAdapter.setArticles(savedArticles);
                            }
                        }
                );
        //bonus: swipe to delete, using ItemTouchHelper w/ delete animation
        //bonus: drag to reorder

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                savedNewsAdapter.onItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int removedPosition = viewHolder.getAdapterPosition();
                Article removedArticle = savedNewsAdapter.getArticleAt(removedPosition);
                viewModel.deleteSavedArticle(removedArticle);
                binding.newsSavedRecyclerView.getAdapter().notifyItemRemoved(removedPosition);
                Log.i("Xue swipe pos", "removedPosition " + removedPosition);
                Toast.makeText(getActivity(), "News Deleted", Toast.LENGTH_SHORT).show();
                //savedNewsAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1.0f);
            }

            @Override
            public void onSelectedChanged(@NonNull RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setAlpha(0.7f);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                final ColorDrawable background = new ColorDrawable(Color.LTGRAY);
                View itemView = viewHolder.itemView;
                Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete_24dp);
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                background.setBounds(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(),itemView.getBottom());
                deleteIcon.setBounds(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(), itemView.getTop() + iconMargin, itemView.getRight() - iconMargin, itemView.getBottom() - iconMargin);
                background.draw(c);
                c.save();
                c.clipRect(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                deleteIcon.draw(c);
                c.restore();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        helper.attachToRecyclerView(binding.newsSavedRecyclerView);
    }
//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
//                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                int fromPosition = viewHolder.getAdapterPosition();
//                int toPosition = target.getAdapterPosition();
//                //swipe article position
//                //?How to change to Room database? NEED SOME WORKs on DAO
//                //TODO
//                savedNewsAdapter.onItemMoved(fromPosition, toPosition);
//                binding.newsSavedRecyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
//
//                return true;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int removedPosition = viewHolder.getAdapterPosition();
//                Article removedArticle = savedNewsAdapter.getArticleAt(removedPosition); //will used for UNDO feature
//                viewModel.deleteSavedArticle(removedArticle);
//                //now the toast is hardcode, will implement deleteAsyncTask to make delete feature has a LiveData return
//                //which indicated whether the deletion is completed from room database
//                binding.newsSavedRecyclerView.getAdapter().notifyItemRemoved(removedPosition);
//                Log.i("Xue swipe pos", "removedPosition " + removedPosition);
//                Toast.makeText(getActivity(), "News Deleted", Toast.LENGTH_SHORT).show();
//
//            }
//        }).attachToRecyclerView(binding.newsSavedRecyclerView);
//    }

    //future implement: menu option - delete all news

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}