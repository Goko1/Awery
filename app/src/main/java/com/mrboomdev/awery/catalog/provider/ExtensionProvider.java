package com.mrboomdev.awery.catalog.provider;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class ExtensionProvider {
	public static final UnimplementedException NOT_IMPLEMENTED = new UnimplementedException("Not implemented!");
	public static final IllegalStateException CONNECTION_FAILED = new IllegalStateException("Failed to connect!");
	public static final IllegalStateException ZERO_RESULTS = new IllegalStateException("Zero results were found!");

	public void search(SearchParams params, @NonNull ResponseCallback<Collection<CatalogMedia>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public record SearchParams(Integer page, String query) {

		public static class Builder {
			private String query;
			private Integer page;

			public Builder setPage(Integer page) {
				this.page = page;
				return this;
			}

			public Builder setQuery(String query) {
				this.query = query;
				return this;
			}

			public SearchParams build() {
				return new SearchParams(page, query);
			}
		}
	}

	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<Collection<CatalogEpisode>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<Collection<CatalogVideo>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public abstract String getName();

	public String getLang() {
		return "en";
	}

	public void getCatalogCategories(@NonNull ResponseCallback<Map<String, CatalogCategory>> callback) {
		callback.onFailure(NOT_IMPLEMENTED);
	}

	public interface ResponseCallback<T> {
		void onSuccess(T t);
		void onFailure(Throwable e);
	}
}