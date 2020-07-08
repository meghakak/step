package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/music-data")
public class MusicDataServlet extends HttpServlet {

  private Map<String, Integer> genreVotes = new HashMap<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(genreVotes);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String genre = request.getParameter("fav-genre");
    int currentVotes = genreVotes.containsKey(genre) ? genreVotes.get(genre) : 0;
    genreVotes.put(genre, currentVotes + 1);

    response.sendRedirect("/about-you.html");
  }
}