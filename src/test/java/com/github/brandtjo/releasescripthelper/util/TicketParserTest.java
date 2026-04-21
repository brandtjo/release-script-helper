package com.github.brandtjo.releasescripthelper.util;

import com.github.brandtjo.releasescripthelper.util.TicketParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TicketParserTest {

	private static final List<String> TICKET_TYPES = List.of("oct", "CHG");

	@Test
	public void testStandardBranchWithHyphen() {
		// "OCT-1234" → type "oct", number "-1234"
		Assert.assertTrue("Should match", TicketParser.extractTicket("OCT-1234", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("OCT-1234", TICKET_TYPES).get();
		Assert.assertEquals("oct", result[0]);
		Assert.assertEquals("-1234", result[1]);
	}

	@Test
	public void testStandardBranchWithUnderscore() {
		// "CHG_5678" → type "CHG", number "_5678"
		Assert.assertTrue("Should match", TicketParser.extractTicket("CHG_5678", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("CHG_5678", TICKET_TYPES).get();
		Assert.assertEquals("CHG", result[0]);
		Assert.assertEquals("_5678", result[1]);
	}

	@Test
	public void testNoSeparatorAfterTicketType() {
		// "OCT1234" → type "oct", number "1234"
		Assert.assertTrue("Should match", TicketParser.extractTicket("OCT1234", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("OCT1234", TICKET_TYPES).get();
		Assert.assertEquals("oct", result[0]);
		Assert.assertEquals("1234", result[1]);
	}

	@Test
	public void testTicketTypeOnly() {
		// "OCT" → type "oct", number "" (empty, no remainder)
		Assert.assertTrue("Should match", TicketParser.extractTicket("OCT", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("OCT", TICKET_TYPES).get();
		Assert.assertEquals("oct", result[0]);
		Assert.assertEquals("", result[1]);
	}

	@Test
	public void testMixedCaseBranchPrefix() {
		// "Oct-999" → type "oct", number "-999"
		Assert.assertTrue("Should match", TicketParser.extractTicket("Oct-999", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("Oct-999", TICKET_TYPES).get();
		Assert.assertEquals("oct", result[0]);
		Assert.assertEquals("-999", result[1]);
	}

	@Test
	public void testSecondTicketTypeMatches() {
		// "chg-42" → type "CHG", number "-42"
		Assert.assertTrue("Should match CHG", TicketParser.extractTicket("chg-42", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("chg-42", TICKET_TYPES).get();
		Assert.assertEquals("CHG", result[0]);
		Assert.assertEquals("-42", result[1]);
	}

	@Test
	public void testNoMatch() {
		// "FEATURE-something" → no match
		Assert.assertFalse("Should not match", TicketParser.extractTicket("FEATURE-something", TICKET_TYPES).isPresent());
	}

	@Test
	public void testEmptyBranchPart() {
		Assert.assertFalse("Should not match empty string", TicketParser.extractTicket("", TICKET_TYPES).isPresent());
	}

	@Test
	public void testNullBranchPart() {
		Assert.assertFalse("Should not match null", TicketParser.extractTicket(null, TICKET_TYPES).isPresent());
	}

	@Test
	public void testBlankBranchPart() {
		Assert.assertFalse("Should not match blank string", TicketParser.extractTicket("   ", TICKET_TYPES).isPresent());
	}

	@Test
	public void testEmptyTicketTypes() {
		Assert.assertFalse("Should not match with empty ticket types",
				TicketParser.extractTicket("OCT-1234", List.of()).isPresent());
	}

	@Test
	public void testNullTicketTypes() {
		Assert.assertFalse("Should not match with null ticket types",
				TicketParser.extractTicket("OCT-1234", null).isPresent());
	}

	@Test
	public void testTicketTypeWithDigitsInBranch() {
		// "OCT-1234-fix" → type "oct", number "-1234" (only first part is checked)
		Assert.assertTrue("Should match", TicketParser.extractTicket("OCT-1234", TICKET_TYPES).isPresent());
		String[] result = TicketParser.extractTicket("OCT-1234", TICKET_TYPES).get();
		Assert.assertEquals("oct", result[0]);
		Assert.assertEquals("-1234", result[1]);
	}
}
