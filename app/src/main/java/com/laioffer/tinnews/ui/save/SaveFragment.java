package com.laioffer.tinnews.ui.save;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
            public void onRemoveFavorite(Article article) {
                viewModel.deleteSavedArticle(article);
                //binding.newsSavedRecyclerView.getAdapter().notifyItemRemoved(holderPos);
                //Log.i("Xue delete pos", "delete Position " + holderPos);
                //toast is currently hardcode
                //will implement deleteAsyncTask to make delete has a LiveData return
                //which indicated whether the deletion is completed from room database
                Toast.makeText(getActivity(), "News Unsaved", Toast.LENGTH_SHORT).show();
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
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                //swipe article position
                //?How to change to Room database? NEED SOME WORKs on DAO
                //TODO
                binding.newsSavedRecyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int removedPosition = viewHolder.getAdapterPosition();
                Article removedArticle = savedNewsAdapter.getArticleAt(removedPosition); //will used for UNDO feature
                viewModel.deleteSavedArticle(removedArticle);
                //now the toast is hardcode, will implement deleteAsyncTask to make delete feature has a LiveData return
                //which indicated whether the deletion is completed from room database
                binding.newsSavedRecyclerView.getAdapter().notifyItemRemoved(removedPosition);
                Log.i("Xue swipe pos", "removedPosition " + removedPosition);
                Toast.makeText(getActivity(), "News Deleted", Toast.LENGTH_SHORT).show();

            }
        }).attachToRecyclerView(binding.newsSavedRecyclerView);
    }

    //future implement: menu option - delete all news

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}