package com.mrboomdev.awery.data.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SettingsItem {
	private final String key;
	private final SettingsItemType type;
	private List<SettingsItem> items;
	private SettingsItem parent;
	@Json(name = "boolean_value")
	private Boolean booleanValue;
	@Json(name = "int_value")
	private Integer intValue;

	public SettingsItem(String key, SettingsItemType type, List<SettingsItem> items) {
		this.key = key;
		this.type = type;
		this.items = items;
	}

	public SettingsItem(String key, SettingsItemType type) {
		this(key, type, Collections.emptyList());
	}

	public void setAsParentForChildren() {
		if(items == null) return;

		for(var item : items) {
			item.setParent(this);
			item.setAsParentForChildren();
		}
	}

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean value) {
		booleanValue = value;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int value) {
		intValue = value;
	}

	public void setParent(SettingsItem parent) {
		this.parent = parent;
	}

	public SettingsItem getParent() {
		return parent;
	}

	public void mergeValues(SettingsItem item) {
		if(item == null) return;

		if(item.booleanValue != null) booleanValue = item.booleanValue;
		if(item.intValue != null) intValue = item.intValue;

		if(items != null) {
			for(var child : items) {
				child.mergeValues(item.findDirect(child.getKey()));
			}
		} else {
			items = item.items;
		}
	}

	public boolean hasParent() {
		return parent != null;
	}

	public String getFullKey() {
		if(!hasParent()) {
			return key;
		}

		return parent.getFullKey() + "_" + key;
	}

	public String getKey() {
		return key;
	}

	public SettingsItemType getType() {
		return type;
	}

	public List<SettingsItem> getItems() {
		return items;
	}

	private SettingsItem findDirect(String key) {
		return items.stream()
				.filter(item -> item.getKey().equals(key))
				.findFirst()
				.orElse(null);
	}

	public SettingsItem find(@NonNull String query) {
		var parts = query.split("\\.");

		if(parts.length == 1) {
			return findDirect(parts[0]);
		}

		if(parts.length > 1) {
			return items.stream()
					.filter(item -> item.getKey().equals(parts[0]))
					.map(item -> item.find(String.join(".", parts).substring(parts[0].length())))
					.findFirst()
					.orElse(null);
		}

		return null;
	}

	@NonNull
	@Override
	public String toString() {
		var result = "{ \"key\": \"" + key + "\"";

		if(intValue != null) {
			result += ", \"int_value\": " + intValue;
		}

		if(booleanValue != null) {
			result += ", \"boolean_value\": " + booleanValue;
		}

		if(type != null) {
			result += ", \"type\": \"" + type.toString().toLowerCase(Locale.ROOT) + "\"";
		}

		if(items != null) {
			result += ", \"items\": " + items;
		}

		return result + " }";
	}
}