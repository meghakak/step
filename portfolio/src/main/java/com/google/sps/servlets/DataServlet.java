// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Only allow unique usernames - returning users should still be able to write additional comments with the same username
/** Servlet that returns inputted fun facts from users and stores previously inputted facts. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final String USER_NAME_KEY = "user-name";
  private static final String FUN_FACT_KEY = "user-fun-fact";
  private static final String NO_NAME = "Anonymous";
  private static final String ENTITY_NAME = "FunFact";
  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_FACT = "fact";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(ENTITY_NAME).addSort(PROPERTY_FACT, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Add previously inputted fun facts to userFacts
    Map<String, String> userFacts = new HashMap<String, String>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty(PROPERTY_NAME);
      String fact = (String) entity.getProperty(PROPERTY_FACT);

      if (userFacts.containsKey(name)) {
        String newValue = userFacts.get(name) + "; " + fact;
        userFacts.remove(name);
        userFacts.put(name, newValue);
      }
      else {
        userFacts.put(name, fact);
      }
    }
    String userFactsJson = convertToJsonUsingGson(userFacts);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(userFactsJson);
  }

  private static String convertToJsonUsingGson(Map<String, String> oldFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(oldFacts);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String userName = getParameter(request, /*content=*/ USER_NAME_KEY, /*defaultValue=*/ "");
    String userFunFact = getParameter(request, /*content=*/ FUN_FACT_KEY, /*defaultValue=*/ "");

    // No need to change oldFacts if there is no new fact
    if (userFunFact.isEmpty()) {
      response.sendRedirect("/index.html");
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Define key to add to datastore
    String factKey = getFactKey(userName);

    Entity funFactEntity = new Entity(ENTITY_NAME);
    funFactEntity.setProperty(PROPERTY_NAME, factKey);
    funFactEntity.setProperty(PROPERTY_FACT, userFunFact);
    datastore.put(funFactEntity);

    response.sendRedirect("/index.html");
  }

  private static String getFactKey(String userName) {
    String factKey;
    if (userName.isEmpty()) {
      factKey = NO_NAME;
    }
    else {
      factKey = userName;
    }
    return factKey;
  }

  private static String getParameter(HttpServletRequest request, String content, String defaultValue) {
    String value = request.getParameter(content);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }
}