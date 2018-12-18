package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
	private EPLiteClient client;
	private ClientAndServer mockServer;

	/**
	 * Useless testing as it depends on a specific API key
	 *
	 * TODO: Find a way to make it configurable
	 */
	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.mockserver.mock"))
						.setLevel(ch.qos.logback.classic.Level.OFF);

		mockServer = startClientAndServer(9001);
	}

	@After
	public void endServer() {
		mockServer.close();
	}

	@Test
	public void validate_token() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/checkToken?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.checkToken();
	}

	@Test
	public void create_and_delete_group() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createGroup?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.mVTckTQri9pQNaxw\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deleteGroup?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.mVTckTQri9pQNaxw"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId,
				groupId != null && groupId.startsWith("g."));

		client.deleteGroup(groupId);
	}

	@Test
	public void create_group_if_not_exists_for_and_list_all_groups()
			throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createGroupIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.6AmvJQSnAyV1aeCM\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/listAllGroups?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.6AmvJQSnAyV1aeCM\"]}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deleteGroup?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.6AmvJQSnAyV1aeCM"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String groupMapper = "groupname";

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		try {
			Map listResponse = client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

			client.createGroupIfNotExistsFor(groupMapper);

			listResponse = client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {
			client.deleteGroup(groupId);
		}
	}

	@Test
	public void create_group_pads_and_list_them() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createGroup?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.k5vhoWGwBXIjzW4f\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createGroupPad?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.k5vhoWGwBXIjzW4f&padName=integration-test-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.k5vhoWGwBXIjzW4f$integration-test-1\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/setPublicStatus?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.k5vhoWGwBXIjzW4f%24integration-test-1&publicStatus=true"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getPublicStatus?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID",
						"g.k5vhoWGwBXIjzW4f$integration-test-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\":true}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/setPassword?")
				.withBody(
						"password=integration&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.k5vhoWGwBXIjzW4f%24integration-test-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/isPasswordProtected?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID",
						"g.k5vhoWGwBXIjzW4f$integration-test-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"isPasswordProtected\":true}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createGroupPad?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.k5vhoWGwBXIjzW4f&padName=integration-test-2&text=Initial+text"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.k5vhoWGwBXIjzW4f$integration-test-2\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getText?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID",
						"g.k5vhoWGwBXIjzW4f$integration-test-2"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"Initial text\\n\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/listPads?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("groupID", "g.k5vhoWGwBXIjzW4f"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.k5vhoWGwBXIjzW4f$integration-test-1\",\"g.k5vhoWGwBXIjzW4f$integration-test-2\"]}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deleteGroup?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.k5vhoWGwBXIjzW4f"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();
		String groupId = (String) response.get("groupID");
		String padName1 = "integration-test-1";
		String padName2 = "integration-test-2";
		try {
			Map padResponse = client.createGroupPad(groupId, padName1);
			assertTrue(padResponse.containsKey("padID"));
			String padId1 = (String) padResponse.get("padID");

			client.setPublicStatus(padId1, true);
			boolean publicStatus = (boolean) client.getPublicStatus(padId1)
					.get("publicStatus");
			assertTrue(publicStatus);

			client.setPassword(padId1, "integration");
			boolean passwordProtected = (boolean) client
					.isPasswordProtected(padId1).get("isPasswordProtected");
			assertTrue(passwordProtected);

			padResponse = client.createGroupPad(groupId, padName2,
					"Initial text");
			assertTrue(padResponse.containsKey("padID"));

			String padId = (String) padResponse.get("padID");
			String initialText = (String) client.getText(padId).get("text");
			assertEquals("Initial text\n", initialText);

			Map padListResponse = client.listPads(groupId);

			assertTrue(padListResponse.containsKey("padIDs"));
			List padIds = (List) padListResponse.get("padIDs");

			assertEquals(2, padIds.size());
		} finally {
			client.deleteGroup(groupId);
		}
	}

	@Test
	public void create_author() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/createAuthor?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qM9H63JcknRhdMl1\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createAuthor?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qM9H63JcknRhdMl1\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getAuthorName?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("authorId", "a.qM9H63JcknRhdMl1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());

		authorResponse = client.createAuthor("integration-author");
		authorId = (String) authorResponse.get("authorID");

		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

	@Test
	public void create_author_with_author_mapper() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createAuthorIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.eAeCUAaCyAV4Z867\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getAuthorName?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("authorId", "a.eAeCUAaCyAV4Z867"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));

		String authorMapper = "username";

		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper,
				"integration-author-1");
		String firstAuthorId = (String) authorResponse.get("authorID");
		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());

		String firstAuthorName = client.getAuthorName(firstAuthorId);

		new MockServerClient("localhost", 9001).reset();

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createAuthorIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.eAeCUAaCyAV4Z867\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getAuthorName?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("authorId", "a.eAeCUAaCyAV4Z867"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper,
				"integration-author-2");
		String secondAuthorId = (String) authorResponse.get("authorID");
		assertEquals(firstAuthorId, secondAuthorId);

		String secondAuthorName = client.getAuthorName(secondAuthorId);

		assertNotEquals(firstAuthorName, secondAuthorName);

		new MockServerClient("localhost", 9001).reset();

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createAuthorIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.eAeCUAaCyAV4Z867\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getAuthorName?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("authorId", "a.eAeCUAaCyAV4Z867"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String thirdAuthorId = (String) authorResponse.get("authorID");
		assertEquals(secondAuthorId, thirdAuthorId);
		String thirdAuthorName = client.getAuthorName(thirdAuthorId);

		assertEquals(secondAuthorName, thirdAuthorName);
	}

	@Test
	public void create_and_delete_session() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createGroupIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.67oHR6WpvDYEF1N8\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createAuthorIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.eAeCUAaCyAV4Z867\"}}"));

		String authorMapper = "username";
		String groupMapper = "groupname";

		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
		String groupId = (String) groupResponse.get("groupID");
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper,
				"integration-author-1");
		String authorId = (String) authorResponse.get("authorID");

		int sessionDuration = 8;
		long inNHours = ((new Date()).getTime()
				+ (sessionDuration * 60L * 60L * 1000L)) / 1000L;

		new MockServerClient("localhost", 9001)
				.when(HttpRequest.request().withMethod("POST")
						.withPath("/api/1.2.13/createSession?"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.10b2e0760c5064e8f441235c67ae8138\"}}"));

		Map sessionResponse = client.createSession(groupId, authorId,
				sessionDuration);
		String firstSessionId = (String) sessionResponse.get("sessionID");

		Calendar oneYearFromNow = Calendar.getInstance();
		oneYearFromNow.add(Calendar.YEAR, 1);
		Date sessionValidUntil = oneYearFromNow.getTime();

		long seconds = sessionValidUntil.getTime() / 1000L;

		new MockServerClient("localhost", 9001).reset();

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createSession?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.67oHR6WpvDYEF1N8&validUntil="
								+ Long.toString(seconds)
								+ "&authorID=a.eAeCUAaCyAV4Z867"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.33b7552c4f74668039dc4dace9ec169b\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getSessionInfo?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("sessionId",
						"s.33b7552c4f74668039dc4dace9ec169b"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.67oHR6WpvDYEF1N8\",\"authorID\":\"a.eAeCUAaCyAV4Z867\",\"validUntil\":"
								+ seconds + "}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/listSessionsOfGroup?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("groupId", "g.67oHR6WpvDYEF1N8"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.10b2e0760c5064e8f441235c67ae8138\":{\"groupID\":\"g.67oHR6WpvDYEF1N8\",\"authorID\":\"a.eAeCUAaCyAV4Z867\",\"validUntil\":\""
								+ Long.toString(inNHours)
								+ "\"},\"s.33b7552c4f74668039dc4dace9ec169b\":{\"groupID\":\"g.67oHR6WpvDYEF1N8\",\"authorID\":\"a.eAeCUAaCyAV4Z867\",\"validUntil\":\""
								+ Long.toString(seconds) + "\"}}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/listSessionsOfAuthor?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("authorId", "a.eAeCUAaCyAV4Z867"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.10b2e0760c5064e8f441235c67ae8138\":{\"groupID\":\"g.67oHR6WpvDYEF1N8\",\"authorID\":\"a.eAeCUAaCyAV4Z867\",\"validUntil\":\""
								+ Long.toString(inNHours)
								+ "\"},\"s.33b7552c4f74668039dc4dace9ec169b\":{\"groupID\":\"g.67oHR6WpvDYEF1N8\",\"authorID\":\"a.eAeCUAaCyAV4Z867\",\"validUntil\":\""
								+ Long.toString(seconds) + "\"}}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deleteSession?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.10b2e0760c5064e8f441235c67ae8138"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deleteSession?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.33b7552c4f74668039dc4dace9ec169b"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		sessionResponse = client.createSession(groupId, authorId,
				sessionValidUntil);
		String secondSessionId = (String) sessionResponse.get("sessionID");
		try {
			assertNotEquals(firstSessionId, secondSessionId);

			Map sessionInfo = client.getSessionInfo(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			assertEquals(authorId, sessionInfo.get("authorID"));
			assertEquals(sessionValidUntil.getTime() / 1000L,
					(long) sessionInfo.get("validUntil"));

			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));

			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
		} finally {
			client.deleteSession(firstSessionId);
			client.deleteSession(secondSessionId);
		}

	}

	@Test
	public void create_pad_set_and_get_content() {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createPad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String padID = "integration-test-pad";
		client.createPad(padID);
		try {

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/setText?")
					.withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=g%C3%A5+%C3%A5+gj%C3%B8r+et+%C3%A6rend"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			String str = new String("gå å gjør et ærend\n");
			JSONObject odata = new JSONObject();
			odata.put("text", str);
			JSONObject o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", odata);
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getText?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(new String(o.toJSONString().getBytes(),
									"ISO-8859-1")));

			client.setText(padID, "gå å gjør et ærend");
			String text = (String) client.getText(padID).get("text");
			assertEquals("gå å gjør et ærend\n", text);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/setHTML?")
					.withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&html=%3C%21DOCTYPE+HTML%3E%3Chtml%3E%3Cbody%3E%3Cp%3Eg%C3%A5+og+gj%C3%B8re+et+%C3%A6rend+igjen%3C%2Fp%3E%3C%2Fbody%3E%3C%2Fhtml%3E"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			str = new String(
					"<!DOCTYPE HTML><html><body><p>g&#229; og gj&#248;re et &#230;rend igjen<br><br></body></html>");
			odata = new JSONObject();
			odata.put("html", str);
			o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", odata);
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getHTML?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(o.toJSONString()));

			client.setHTML(padID,
					"<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>");
			String html = (String) client.getHTML(padID).get("html");
			assertTrue(html, html.contains(
					"g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));

			new MockServerClient("localhost", 9001).reset();
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getHTML?")
					.withQueryStringParameter("rev", "2")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body><br></body></html>\"}}"));

			html = (String) client.getHTML(padID, 2).get("html");
			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getText?")
					.withQueryStringParameter("rev", "2")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"\\n\"}}"));

			text = (String) client.getText(padID, 2).get("text");
			assertEquals("\n", text);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET")
					.withPath("/api/1.2.13/getRevisionsCount?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"revisions\":3}}"));

			long revisionCount = (long) client.getRevisionsCount(padID)
					.get("revisions");
			assertEquals(3L, revisionCount);

			str = new String("Z:1>r|1+r$gå og gjøre et ærend igjen");
			o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", str);
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET")
					.withPath("/api/1.2.13/getRevisionChangeset?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(new String(o.toJSONString().getBytes(),
									"ISO-8859-1")));

			String revisionChangeset = client.getRevisionChangeset(padID);
			assertTrue(revisionChangeset,
					revisionChangeset.contains("gå og gjøre et ærend igjen"));

			str = new String("Z:j<i|1-j|1+1$\n");
			o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", str);
			new MockServerClient("localhost", 9001).reset();
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withQueryStringParameter("rev", "2").withMethod("GET")
					.withPath("/api/1.2.13/getRevisionChangeset?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(new String(o.toJSONString().getBytes(),
									"ISO-8859-1")));

			revisionChangeset = client.getRevisionChangeset(padID, 2);
			assertTrue(revisionChangeset,
					revisionChangeset.contains("|1-j|1+1$\n"));

			str = new String(
					"<style>\n.removed\"text-decoration: line-through; -ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)'; filter: alpha(opacity=80); opacity: 0.8; }\n</style><span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span><br><br>\",\"authors\":[\"\"]");
			odata = new JSONObject();
			odata.put("html", str);
			o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", odata);
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/createDiffHTML?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad")
					.withQueryStringParameter("startRev", "1")
					.withQueryStringParameter("endRev", "2"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(o.toJSONString()));

			String diffHTML = (String) client.createDiffHTML(padID, 1, 2)
					.get("html");
			assertTrue(diffHTML, diffHTML.contains(
					"<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/appendText?")
					.withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=lagt+til+n%C3%A5"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			str = new String("gå og gjøre et ærend igjen\nlagt til nå\n");
			odata = new JSONObject();
			odata.put("text", str);
			o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", odata);
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getText?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(new String(o.toJSONString().getBytes(),
									"ISO-8859-1")));

			client.appendText(padID, "lagt til nå");
			text = (String) client.getText(padID).get("text");
			assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getAttributePool?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"pool\":{\"numToAttrib\":{\"0\":[\"author\",\"\"],\"1\":[\"removed\",\"true\"]},\"attribToNum\":{\"author,\":0,\"removed,true\":1},\"nextNum\":2}}}"));

			Map attributePool = (Map) client.getAttributePool(padID)
					.get("pool");
			assertTrue(attributePool.containsKey("attribToNum"));
			assertTrue(attributePool.containsKey("nextNum"));
			assertTrue(attributePool.containsKey("numToAttrib"));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/saveRevision?")
					.withBody(
							"rev=2&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/saveRevision?")
					.withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.saveRevision(padID);
			client.saveRevision(padID, 2);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET")
					.withPath("/api/1.2.13/getSavedRevisionsCount?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":2}}"));

			long savedRevisionCount = (long) client
					.getSavedRevisionsCount(padID).get("savedRevisions");
			assertEquals(2L, savedRevisionCount);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET")
					.withPath("/api/1.2.13/listSavedRevisions?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":[2,4]}}"));

			List savedRevisions = (List) client.listSavedRevisions(padID)
					.get("savedRevisions");
			assertEquals(2, savedRevisions.size());
			assertEquals(2L, savedRevisions.get(0));
			assertEquals(4L, savedRevisions.get(1));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/padUsersCount?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsersCount\":0}}"));

			long padUsersCount = (long) client.padUsersCount(padID)
					.get("padUsersCount");
			assertEquals(0, padUsersCount);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/padUsers?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsers\":[]}}"));

			List padUsers = (List) client.padUsers(padID).get("padUsers");
			assertEquals(0, padUsers.size());

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getReadOnlyID?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"readOnlyID\":\"r.12f99615dae4f575584d19f2cf5a89ed\"}}"));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getPadID?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("roID",
							"r.12f99615dae4f575584d19f2cf5a89ed"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad\"}}"));

			String readOnlyId = (String) client.getReadOnlyID(padID)
					.get("readOnlyID");
			String padIdFromROId = (String) client.getPadID(readOnlyId)
					.get("padID");
			assertEquals(padID, padIdFromROId);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/listAuthorsOfPad?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorIDs\":[]}}"));

			List authorsOfPad = (List) client.listAuthorsOfPad(padID)
					.get("authorIDs");
			assertEquals(0, authorsOfPad.size());

			Long time = System.currentTimeMillis() / 1000L;

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getLastEdited?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID", "integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"lastEdited\":"
											+ Long.toString(time) + "}}"));

			long lastEditedTimeStamp = (long) client.getLastEdited(padID)
					.get("lastEdited");
			Calendar lastEdited = Calendar.getInstance();
			lastEdited.setTimeInMillis(lastEditedTimeStamp);
			Calendar now = Calendar.getInstance();
			assertTrue(lastEdited.before(now));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST")
					.withPath("/api/1.2.13/sendClientsMessage?").withBody(
							"msg=test+message&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{}}"));

			client.sendClientsMessage(padID, "test message");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/deletePad?")
					.withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deletePad(padID);
		}
	}

	@Test
	public void create_pad_move_and_copy() throws Exception {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createPad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=should+be+kept"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/copyPad?").withBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getText?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID", "integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/copyPad?").withBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-move-pad-move"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-move-pad-move\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getText?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID", "integration-move-pad-move"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/setText?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-move-pad-move&text=should+be+changed"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/copyPad?").withBody(
						"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		String padID = "integration-test-pad";
		String copyPadId = "integration-test-pad-copy";
		String movePadId = "integration-move-pad-move";
		String keep = "should be kept";
		String change = "should be changed";
		client.createPad(padID, keep);

		client.copyPad(padID, copyPadId);
		String copyPadText = (String) client.getText(copyPadId).get("text");
		client.movePad(padID, movePadId);
		String movePadText = (String) client.getText(movePadId).get("text");

		client.setText(movePadId, change);
		client.copyPad(movePadId, copyPadId, true);

		new MockServerClient("localhost", 9001).reset();

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getText?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID", "integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/movePad?").withBody(
						"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String copyPadTextForce = (String) client.getText(copyPadId)
				.get("text");
		client.movePad(movePadId, copyPadId, true);

		new MockServerClient("localhost", 9001).reset();

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/getText?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
				.withQueryStringParameter("padID", "integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));

		String movePadTextForce = (String) client.getText(copyPadId)
				.get("text");

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deletePad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deletePad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.deletePad(copyPadId);
		client.deletePad(padID);

		assertEquals(keep + "\n", copyPadText);
		assertEquals(keep + "\n", movePadText);

		assertEquals(change + "\n", copyPadTextForce);
		assertEquals(change + "\n", movePadTextForce);
	}

	@Test
	public void create_pads_and_list_them() throws InterruptedException {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createPad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createPad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("GET").withPath("/api/1.2.13/listAllPads?")
				.withQueryStringParameter("apikey",
						"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"integration-test-pad-1\",\"integration-test-pad-2\"]}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deletePad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/deletePad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String pad1 = "integration-test-pad-1";
		String pad2 = "integration-test-pad-2";
		client.createPad(pad1);
		client.createPad(pad2);
		Thread.sleep(100);
		List padIDs = (List) client.listAllPads().get("padIDs");
		client.deletePad(pad1);
		client.deletePad(pad2);

		assertTrue(String.format("Size was %d", padIDs.size()),
				padIDs.size() >= 2);
		assertTrue(padIDs.contains(pad1));
		assertTrue(padIDs.contains(pad2));
	}

	@Test
	public void create_pad_and_chat_about_it() {

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createAuthorIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=user1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.1fMwGcA2xP9Dex9e\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/api/1.2.13/createAuthorIfNotExistsFor?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=user2"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.2Hxa00d5XalpLbf1\"}}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/createPad?").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		new MockServerClient("localhost", 9001).when(HttpRequest.request()
				.withMethod("POST").withPath("/api/1.2.13/appendChatMessage?")
				.withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user1&authorID=a.1fMwGcA2xP9Dex9e"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String padID = "integration-test-pad-1";
		String user1 = "user1";
		String user2 = "user2";
		Map response = client.createAuthorIfNotExistsFor(user1,
				"integration-author-1");
		String author1Id = (String) response.get("authorID");
		response = client.createAuthorIfNotExistsFor(user2,
				"integration-author-2");
		String author2Id = (String) response.get("authorID");

		client.createPad(padID);
		try {
			Long time = System.currentTimeMillis() / 1000L;
			client.appendChatMessage(padID, "hi from user1", author1Id);

			Long time1 = System.currentTimeMillis() / 1000L;

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST")
					.withPath("/api/1.2.13/appendChatMessage?").withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user2&time="
									+ Long.toString(time1)
									+ "&authorID=a.2Hxa00d5XalpLbf1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.appendChatMessage(padID, "hi from user2", author2Id, time1);

			Long time2 = System.currentTimeMillis() / 1000L;

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST")
					.withPath("/api/1.2.13/appendChatMessage?").withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=g%C3%A5+%C3%A5+gj%C3%B8r+et+%C3%A6rend&time="
									+ Long.toString(time2)
									+ "&authorID=a.1fMwGcA2xP9Dex9e"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.appendChatMessage(padID, "gå å gjør et ærend", author1Id,
					time2);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getChatHead?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID",
							"integration-test-pad-1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getChatHistory?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("start", "0")
					.withQueryStringParameter("padID", "integration-test-pad-1")
					.withQueryStringParameter("end", "1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\\n\",\"userID\":\"a.1fMwGcA2xP9Dex9e\",\"time\":\""
											+ Long.toString(time)
											+ "\",\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userID\":\"a.2Hxa00d5XalpLbf1\",\"time\":\""
											+ Long.toString(time1)
											+ "\",\"userName\":\"integration-author-2\"}]}}"));

			String str = new String("gå å gjør et ærend");

			JSONObject omes = new JSONObject();
			omes.put("text", "hi from user1\\n");
			omes.put("userID", "a.1fMwGcA2xP9Dex9e");
			omes.put("time", Long.toString(time));
			omes.put("userName", "integration-author-1");

			JSONArray arr = new JSONArray();

			arr.add(omes);

			JSONObject omes2 = new JSONObject();
			omes2.put("text", "hi from user2\\n");
			omes2.put("userID", "a.2Hxa00d5XalpLbf1");
			omes2.put("time", Long.toString(time1));
			omes2.put("userName", "integration-author-2");

			arr.add(omes2);

			JSONObject omes3 = new JSONObject();
			omes3.put("text", str);
			omes3.put("userID", "a.1fMwGcA2xP9Dex9e");
			omes3.put("time", Long.toString(time2));
			omes3.put("userName", "integration-author-1");

			arr.add(omes3);

			JSONObject odata = new JSONObject();
			odata.put("messages", arr);

			JSONObject o = new JSONObject();
			o.put("code", 0);
			o.put("message", "ok");
			o.put("data", odata);

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("GET").withPath("/api/1.2.13/getChatHistory?")
					.withQueryStringParameter("apikey",
							"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
					.withQueryStringParameter("padID",
							"integration-test-pad-1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(new String(o.toJSONString().getBytes(),
									"ISO-8859-1")));

			response = client.getChatHead(padID);
			long chatHead = (long) response.get("chatHead");
			assertEquals(2, chatHead);

			response = client.getChatHistory(padID);
			List chatHistory = (List) response.get("messages");
			assertEquals(3, chatHistory.size());
			assertEquals("gå å gjør et ærend",
					((Map) chatHistory.get(2)).get("text"));

			response = client.getChatHistory(padID, 0, 1);
			chatHistory = (List) response.get("messages");
			assertEquals(2, chatHistory.size());
			assertEquals("hi from user2",
					((Map) chatHistory.get(1)).get("text"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			new MockServerClient("localhost", 9001).when(HttpRequest.request()
					.withMethod("POST").withPath("/api/1.2.13/deletePad?")
					.withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody(
									"{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deletePad(padID);
		}

	}
}
