package com.google.sps.servlets;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import java.io.IOException;
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
    Query query = new Query(ENTITY_NAME);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Add datastore entities with votes for each genre
    ImmutableMap<String, Integer> genreVotes =
        Streams.stream(results.asIterable())
            .collect(
                toImmutableMap(MusicDataServlet::getEntityKey, MusicDataServlet::getEntityValue));

    String genreVotesJson = convertToJsonUsingGson(genreVotes);
    response.setContentType("application/json");
    response.getWriter().println(genreVotesJson);
  }

  private static String getEntityKey(Entity entity) {
    return entity.getProperty(PROPERTY_GENRE).toString();
  }

  private static Integer getEntityValue(Entity entity) {
    return Integer.parseInt(entity.getProperty(PROPERTY_VOTES).toString());
  }

  private static String convertToJsonUsingGson(ImmutableMap<String, Integer> genreVotes) {
    Gson gson = new Gson();
    String json = gson.toJson(genreVotes);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String genre = request.getParameter("fav-genre");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Get the entity that includes the selected genre
    FilterPredicate filter = new FilterPredicate(PROPERTY_GENRE, FilterOperator.EQUAL, genre);
    Query query = new Query(ENTITY_NAME).setFilter(filter);
    PreparedQuery results = datastore.prepare(query);

    ImmutableList<Entity> entities = ImmutableList.copyOf(results.asIterable());

    datastore.put(getGenreEntity(entities, genre));

    response.sendRedirect("/about-you.html");
  }

  private static Entity getGenreEntity(ImmutableList<Entity> entities, String genre) {
    Entity genreEntity = new Entity(ENTITY_NAME);
    if (entities.isEmpty()) {
      genreEntity.setProperty(PROPERTY_GENRE, genre);
      genreEntity.setProperty(PROPERTY_VOTES, 1);
    } else {
      genreEntity = entities.get(0);
      int numVotes = Integer.parseInt(genreEntity.getProperty(PROPERTY_VOTES).toString()) + 1;
      genreEntity.setProperty(PROPERTY_VOTES, numVotes);
    }
    return genreEntity;
  }
}
