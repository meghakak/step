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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private ArrayList<String> fruitFacts;

  @Override
  public void init() {
    fruitFacts = new ArrayList<>();
    fruitFacts.add("A strawberry is not an actual berry");
    fruitFacts.add("Kiwis contain more Vitamin C than oranges");
    fruitFacts.add("The pineapple is actually a berry");    
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Convert the fruitFacts ArrayList to JSON
    String json = convertToJsonUsingGson(fruitFacts);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /** Convert an ArrayList instance into a JSON string using the Gson library. */
  private String convertToJsonUsingGson(ArrayList<String> fruitFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(fruitFacts);
    return json;
  }
}
