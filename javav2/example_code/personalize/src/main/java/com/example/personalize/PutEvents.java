//snippet-sourcedescription:[PutEvents.java demonstrates how to import real-time event data into Amazon Personalize.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-keyword:[Code Sample]
//snippet-service:[Amazon Personalize]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[5/19/2021]
//snippet-sourceauthor:[seashman - AWS]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.personalize;

//snippet-start:[personalize.java2.put_events.import]

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.personalize.PersonalizeClient;
import software.amazon.awssdk.services.personalizeevents.PersonalizeEventsClient;
import software.amazon.awssdk.services.personalizeevents.model.Event;
import software.amazon.awssdk.services.personalizeevents.model.PersonalizeEventsException;
import software.amazon.awssdk.services.personalizeevents.model.PutEventsRequest;

//snippet-end:[personalize.java2.put_events.import]
public class PutEvents {

    public static void main(String[] args) {
        final String USAGE = "\n" +
                "Usage:\n" +
                "    PutEvents <trackingId, userId, sessionId, itemId>\n\n" +
                "Where:\n" +
                "    trackingId - The identification number of the dataset group's event tracker." +
                "The ID is generated by a call to the CreateEventTracker API.\n" +
                "    userId -The user associated with the event.\n" +
                "    sessionId - The session ID associated with the user's visit. Your application generates the " +
                "sessionId when a user first visits your website or uses your application.\n" +
                "    itemId - The item's identification number.\n\n";

        if (args.length != 4) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String trackingId = args[0];
        String userId = args[1];
        String sessionId = args[2];
        String itemId = args[3];

        // Change to the region where your resources are located
        Region region = Region.US_WEST_2;

        // Build a personalize events client
        PersonalizeEventsClient personalizeEventsClient = PersonalizeEventsClient.builder()
                .region(region)
                .build();
        putEvents(personalizeEventsClient, trackingId, sessionId, userId, itemId);

        personalizeEventsClient.close();
    }

    //snippet-start:[personalize.java2.put_events.main]
    public static int putEvents(PersonalizeEventsClient personalizeEventsClient,
                                String trackingId,
                                String userId,
                                String itemId,
                                String sessionId) {

        int responseCode = 0;

        try {

            // Build an event and a putEvents request with only the required information for a minimal schema.
            // Schema columns for this example would be itemId, userId, and timestamp.
            Event event = Event.builder()
                    .sentAt(Instant.ofEpochMilli(System.currentTimeMillis() + 10 * 60 * 1000))
                    .itemId(itemId)
                    .eventType("typePlaceholder")
                    .build();

            PutEventsRequest putEventsRequest = PutEventsRequest.builder()
                    .trackingId(trackingId)
                    .userId(userId)
                    .sessionId(sessionId)
                    .eventList(event)
                    .build();

            responseCode = personalizeEventsClient.putEvents(putEventsRequest).sdkHttpResponse().statusCode();
            System.out.println("Response code: " + responseCode);
            return responseCode;

        } catch (PersonalizeEventsException e) {
            System.out.println(e.awsErrorDetails().errorMessage());
        }
        return responseCode;
    }
    //snippet-end:[personalize.java2.put_events.main]
}
