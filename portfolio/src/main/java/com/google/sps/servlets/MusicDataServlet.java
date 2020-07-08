package com.google.sps.servlets;

import static com.google.appengine.api.urlfetch.FetchOptions.Builder;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.google.common.collect.Streams;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/music-data")
public class MusicDataServlet extends HttpServlet {
  private static String ENTITY_NAME = "FavGenre";
  private static String PROPERTY_GENRE = "genre";
  private static String PROPERTY_VOTES = "votes";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Query query = new Query(ENTITY_NAME);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Add previously inputted fun facts to userFacts
    ImmutableMap<String, Integer> genreVotes =
        Streams.stream(results.asIterable())
            .collect(toImmutableMap(entity -> entity.getProperty(PROPERTY_GENRE).toString(), entity -> Integer.parseInt(entity.getProperty(PROPERTY_VOTES).toString())));

    Gson gson = new Gson();
    String json = gson.toJson(genreVotes);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String genre = request.getParameter("fav-genre");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query(ENTITY_NAME);
    q.setFilter(new FilterPredicate(PROPERTY_GENRE, FilterOperator.EQUAL, genre));
    List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    Entity genreEntity = new Entity(ENTITY_NAME);
    if (entities.isEmpty()) {
      genreEntity.setProperty(PROPERTY_GENRE, genre);
      genreEntity.setProperty(PROPERTY_VOTES, 1);
    }
    else {
      genreEntity = entities.get(0);
      genreEntity.setProperty(PROPERTY_VOTES, Integer.parseInt(genreEntity.getProperty(PROPERTY_VOTES).toString())+1);
    }
    datastore.put(genreEntity);

    response.sendRedirect("/about-you.html");
  }
}