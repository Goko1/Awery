package com.mrboomdev.awery.util.graphql;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class GraphQLError {
	private Location[] locations;
	private String[] messages;
	private String message;
	private int status;

	public static class Location {
		public int line, column;
	}

	public String toUserReadableString() {
		return ((messages == null ? message : Arrays.toString(messages))
				+ ", Error: " + status).replaceAll(".,", ",");
	}

	@NonNull
	@Override
	public String toString() {
		var builder = new StringBuilder()
				.append("{ ")
				.append(messages == null ? message : Arrays.toString(messages))
				.append(", status: ")
				.append(status);

		if(locations != null) {
			builder.append(", locations: [ ");

			for(var location : locations) {
				builder.append("line: ")
						.append(location.line)
						.append("column: ")
						.append(location.column);
			}

			builder.append(" ]");
		}

		builder.append(" }");
		return builder.toString();
	}
}