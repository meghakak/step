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

import java.util.*;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private HashMap<String, String> OLD_FACTS = new HashMap<String, String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String oldFactsJson = convertToJsonUsingGson(OLD_FACTS);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(oldFactsJson);
  }

  private String convertToJsonUsingGson(HashMap<String, String> oldFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(oldFacts);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String userFirstName = getParameter(request, "first-name", "");
    String userLastName = getParameter(request, "last-name", "");
    String userFunFact = getParameter(request, "user-fun-fact", "");

    // Define key and value pair to add to OLD_FACTS
    String factKey;
    if (userFirstName.isEmpty() && userLastName.isEmpty()) {
      factKey = "Anonymous";
    }
    else if (userFirstName.isEmpty()) {
      factKey = userLastName;
    }
    else if (userLastName.isEmpty()) {
      factKey = userFirstName;
    }
    else {
      factKey = userFirstName + " " + userLastName;
    }
    String factValue = "";
    if (!(userFunFact.isEmpty())) {
      if (OLD_FACTS.containsKey(factKey)) {
        factValue = OLD_FACTS.get(factKey) + ", " + userFunFact;
        OLD_FACTS.remove(factKey);
      }
      else {
        factValue = userFunFact;
      }
    }
    
    // Store user's input to access later
    if (!(userFunFact.isEmpty())) {
      OLD_FACTS.put(factKey, factValue);
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
    response.getWriter().println(factKey + ": " + factValue);
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String content, String defaultValue) {
    String value = request.getParameter(content);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }
}