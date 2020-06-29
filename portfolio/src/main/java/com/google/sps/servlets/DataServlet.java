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
  private static final ImmutableList FRUIT_FACTS = ImmutableList.of("A strawberry is not an actual berry", 
  "Kiwis contain more Vitamin C than oranges", "The pineapple is actually a berry");

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String fruitFactsJson = convertToJsonUsingGson(FRUIT_FACTS);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(fruitFactsJson);
  }

  private String convertToJsonUsingGson(ImmutableList fruitFacts) {
    Gson gson = new Gson();
    String json = gson.toJson(fruitFacts);
    return json;
  }
}
