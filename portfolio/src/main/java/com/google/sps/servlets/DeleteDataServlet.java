package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for deleting comments. */
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {
  private static final String ENTITY_NAME = "FunFact";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(ENTITY_NAME);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Delete all inputted comments in datastore
    ImmutableList<Key> userFacts =
        Streams.stream(results.asIterable()).map(Entity::getKey).collect(toImmutableList());

    datastore.delete(userFacts);
    response.sendRedirect("/about-you.html");
  }
}
