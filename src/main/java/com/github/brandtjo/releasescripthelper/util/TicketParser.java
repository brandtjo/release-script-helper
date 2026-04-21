package com.github.brandtjo.releasescripthelper.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public final class TicketParser {

	private TicketParser() {}

	/**
	 * Extract ticket type and number from a branch name part (e.g. "OCT-1234").
	 *
	 * @param branchPart  the branch name part, split by [-_]
	 * @param ticketTypes ordered list of configured ticket type prefixes
	 * @return Optional containing [ticketType, ticketNumber] if a match is found, empty otherwise
	 */
	public static Optional<String[]> extractTicket(String branchPart, List<String> ticketTypes) {
		if (StringUtils.isBlank(branchPart) || ticketTypes == null || ticketTypes.isEmpty()) {
			return Optional.empty();
		}

		for (String ticketType : ticketTypes) {
			if (branchPart.regionMatches(true, 0, ticketType, 0, ticketType.length())) {
				String ticketNumber = branchPart.substring(ticketType.length());
				return Optional.of(new String[] { ticketType, ticketNumber });
			}
		}

		return Optional.empty();
	}
}
