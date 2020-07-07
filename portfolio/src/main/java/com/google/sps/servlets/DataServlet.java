package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that returns inputted fun facts from users and stores previously inputted facts. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final String FUN_FACT_KEY = "user-fun-fact";
  private static final String ENTITY_NAME = "FunFact";
  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_FACT = "fact";
  private static final String NO_NAME = "Anonymous";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(ENTITY_NAME).addSort(PROPERTY_FACT, SortDirection.DESCENDING);

    int commentsLimit = Integer.parseInt(getParameter(request, "limit", "100"));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Add previously inputted fun facts to userFacts
    ImmutableList<String> userFacts =
        Streams.stream(results.asIterable())
            .limit(commentsLimit)
            .map(DataServlet::getContent)
            .collect(toImmutableList());

    String userFactsJson = convertToJsonUsingGson(userFacts);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(userFactsJson);
  }

  private static String getContent(Entity entity) {
    return entity.getProperty(PROPERTY_NAME).toString() + ": " + entity.getProperty(PROPERTY_FACT).toString();
  }

  private static String convertToJsonUsingGson(ImmutableList<String> oldFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(oldFacts);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get input from the webpage
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String username = userEmail.substring(0, userEmail.indexOf("@"));
    String userFunFact = getParameter(request, /*content=*/ FUN_FACT_KEY, /*defaultValue=*/ "");

    // No need to add to datastore if there is no new fact
    if (userFunFact.isEmpty()) {
      response.sendRedirect("/about-you.html");
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Define key to add to datastore
    String factKey = username.isEmpty() ? NO_NAME : username;

    Entity funFactEntity = new Entity(ENTITY_NAME);
    funFactEntity.setProperty(PROPERTY_NAME, factKey);
    funFactEntity.setProperty(PROPERTY_FACT, userFunFact);
    datastore.put(funFactEntity);

    response.sendRedirect("/about-you.html");
  }

  private static String getParameter(HttpServletRequest request, String content, String defaultValue) {
    String value = request.getParameter(content);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }
}