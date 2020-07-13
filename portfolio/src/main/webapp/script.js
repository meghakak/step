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

/** jQuery - Allow access of navbar.html to other files. */
$(document).ready(function() {
  $('#top-navigation').load('navbar.html');
});

function addRandomFunFact() {
  const funFacts =
      ['I am a Black Belt in Mixed Martial Arts 🥋', 
      'I am the Internal Vice President of Women in Information' +
      ' and Computer Sciences at UCI 👩💻',
      'I played trumpet in my high school marching band 🎺', 
      'I prefer dogs over cats, but both are equally lovable and adorable! 🐶🐱'];

  // Pick a random fun fact.
  const funFact = funFacts[Math.floor(Math.random() * funFacts.length)];

  // Add it to the page.
  var funFactContainer = document.getElementById('fun-fact-container');
  funFactContainer.innerText = funFact;
}

/** Accordion / collapsible template from https://www.w3schools.com/howto/howto_js_accordion.asp */

// jQuery - Initialize accordion / collapsible
if($('body').is('.blog')) {
  showAccordion();
}

/** Display accordion / collapsible */
function showAccordion() {
  var accordion = document.getElementsByClassName('accordion');

  // Open chosen panel(s) and hide all other panels
  for (const accordionIndex = 0; accordionIndex < accordion.length; accordionIndex++) {
    accordion[accordionIndex].addEventListener('click', function() {
      this.classList.toggle('panel-active');
      var panel = this.nextElementSibling;
      panel.style.maxHeight = panel.style.maxHeight ? null : panel.scrollHeight + 'px';
    });
  }
}

/** Slideshow template from https://www.w3schools.com/howto/howto_js_slideshow.asp */

// jQuery - Initialize slideshow
if($('body').is('.gallery')) {
  var slideIndex = 1;
  showSlides(slideIndex);
}

/** Slideshow - add next and previous controls */
function plusSlides(nextOrPrev) {
  showSlides(slideIndex += nextOrPrev);
}

/** Slideshow - display image */
function currentSlide(index) {
  showSlides(slideIndex = index);
}

/** Slideshow - display content */
function showSlides(index) {
  var slides = document.getElementsByClassName('slides');
  var dots = document.getElementsByClassName('dot');

  // Handle start and end of slideshow cases
  if (index > slides.length) {
    slideIndex = 1;
  }
  if (index < 1) {
    slideIndex = slides.length;
  }

  // Hide all slides except the current one
  for (var slideListIndex = 0; slideListIndex < slides.length; slideListIndex++) {
    slides[slideListIndex].style.display = 'none';
  }
  for (var dotIndex = 0; dotIndex < dots.length; dotIndex++) {
    dots[dotIndex].className = dots[dotIndex].className.replace(' dot-active','');
  }
  slides[slideIndex-1].style.display = 'block';
  dots[slideIndex-1].className += ' dot-active';
}

/** Fetch user's facts and add them to the DOM. */
function getUserFacts() {
  fetch('/data').then(response => response.json()).then((oldFacts) => {  
    // Reference each element in oldFacts to create HTML content
    const limit = parseInt(document.getElementById('limit').value);
    var factsListElement = document.getElementById('user-facts-container');
    factsListElement.innerHTML = '';
    for(const key in oldFacts) {
      if (oldFacts.hasOwnProperty(key) && key < limit) {
        factsListElement.appendChild(createListElement(oldFacts[key]));
      }
    }
  });
}

/** Create an <li> element containing text. */
function createListElement(text) {
  var liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/** Delete all comments if user selects delete option */
async function deleteComments() {
  await fetch('/delete-data', {method: 'POST'});
  getUserFacts();
}

/** Fetch login status and show or hide form accordingly */
function showOrHideForm() {
  fetch('/auth').then(response => response.text()).then((loginStatus) => {
    var authContent = document.getElementById('servlet-content');
    if (loginStatus.includes('Logout')) {
      authContent.innerHTML += loginStatus;
      getUserFacts();
    }
    else {
      authContent.innerHTML = loginStatus;
    }
  })
}

google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Fetch music data and use it to create a chart */
function drawChart() {
  fetch('/music-data').then(response => response.json())
    .then((genreVotes) => {
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Genre');
      data.addColumn('number', 'Votes');
      Object.keys(genreVotes).forEach((genre) => {
        data.addRow([genre, genreVotes[genre]]);
      });

      const options = {
        'title': 'Favorite Genres of Music',
        'width': 600,
        'height': 500
      };

      var chart = new google.visualization.ColumnChart(
          document.getElementById('chart'));
      chart.draw(data, options);
    });
}

/** Print song list according to which genre is selected */
function getSongList(genre) {
  const songs =
      {'Rock': 'Last Hope - Paramore<br>Alone Together - Fall Out Boy<br>Babylon - ' +
      '5 Seconds of Summer<br>Sk8ter Boi - Avril Lavigne<br>Tear in My Heart - Twenty One Pilots', 
      'Pop': 'Temporary Heart - PRETTYMUCH<br>In Your Eyes - The Weeknd<br>This Is What It Takes - ' +
      'Shawn Mendes<br>Real Friends - Camila Cabello ft. Swae Lee<br>2002 - Anne-Marie',
      'R&B': 'Everything - Ella Mai ft. John Legend<br>U - H.E.R.<br>What You Did - ' + 
      'Mahalia ft. Ella Mai<br>Let Me Love You - Mario<br>Summertime Magic - Childish Gambino', 
      'EDM': 'Good Things Fall Apart - ILLENIUM ft. Jon Bellion<br>More Than You Know - Axwell & Ingrosso' +
      '<br>Middle - DJ Snake ft. Bipolar Sunsine<br>Call On Me - Starley ft. Ryan Riback<br>' + 
      'Paris - The Chainsmokers',
      'Rap': 'A Tale of 2 Citiez - J. Cole<br>From Time - Drake ft. Jhene Aiko<br>Gold - BROCKHAMPTON<br>' + 
      'From Florida With Love - Drake<br>Pretty Little Fears - 6LACK ft. J. Cole'};
  var songsContent = document.getElementById('music-container');
  songsContent.innerHTML = songs[genre];
}