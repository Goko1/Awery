package com.mrboomdev.awery.ui.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.mrboomdev.awery.app.AweryApp.getConfiguration;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mrboomdev.awery.databinding.MediaCatalogFeaturedBinding;
import com.mrboomdev.awery.databinding.MediaCatalogFeaturedPagerBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java9.util.stream.Collectors;

public class FeaturedMediaAdapter extends SingleViewAdapter {
	private final List<CatalogMedia> items = new ArrayList<>();
	private final PagerAdapter adapter = new PagerAdapter();

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(Collection<? extends CatalogMedia> items) {
		this.items.clear();
		this.items.addAll(items);
		adapter.notifyDataSetChanged();
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull ViewGroup parent) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = MediaCatalogFeaturedPagerBinding.inflate(inflater, parent, false);
		binding.pager.setAdapter(adapter);

		// What? I don't even remember why is it there.
		setEnabled(isEnabled());

		ViewUtil.setOnApplyUiInsetsListener(binding.headerWrapper, insets -> {
			ViewUtil.setTopMargin(binding.headerWrapper, insets.top);
			ViewUtil.setRightMargin(binding.headerWrapper, insets.right);
			ViewUtil.setLeftMargin(binding.headerWrapper, insets.left);
			return false;
		}, parent);

		if(binding.pageIndicator != null) {
			ViewUtil.setOnApplyUiInsetsListener(binding.pageIndicator, insets ->
					ViewUtil.setRightMargin(binding.pageIndicator, insets.right), parent);
		}

		return binding.getRoot();
	}

	public class PagerAdapter extends RecyclerView.Adapter<PagerViewHolder> {

		@NonNull
		@Override
		public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = MediaCatalogFeaturedBinding.inflate(inflater, parent, false);
			var holder = new PagerViewHolder(binding);

			binding.getRoot().setOnClickListener(v -> MediaUtils.launchMediaActivity(
					parent.getContext(), holder.getItem()));

			binding.watch.setOnClickListener(v -> MediaUtils.launchMediaActivity(
					parent.getContext(), holder.getItem(), "watch"));

			binding.bookmark.setOnClickListener(v -> MediaUtils.openMediaBookmarkMenu(
					parent.getContext(), holder.getItem()));

			binding.getRoot().setOnLongClickListener(v -> {
				var media = holder.getItem();
				var index = items.indexOf(media);

				MediaUtils.openMediaActionsMenu(parent.getContext(), media, () -> {
					MediaUtils.isMediaFiltered(media, isFiltered -> {
						if(!isFiltered) return;

						runOnUiThread(() -> {
							items.remove(index);
							notifyItemRemoved(index);
						});
					});
				});
				return true;
			});

			ViewUtil.setOnApplyUiInsetsListener(binding.leftSideBarrier, insets -> {
				ViewUtil.setLeftMargin(binding.leftSideBarrier, insets.left);
				return true;
			}, parent);

			ViewUtil.setOnApplyUiInsetsListener(binding.rightSideBarrier, insets -> {
				ViewUtil.setRightMargin(binding.rightSideBarrier, insets.right);
				return true;
			}, parent);

			ViewUtil.setOnApplyUiInsetsListener(binding.topSideBarrier, insets -> {
				ViewUtil.setTopMargin(binding.topSideBarrier, insets.top);
				return true;
			}, parent);

			return holder;
		}

		@Override
		public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			if(!isEnabled()) {
				return 0;
			}

			return items.size();
		}
	}

	public static class PagerViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogFeaturedBinding binding;
		private CatalogMedia item;

		public PagerViewHolder(@NonNull MediaCatalogFeaturedBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public View getView() {
			return binding.getRoot();
		}

		public CatalogMedia getItem() {
			return item;
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogMedia item) {
			binding.title.setText(item.getTitle());

			var description = item.description == null ? null :
					Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT).toString().trim();

			while(description != null && description.contains("\n\n")) {
				description = description.replaceAll("\n\n", "\n");
			}

			binding.description.setText(description);

			if(item.averageScore != null) {
				binding.metaSeparator.setVisibility(View.VISIBLE);
				binding.status.setVisibility(View.VISIBLE);
				binding.status.setText(item.averageScore + "/10");
			} else {
				binding.metaSeparator.setVisibility(View.GONE);
				binding.status.setVisibility(View.GONE);
			}

			var tagsCount = new AtomicInteger(0);

			var formattedTags = (item.genres != null ? (
					stream(item.genres)
							.filter(tag -> tagsCount.getAndAdd(1) < 3)
				) : (
					stream(item.tags)
							.filter(tag -> tagsCount.getAndAdd(1) < 3)
							.map(CatalogTag::getName)
			)).collect(Collectors.joining(", "));

			binding.tags.setText(formattedTags);

			binding.poster.setImageDrawable(null);
			binding.banner.setImageDrawable(null);

			if((getConfiguration(getContext(binding)).uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
				binding.metaSeparator.setShadowLayer(1, 0, 0, Color.BLACK);
				binding.tags.setShadowLayer(1, 0, 0, Color.BLACK);
				binding.status.setShadowLayer(1, 0, 0, Color.BLACK);
				binding.title.setShadowLayer(3, 0, 0, Color.BLACK);
				binding.description.setShadowLayer(2, 0, 0, Color.BLACK);
			} else {
				binding.metaSeparator.setShadowLayer(0, 0, 0, 0);
				binding.tags.setShadowLayer(0, 0, 0, 0);
				binding.status.setShadowLayer(0, 0, 0, 0);
				binding.title.setShadowLayer(0, 0, 0, 0);
				binding.description.setShadowLayer(0, 0, 0, 0);
			}

			Glide.with(binding.getRoot())
					.load(item.poster.extraLarge)
					.transition(withCrossFade())
					.into(binding.poster);

			/*
			 Because of some strange bug we have to load banner by our hands
			 or else it'll in some moment will stretch
			*/

			Glide.with(binding.getRoot())
					.load(item.banner != null ? item.banner : item.poster.extraLarge)
					.into(new CustomTarget<Drawable>() {
						@Override
						public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
							if(item != PagerViewHolder.this.item) return;
							binding.banner.setImageDrawable(resource);
						}

						@Override
						public void onLoadCleared(@Nullable Drawable placeholder) {}
					});

			this.item = item;
		}
	}
}