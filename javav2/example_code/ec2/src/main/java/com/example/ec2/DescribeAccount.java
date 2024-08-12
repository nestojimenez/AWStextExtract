// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.ec2;

// snippet-start:[ec2.java2.describe_account.main]
// snippet-start:[ec2.java2.describe_account.import]
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.DescribeAccountAttributesResponse;
import java.util.concurrent.CompletableFuture;
// snippet-end:[ec2.java2.describe_account.import]

/**
 * Before running this Java V2 code example, set up your development
 * environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class DescribeAccount {
    public static void main(String[] args) {
        Ec2AsyncClient ec2AsyncClient = Ec2AsyncClient.builder()
            .region(Region.US_EAST_1)
             .build();

        try {
            CompletableFuture<DescribeAccountAttributesResponse> future = describeEC2AccountAsync(ec2AsyncClient);
            future.join(); // Get the value.
            System.out.println("EC2 Account attributes described successfully.");
        } catch (RuntimeException rte) {
            System.err.println("An exception occurred: " + (rte.getCause() != null ? rte.getCause().getMessage() : rte.getMessage()));
        }
    }

    public static CompletableFuture<DescribeAccountAttributesResponse> describeEC2AccountAsync(Ec2AsyncClient ec2AsyncClient) {
        CompletableFuture<DescribeAccountAttributesResponse> response = ec2AsyncClient.describeAccountAttributes();

        response.whenComplete((accountResults, ex) -> {
            if (accountResults != null) {
                accountResults.accountAttributes().forEach(attribute -> {
                    System.out.print("\n The name of the attribute is " + attribute.attributeName());
                    attribute.attributeValues().forEach(
                        myValue -> System.out.print("\n The value of the attribute is " + myValue.attributeValue()));
                });
            } else {
                throw new RuntimeException("Failed to describe EC2 account attributes.", ex);
            }
        });

        return response;
    }
}
// snippet-end:[ec2.java2.describe_account.main]
