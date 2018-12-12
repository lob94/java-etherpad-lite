package net.gjerull.etherpad.client;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.net.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * Integration test for simple App.
 */

@RunWith(JUnitQuickcheck.class)
public class EPLiteClientIntegrationQuickCheckTest {
    private EPLiteClient client;
    private ClientAndServer mockServer;

    /**
     * Useless testing as it depends on a specific API key
     *
     * TODO: Find a way to make it configurable
     */
    @Before
    public void setUp() throws Exception {
        this.client = new EPLiteClient(
                "http://localhost:9001",
                "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"
        );
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger("org.mockserver.mock"))
       .setLevel(ch.qos.logback.classic.Level.OFF);
        
        mockServer = startClientAndServer(9001);
    }

    @After
    public void endServer() {
    	mockServer.close();
    }

    @Property public void create_author(String authorName) throws Exception {
    	
    	new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("POST").withPath("/api/1.2.13/createAuthor?")
    			.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name="+URLEncoder.encode((String) authorName, "UTF-8"))
    			).respond(HttpResponse.response().withStatusCode(200)
    					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qM9H63JcknRhdMl1\"}}"));
    	
    	
        JSONObject o = new JSONObject();
        o.put("code", 0);
        o.put("message", "ok");
        o.put("data", authorName);
    	
    	String response = "{\"code\":0,\"message\":\"ok\",\"data\":\""+authorName+"\"}";
    	new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("GET").withPath("/api/1.2.13/getAuthorName?")
    			.withQueryStringParameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
    			.withQueryStringParameter("authorId","a.qM9H63JcknRhdMl1")
    			).respond(HttpResponse.response().withStatusCode(200).withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
    					.withBody(o.toJSONString()));
    					
    	if(!authorName.isEmpty()) {
        Map authorResponse = client.createAuthor(authorName);
        String authorId = (String) authorResponse.get("authorID");
        assertTrue(authorId != null && !authorId.isEmpty());
        
        String getAuthorName = client.getAuthorName(authorId);
        assertEquals(authorName, getAuthorName);
    	}
    }
    
    @Property public void create_pad_with_random_text(String keep) throws UnsupportedEncodingException{
    	
    	new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("POST").withPath("/api/1.2.13/createPad?")
    			.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text="+URLEncoder.encode((String) keep, "UTF-8"))
    			).respond(HttpResponse.response().withStatusCode(200)
    					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
    	
    	String str = keep;
        JSONObject odata = new JSONObject();
        odata.put("text", str);
        JSONObject o = new JSONObject();
        o.put("code", 0);
        o.put("message", "ok");
        o.put("data", odata);
        new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("GET").withPath("/api/1.2.13/getText?")
    			.withQueryStringParameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
    			.withQueryStringParameter("padID", "integration-test-pad")
    			).respond(HttpResponse.response().withStatusCode(200).withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
    					.withBody(o.toJSONString()));
    	
        String padID = "integration-test-pad";
        String copyPadId = "integration-test-pad-copy";
        String movePadId = "integration-move-pad-move";
        client.createPad(padID, keep);
        
        String text = (String) client.getText(padID).get("text");
        assertEquals(keep, text);
    }
    
    @Property public void chat_whit_random_messages(String message) throws UnsupportedEncodingException {
    	
    	new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor?")
    			.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=user1")
    			).respond(HttpResponse.response().withStatusCode(200)
    					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.1fMwGcA2xP9Dex9e\"}}"));
    	
    	new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("POST").withPath("/api/1.2.13/createPad?")
    			.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")
    			).respond(HttpResponse.response().withStatusCode(200)
    					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
    	
    	new MockServerClient("localhost",9001).when(HttpRequest.request()
    			.withMethod("POST").withPath("/api/1.2.13/appendChatMessage?")
    			.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text="+URLEncoder.encode((String) message, "UTF-8")+"&authorID=a.1fMwGcA2xP9Dex9e")
    			).respond(HttpResponse.response().withStatusCode(200)
    					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
    	
        String padID = "integration-test-pad-1";
        String user = "user1";
        Map response = client.createAuthorIfNotExistsFor(user, "integration-author-1");
        String authorId = (String) response.get("authorID");

        client.createPad(padID);
        Long time = System.currentTimeMillis() / 1000L;
        client.appendChatMessage(padID, message, authorId);
        
        JSONObject omes = new JSONObject();
        omes.put("text", message);
        omes.put("userID", "a.1fMwGcA2xP9Dex9e");
        omes.put("time", Long.toString(time));
        omes.put("userName", "integration-author-1");
            
        JSONArray arr = new JSONArray();
            
        arr.add(omes);
            
        JSONObject odata = new JSONObject();
        odata.put("messages", arr);
            
            
        JSONObject o = new JSONObject();
        o.put("code", 0);
        o.put("message", "ok");
        o.put("data", odata);
            
        new MockServerClient("localhost",9001).when(HttpRequest.request()
        			.withMethod("GET").withPath("/api/1.2.13/getChatHistory?")
        			.withQueryStringParameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")
        			.withQueryStringParameter("padID", "integration-test-pad-1")
        			).respond(HttpResponse.response().withStatusCode(200).withHeaders(
                            new Header("Content-Type", "application/json; charset=utf-8"),
                            new Header("Cache-Control", "public, max-age=86400"))
        					.withBody(o.toJSONString()));

        response = client.getChatHistory(padID);
        List chatHistory = (List) response.get("messages");
        assertEquals(1, chatHistory.size());
        assertEquals(message, ((Map)chatHistory.get(0)).get("text"));
        	
        new MockServerClient("localhost",9001).when(HttpRequest.request()
        			.withMethod("POST").withPath("/api/1.2.13/deletePad?")
        			.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1")
        			).respond(HttpResponse.response().withStatusCode(200)
        					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
        	
        client.deletePad(padID);
   }

}
