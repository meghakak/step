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
  const funFactContainer = document.getElementById('fun-fact-container');
  funFactContainer.innerText = funFact;
}

/** Slideshow template from https://www.w3schools.com/howto/howto_js_slideshow.asp */

// Initialize slideshow
var slideIndex = 1;
showSlides(slideIndex);

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
  var slides = document.getElementsByClassName("slides");
  var dots = document.getElementsByClassName("dot");

  // Handle start and end of slideshow cases
  if (index > slides.length) {
    slideIndex = 1;
  }
  if (index < 1) {
    slideIndex = slides.length;
  }

  // Hide all slides except the current one
  for (var slideListIndex = 0; slideListIndex < slides.length; slideListIndex++) {
    slides[slideListIndex].style.display = "none";
  }
  for (var dotIndex = 0; dotIndex < dots.length; dotIndex++) {
    dots[dotIndex].className = dots[dotIndex].className.replace(" dot-active", "");
  }
  slides[slideIndex-1].style.display = "block";
  dots[slideIndex-1].className += " dot-active";
}