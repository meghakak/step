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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
 
public final class FindMeetingQuery {
  private static final int MINUTES_IN_A_DAY = 1440;
  private int nextAvailableStart = TimeRange.START_OF_DAY;
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
     
    // Cannot support events longer than 24 hours (1440 minutes)
    if((request.getDuration()) > MINUTES_IN_A_DAY) {
      return ImmutableList.of();
    }
 
    ImmutableSet<String> requiredAttendees = ImmutableSet.copyOf(request.getAttendees());
 
    // Get time ranges from events that include at least one required attendee
    ImmutableList<TimeRange> unavailableTimes = 
        Streams.stream(events)
            .filter(event -> !Collections.disjoint(requiredAttendees, event.getAttendees()))
            .map(Event::getWhen)
            .collect(toImmutableList());
 
    // No unavailable time ranges means the entire day is available
    if (unavailableTimes.isEmpty()) {
      return ImmutableList.of(TimeRange.WHOLE_DAY);
    }

    // TODO: Change structure to not update a class variable in the stream
    // Find all available time ranges for the requested meeting based on the attendees' unavailable times 
    List<TimeRange> availableTimes = 
        Streams.stream(unavailableTimes)
            .map(currentUnavailableTime -> {

                // Find the next available time range given an unavailable time range
                TimeRange timeRangeAvailable = findAvailableTimeRange(currentUnavailableTime, nextAvailableStart, request);

                // Store the next available start time for the next available time range
                if(currentUnavailableTime.end() > nextAvailableStart) {
                  nextAvailableStart = currentUnavailableTime.end();
                }
                
                return timeRangeAvailable;
            })
            .filter(currentUnavailableTime -> currentUnavailableTime!=null)
            .collect(Collectors.toList());

    // Add time leftover as the final available time range
    if(nextAvailableStart < TimeRange.END_OF_DAY) {
      availableTimes.add(TimeRange.fromStartEnd(nextAvailableStart, TimeRange.END_OF_DAY, true));
    }
 
    return availableTimes;
  }

  private static TimeRange findAvailableTimeRange(TimeRange currentUnavailableTime, int nextAvailableStart, MeetingRequest request) {
    TimeRange timeRangeAvailable;

    // Add time range only if it can fit the requsted duration
    int timeDifference = currentUnavailableTime.start() - nextAvailableStart;
    if (timeDifference >= request.getDuration()) {
      timeRangeAvailable = TimeRange.fromStartEnd(nextAvailableStart, currentUnavailableTime.start(), false);
    }
    else {
      timeRangeAvailable = null;
    }

    return timeRangeAvailable;
  }
}