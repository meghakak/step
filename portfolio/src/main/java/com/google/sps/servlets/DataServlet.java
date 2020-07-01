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

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns inputted fun facts from users and stores previously inputted facts. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final String FIRST_NAME_KEY = "first-name";
  private static final String LAST_NAME_KEY = "last-name";
  private static final String FUN_FACT_KEY = "user-fun-fact";
  private static final String NO_NAME = "Anonymous";
  private Map<String, String> oldFacts = new HashMap<String, String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String oldFactsJson = convertToJsonUsingGson(oldFacts);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(oldFactsJson);
  }

  private static String convertToJsonUsingGson(Map<String, String> oldFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(oldFacts);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String userFirstName = getParameter(request, /*content=*/ FIRST_NAME_KEY, /*defaultValue=*/ "");
    String userLastName = getParameter(request, /*content=*/ LAST_NAME_KEY, /*defaultValue=*/ "");
    String userFunFact = getParameter(request, /*content=*/ FUN_FACT_KEY, /*defaultValue=*/ "");

    // No need to change oldFacts if there is no new fact
    if (userFunFact.isEmpty()) {
      response.sendRedirect("/index.html");
      return;
    }

    // Define key and value pair to add to oldFacts
    String factKey = getFactKey(userFirstName, userLastName);
    String factValue = getFactValue(oldFacts, userFunFact, factKey);
    
    // Store user's input to access later
    oldFacts.put(factKey, factValue);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
    response.getWriter().println(factKey + ": " + factValue);
  }

  private static String getFactKey(String firstName, String lastName) {
    String factKey;
    if (firstName.isEmpty() && lastName.isEmpty()) {
      factKey = NO_NAME;
    }
    else if (firstName.isEmpty()) {
      factKey = lastName;
    }
    else if (lastName.isEmpty()) {
      factKey = firstName;
    }
    else {
      factKey = firstName + " " + lastName;
    }
    return factKey;
  }

  private static String getFactValue(Map<String, String> oldFacts, String funFact, String factKey) {
    String factValue;
    if (oldFacts.containsKey(factKey)) {
      factValue = oldFacts.get(factKey) + ", " + funFact;
    }
    else {
      factValue = funFact;
    }
    return factValue;
  }

  private static String getParameter(HttpServletRequest request, String content, String defaultValue) {
    String value = request.getParameter(content);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }
}