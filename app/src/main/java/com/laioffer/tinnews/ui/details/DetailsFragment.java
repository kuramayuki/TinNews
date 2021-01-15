package com.laioffer.tinnews.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.laioffer.tinnews.databinding.FragmentDetailsBinding;
import com.laioffer.tinnews.model.Article;
import com.squareup.picasso.Picasso;

public class DetailsFragment extends Fragment {
    private FragmentDetailsBinding binding;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Article article = DetailsFragmentArgs.fromBundle(getArguments()).getArticle();
        binding.detailsTitleTextView.setText(article.title);
        binding.detailsAuthorTextView.setText(article.author);
        binding.detailsDateTextView.setText(article.publishedAt);
        binding.detailsDescriptionTextView.setText(article.description);
        binding.detailsContentTextView.setText(article.content);
        Picasso.get().load(article.urlToImage).into(binding.detailsImageView);

        binding.detailsUrlTextView.setOnClickListener(view1 -> gotoUrl(article.url));
    }

    //bonus: click to read more, hyperlink
    private void gotoUrl(String url) {
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));

    }
    //add color change after click
    //TODO

}