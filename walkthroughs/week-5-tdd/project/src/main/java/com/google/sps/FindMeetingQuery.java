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
 
package com.google.sps;
 
import static com.google.common.collect.ImmutableList.toImmutableList;
 
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
 
public final class FindMeetingQuery {
  private int nextAvailableStart = TimeRange.START_OF_DAY;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> timesAvailable = new ArrayList<TimeRange>();
 
    // Cannot support events longer than 24 hours (1440 minutes)
    if((request.getDuration()) > 1440) {
      return timesAvailable;
    }
 
    ImmutableSet<String> requiredAttendees = ImmutableSet.copyOf(request.getAttendees());
 
    // Get time ranges from events that include required attendees
    ImmutableList<TimeRange> timesUnavailable = 
        Streams.stream(events)
            .filter(event -> !Collections.disjoint(requiredAttendees, event.getAttendees()))
            .map(Event::getWhen)
            .collect(toImmutableList());
 
    // No times unavailable means the entire day is available
    if (timesUnavailable.isEmpty()) {
      timesAvailable.add(TimeRange.WHOLE_DAY);
      return timesAvailable;
    }

    timesAvailable = 
        Streams.stream(timesUnavailable)
            .map(currentTimeRange -> {
                HashMap<TimeRange, Integer> content = findAvailableTimeRange(currentTimeRange, nextAvailableStart, request);
                TimeRange timeAvailable = content.entrySet().iterator().next().getKey();
                nextAvailableStart = content.get(timeAvailable);
                return timeAvailable;
            })
            .filter(currentTimeRange -> currentTimeRange!=null)
            .collect(Collectors.toList());
 
    if(nextAvailableStart < TimeRange.END_OF_DAY) {
      timesAvailable.add(TimeRange.fromStartEnd(nextAvailableStart, TimeRange.END_OF_DAY, true));
    }
 
    return timesAvailable;
  }

  private static HashMap<TimeRange, Integer> findAvailableTimeRange(TimeRange currentTimeRange, int nextAvailableStart, MeetingRequest request) {
    HashMap<TimeRange, Integer> content = new HashMap<TimeRange, Integer>();
    TimeRange timeRangeAvailable = null;
    int timeDiff = currentTimeRange.start() - nextAvailableStart;
    if (timeDiff >= 0 && timeDiff >= request.getDuration()) {
      timeRangeAvailable = TimeRange.fromStartEnd(nextAvailableStart, currentTimeRange.start(), false);
    }

    if(currentTimeRange.end() > nextAvailableStart) {
      nextAvailableStart = currentTimeRange.end();
    }
    content.put(timeRangeAvailable, nextAvailableStart);
    return content;
  }
}