package com.laioffer.tinnews.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.laioffer.tinnews.R;
import com.laioffer.tinnews.databinding.FragmentHomeBinding;
import com.laioffer.tinnews.databinding.FragmentSearchBinding;
import com.laioffer.tinnews.model.Article;
import com.laioffer.tinnews.repository.NewsRepository;
import com.laioffer.tinnews.repository.NewsViewModelFactory;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.List;

import static java.lang.Boolean.TRUE;

public class HomeFragment extends Fragment implements CardStackListener {

    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;
    private CardStackLayoutManager layoutManager;
    private List<Article> articles;

    private void swipeCard(Direction direction) {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                .setDirection(direction).setDuration(Duration.Normal.duration).build();
        layoutManager.setSwipeAnimationSetting(setting);
        binding.homeCardStackView.swipe();
    }

    //Bonus rewind feature
    private void rewindCard(){
        RewindAnimationSetting setting = new RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom).setDuration(Duration.Normal.duration)
                .setInterpolator(new DecelerateInterpolator())
                .build();
        layoutManager.setRewindAnimationSetting(setting);
        binding.homeCardStackView.rewind();
        //bonus bonus: after rewind process
        //TODO
        //re-like or unlike
        //unlike => delete from db
        //re-like => "News Already Saved! "
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
        // Inflate the layout for this fragment

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setup CardStackView
        CardSwipeAdapter swipeAdapter = new CardSwipeAdapter();
        layoutManager = new CardStackLayoutManager(requireContext(), this);
        layoutManager.setStackFrom(StackFrom.Top);
        binding.homeCardStackView.setLayoutManager(layoutManager);
        binding.homeCardStackView.setAdapter(swipeAdapter);

        //Handle like unlike button clicks
        binding.homeLikeButton.setOnClickListener(v -> swipeCard(Direction.Right));
        binding.homeUnlikeButton.setOnClickListener(v -> swipeCard(Direction.Left));

        //Handle rewind button clicks
        binding.homeRewindButton.setOnClickListener(v -> rewindCard());

        NewsRepository repository = new NewsRepository(getContext());
        viewModel = new ViewModelProvider(this, new NewsViewModelFactory(repository))
                .get(HomeViewModel.class);
        viewModel.setCountryInput("us");
        viewModel
                .getTopHeadlines()
                .observe(
                        getViewLifecycleOwner(),
                        newsResponse -> {
                            if (newsResponse != null) {
                                articles = newsResponse.articles;
                                swipeAdapter.setArticles(articles);
                                //Log.d("HomeFragment", newsResponse.toString());
                            }
                        });

    }


    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (direction == Direction.Left) {
            Log.d("CardStackView", "Unlike" + layoutManager.getTopPosition());
        } else if (direction == Direction.Right) {
            Log.d("CardStackView", "Like" + layoutManager.getTopPosition());
            Article article = articles.get(layoutManager.getTopPosition() - 1);//-1 => prev card => because already swiped out
            //bonus bonus: after rewind or reload if article already liked => Toast "News Already Saved!"
            //TODO
            //check Article table whether contain the id?
            viewModel.setFavoriteArticleInput(article).observe( // bonus: add new observe to observe resultLiveData => implement Toast
                    getViewLifecycleOwner(),
                    resultLiveData -> {
                        //bonus: Toast
                        //if favoriteArticle(article) ture?
                        if (resultLiveData == TRUE) {
                            Toast.makeText(getContext(), "News Saved !", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        }
    }

    @Override
    public void onCardRewound() {

    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {

    }

    @Override
    public void onCardDisappeared(View view, int position) {

    }
}