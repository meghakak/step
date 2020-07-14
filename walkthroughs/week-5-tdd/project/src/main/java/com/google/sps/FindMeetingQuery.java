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
import com.google.common.collect.Streams;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
 
public final class FindMeetingQuery {
  private static final int MINUTES_IN_A_DAY = 1440;
  private int nextAvailableStart = TimeRange.START_OF_DAY;
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
     
    // Cannot support events longer than 24 hours (1440 minutes)
    if(request.getDuration() > MINUTES_IN_A_DAY) {
      return ImmutableList.of();
    }
 
    ImmutableList<String> requiredAttendees = ImmutableList.copyOf(request.getAttendees());
    ImmutableList<String> optionalAttendees = ImmutableList.copyOf(request.getOptionalAttendees());
 
    // Get required and optional unavailable time ranges
    ImmutableList<TimeRange> unavailableTimes = getUnavailableTimeRanges(events, requiredAttendees);
    ImmutableList<TimeRange> optionalUnavailableTimes = getUnavailableTimeRanges(events, optionalAttendees);
 
    // No unavailable time ranges means the entire day is available
    if (unavailableTimes.isEmpty() && optionalUnavailableTimes.isEmpty()) {
      return ImmutableList.of(TimeRange.WHOLE_DAY);
    }

    // Find all available time ranges for the requested meeting based on the attendees' unavailable times 
    ImmutableList<TimeRange> availableTimes = ImmutableList.copyOf(getAvailableTimeRanges(unavailableTimes, request));
    nextAvailableStart = TimeRange.START_OF_DAY; // Reset for finding optional available times
    ImmutableList<TimeRange> optionalAvailableTimes = ImmutableList.copyOf(getAvailableTimeRanges(optionalUnavailableTimes, request));

    
    if (availableTimes.contains(TimeRange.WHOLE_DAY)){ 
      // Return the optional time ranges only if there are no other time constraints
      return optionalAvailableTimes;
    }
    else if (optionalAvailableTimes.isEmpty() || optionalAvailableTimes.contains(TimeRange.WHOLE_DAY)) {
      // No need to check for overlapping times if optional available times do not provide time constraints
      return availableTimes;
    }

    // Find overlapping available time ranges and only return if there are any overlapping times, otherwise ignore optional attendees
    ImmutableList<TimeRange> overlappingAvailableTimes = getOverlappingTimeRanges(availableTimes, optionalAvailableTimes, request);    

    return overlappingAvailableTimes.isEmpty() ? availableTimes : overlappingAvailableTimes;
  }

  public final List<TimeRange> getAvailableTimeRanges(ImmutableList<TimeRange> unavailableTimes, MeetingRequest request) {
    // Method must be public final to access nextAvailableStart and update it in the lambda function
    // TODO: Change structure to not update a class variable in the stream
    List<TimeRange> availableTimes = 
        unavailableTimes.stream()
            .map(currentUnavailableTime -> {

                // Find the next available time range given an unavailable time range
                TimeRange timeRangeAvailable = findAvailableTimeRange(currentUnavailableTime, nextAvailableStart, request);

                // Store the next available start time for the next available time range if needed
                if(currentUnavailableTime.end() > nextAvailableStart) {
                  nextAvailableStart = currentUnavailableTime.end();
                }
                
                return timeRangeAvailable;
            })
            .filter(currentUnavailableTime -> currentUnavailableTime != null)
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

  private static ImmutableList<TimeRange> getUnavailableTimeRanges(Collection<Event> events, ImmutableList<String> attendees) {
    // Get time ranges from events that include at least one attendee
    ImmutableList<TimeRange> unavailableTimes = 
        Streams.stream(events)
            .filter(event -> !Collections.disjoint(attendees, event.getAttendees()))
            .map(Event::getWhen)
            .collect(toImmutableList());
    return unavailableTimes;
  }

  private static ImmutableList<TimeRange> getOverlappingTimeRanges(ImmutableList<TimeRange> requiredTimes, ImmutableList<TimeRange> optionalTimes, MeetingRequest request) {
    List<TimeRange> overlappingAvailableTimes = new ArrayList<TimeRange>();
    int requiredTimeIndex = 0;
    int optionalTimeIndex = 0;

    while (optionalTimeIndex < optionalTimes.size()) {
      // Define current available time ranges
      TimeRange currentRequiredTime = requiredTimes.get(requiredTimeIndex);
      TimeRange currentOptionalTime = optionalTimes.get(optionalTimeIndex);

      // Only add overlapping time ranges that fit the requested duration
      if (currentRequiredTime.overlaps(currentOptionalTime)) {
        // Get the furthest start time and the nearest end time to find the overlapping time range
        int start = Collections.max(ImmutableList.of(currentRequiredTime.start(), currentOptionalTime.start()));
        int end = Collections.min(ImmutableList.of(currentRequiredTime.end(), currentOptionalTime.end()));
        
        // Add time range only if it can fit the requested meeting duration
        if(end - start >= request.getDuration()) {
          overlappingAvailableTimes.add(TimeRange.fromStartEnd(start, end, end == TimeRange.END_OF_DAY));
        }
        requiredTimeIndex += 1;
      }
      optionalTimeIndex += 1;
    }

    // Add the last available time range
    overlappingAvailableTimes.add(requiredTimes.get(requiredTimes.size()-1));
    
    return ImmutableList.copyOf(overlappingAvailableTimes);
  }
}