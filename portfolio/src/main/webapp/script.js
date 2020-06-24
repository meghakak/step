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
