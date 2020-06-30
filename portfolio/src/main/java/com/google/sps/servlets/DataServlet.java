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
import com.google.gson.Gson;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private ArrayList<String> OLD_FACTS;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String oldFactsJson = convertToJsonUsingGson(OLD_FACTS);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(oldFactsJson);
  }

  private String convertToJsonUsingGson(ArrayList<String> oldFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(oldFacts);
    return json;
  }

  // TODO: Edit doPost function
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String userFirstName = request.getParameter("first-name");
    String userLastName = request.getParameter("last-name");
    String userFunFact = request.getParameter("user-fun-fact");
    
    // Store user's input to access later - causing NullPointer Exception Error
    OLD_FACTS.add(userFirstName + " " + userLastName + ": " + userFunFact);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
