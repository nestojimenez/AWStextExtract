// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.ec2.scenario;

// snippet-start:[ec2.java2.actions.main]
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.AllocateAddressRequest;
import software.amazon.awssdk.services.ec2.model.AllocateAddressResponse;
import software.amazon.awssdk.services.ec2.model.AssociateAddressRequest;
import software.amazon.awssdk.services.ec2.model.AssociateAddressResponse;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsResponse;
import software.amazon.awssdk.services.ec2.model.DisassociateAddressRequest;
import software.amazon.awssdk.services.ec2.model.DisassociateAddressResponse;
import software.amazon.awssdk.services.ec2.model.DomainType;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressResponse;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.ec2.waiters.Ec2AsyncWaiter;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesResponse;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class EC2Actions {
    private static final Logger logger = LoggerFactory.getLogger(EC2Actions.class);
    private static Ec2AsyncClient ec2AsyncClient;

    /**
     * Retrieves an asynchronous Amazon Elastic Container Registry (ECR) client.
     *
     * @return the configured ECR asynchronous client.
     */
    private static Ec2AsyncClient getAsyncClient() {
        if (ec2AsyncClient == null) {
            /*
            The `NettyNioAsyncHttpClient` class is part of the AWS SDK for Java, version 2,
            and it is designed to provide a high-performance, asynchronous HTTP client for interacting with AWS services.
             It uses the Netty framework to handle the underlying network communication and the Java NIO API to
             provide a non-blocking, event-driven approach to HTTP requests and responses.
             */

            SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(50)  // Adjust as needed.
                .connectionTimeout(Duration.ofSeconds(60))  // Set the connection timeout.
                .readTimeout(Duration.ofSeconds(60))  // Set the read timeout.
                .writeTimeout(Duration.ofSeconds(60))  // Set the write timeout.
                .build();

            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
               .apiCallTimeout(Duration.ofMinutes(2))  // Set the overall API call timeout.
                .apiCallAttemptTimeout(Duration.ofSeconds(90))  // Set the individual call attempt timeout.
                .build();

            ec2AsyncClient = Ec2AsyncClient.builder()
                .region(Region.US_EAST_1)
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .build();
        }
        return ec2AsyncClient;
    }

    // snippet-start:[ec2.java2.delete_key_pair.main]
    /**
     * Deletes a key pair asynchronously.
     *
     * @param keyPair the name of the key pair to delete
     * @return a {@link CompletableFuture} that represents the result of the asynchronous operation.
     *         The {@link CompletableFuture} will complete with a {@link DeleteKeyPairResponse} object
     *         that provides the result of the key pair deletion operation.
     */
    public CompletableFuture<DeleteKeyPairResponse> deleteKeysAsync(String keyPair) {
        DeleteKeyPairRequest request = DeleteKeyPairRequest.builder()
            .keyName(keyPair)
            .build();

        // Initiate the asynchronous request to delete the key pair.
        CompletableFuture<DeleteKeyPairResponse> response = getAsyncClient().deleteKeyPair(request);
        return response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to delete key pair: " + keyPair, ex);
            } else if (resp == null) {
                throw new RuntimeException("No response received for deleting key pair: " + keyPair);
            }
        });
    }
    // snippet-end:[ec2.java2.delete_key_pair.main]


     // snippet-start:[ec2.java2.delete_security_group.main]
    /**
     * Deletes an EC2 security group asynchronously.
     *
     * @param groupId the ID of the security group to delete
     * @return a CompletableFuture that completes when the security group is deleted
     */
    public CompletableFuture<Void> deleteEC2SecGroupAsync(String groupId) {
        DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
            .groupId(groupId)
            .build();

        // Initiate the asynchronous request to delete the security group.
        CompletableFuture<DeleteSecurityGroupResponse> response = getAsyncClient().deleteSecurityGroup(request);
        return response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to delete security group with Id " + groupId, ex);
            } else if (resp == null) {
                throw new RuntimeException("No response received for deleting security group with Id " + groupId);
            }
        }).thenApply(resp -> null);
    }
    // snippet-end:[ec2.java2.delete_security_group.main]

    // snippet-start:[ec2.java2.terminate_instance]
    /**
     * Terminates an EC2 instance asynchronously.
     *
     * @param instanceId the ID of the EC2 instance to terminate
     * @return a {@link CompletableFuture} that completes when the instance has been successfully terminated
     */
    public CompletableFuture<Void> terminateEC2Async(String instanceId) {
        TerminateInstancesRequest terminateRequest = TerminateInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();

        CompletableFuture<TerminateInstancesResponse> response = getAsyncClient().terminateInstances(terminateRequest);
        return response
            .thenCompose(terminateResponse -> {
                if (terminateResponse == null) {
                    throw new RuntimeException("No response received for terminating instance " + instanceId);
                }
                return pollInstanceTerminationStatus(instanceId);
            })
            .whenComplete((pollResponse, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to terminate instance: " + instanceId, ex);
                }
            })
            .thenApply(resp -> null);
    }
    // snippet-end:[ec2.java2.terminate_instance]

    private CompletableFuture<DescribeInstancesResponse> pollInstanceTerminationStatus(String instanceId) {
        DescribeInstancesRequest describeRequest = DescribeInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();

        return getAsyncClient().describeInstances(describeRequest)
            .thenCompose(describeResponse -> {
                String state = describeResponse.reservations().get(0).instances().get(0).state().name().toString();
                if ("terminated".equalsIgnoreCase(state)) {
                    return CompletableFuture.completedFuture(describeResponse);
                } else {
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return pollInstanceTerminationStatus(instanceId).join();
                    });
                }
            });
    }

    // snippet-start:[ec2.java2.release_instance.main]
    /**
     * Releases an Elastic IP address asynchronously.
     *
     * @param allocId the allocation ID of the Elastic IP address to be released
     * @return a {@link CompletableFuture} representing the asynchronous operation of releasing the Elastic IP address
     */
    public CompletableFuture<ReleaseAddressResponse> releaseEC2AddressAsync(String allocId) {
        ReleaseAddressRequest request = ReleaseAddressRequest.builder()
            .allocationId(allocId)
            .build();

        CompletableFuture<ReleaseAddressResponse> response = getAsyncClient().releaseAddress(request);
        response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to release Elastic IP address", ex);
            }
        });

        return response;
    }
    // snippet-end:[ec2.java2.release_instance.main]

    // snippet-start:[ec2.java2.scenario.disassociate_address.main]
    /**
     * Disassociates an Elastic IP address from an instance asynchronously.
     *
     * @param associationId The ID of the association you want to disassociate.
     * @return a {@link CompletableFuture} representing the asynchronous operation of disassociating the address. The
     *         {@link CompletableFuture} will complete with a {@link DisassociateAddressResponse} when the operation is
     *         finished.
     * @throws RuntimeException if the disassociation of the address fails.
     */
    public CompletableFuture<DisassociateAddressResponse> disassociateAddressAsync(String associationId) {
        Ec2AsyncClient ec2 = getAsyncClient();
        DisassociateAddressRequest addressRequest = DisassociateAddressRequest.builder()
            .associationId(associationId)
            .build();

        // Disassociate the address asynchronously.
        CompletableFuture<DisassociateAddressResponse> response = ec2.disassociateAddress(addressRequest);
        response.whenComplete((resp, ex) -> {
            if (ex != null) {
               throw new RuntimeException("Failed to disassociate address", ex);
            }
        });

        return response;
    }
    // snippet-end:[ec2.java2.scenario.disassociate_address.main]

    // snippet-start:[ec2.java2.associate_address.main]
    /**
     * Associates an Elastic IP address with an EC2 instance asynchronously.
     *
     * @param instanceId    the ID of the EC2 instance to associate the Elastic IP address with
     * @param allocationId  the allocation ID of the Elastic IP address to associate
     * @return a {@link CompletableFuture} that completes with the association ID when the operation is successful,
     *         or throws a {@link RuntimeException} if the operation fails
     */
    public CompletableFuture<String> associateAddressAsync(String instanceId, String allocationId) {
        AssociateAddressRequest associateRequest = AssociateAddressRequest.builder()
            .instanceId(instanceId)
            .allocationId(allocationId)
            .build();

        CompletableFuture<AssociateAddressResponse> responseFuture = getAsyncClient().associateAddress(associateRequest);
        return responseFuture.thenApply(response -> {
            if (response.associationId() != null) {
                return response.associationId();
            } else {
                throw new RuntimeException("Association ID is null after associating address.");
            }
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to associate address", ex);
            }
        });
    }
    // snippet-end:[ec2.java2.associate_address.main]

    // snippet-start:[ec2.java2.allocate_address.main]
    /**
     * Allocates an Elastic IP address asynchronously in the VPC domain.
     *
     * @return a {@link CompletableFuture} containing the allocation ID of the allocated Elastic IP address
     */
    public CompletableFuture<String> allocateAddressAsync() {
        AllocateAddressRequest allocateRequest = AllocateAddressRequest.builder()
            .domain(DomainType.VPC)
            .build();

        CompletableFuture<AllocateAddressResponse> responseFuture = getAsyncClient().allocateAddress(allocateRequest);
        return responseFuture.thenApply(AllocateAddressResponse::allocationId).whenComplete((result, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to allocate address", ex);
            }
        });
    }
    // snippet-end:[ec2.java2.allocate_address.main]

    // snippet-start:[ec2.java2.start_stop_instance.start]
    /**
     * Starts an Amazon EC2 instance asynchronously and waits until it is in the "running" state.
     *
     * @param instanceId the ID of the instance to start
     * @return a {@link CompletableFuture} that completes when the instance has been started and is in the "running" state, or exceptionally if an error occurs
     */
    public CompletableFuture<Void> startInstanceAsync(String instanceId) {
        StartInstancesRequest startRequest = StartInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();

        DescribeInstancesRequest describeRequest = DescribeInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();

        logger.info("Starting instance " + instanceId + " and waiting for it to run.");
        return getAsyncClient().startInstances(startRequest)
            .thenCompose(response -> waitUntilInstanceRunningAsync(describeRequest))
            .whenComplete((response, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to start instance: " + instanceId, ex);
                } else if (response == null) {
                    throw new RuntimeException("No response received for starting instance: " + instanceId);
                }
            }).thenApply(resp -> null);
    }
    // snippet-end:[ec2.java2.start_stop_instance.start]

    /**
     * Waits asynchronously until the specified instance is in the "running" state.
     *
     * @param describeRequest the DescribeInstancesRequest containing the instance ID to check
     * @return a CompletableFuture that completes with the DescribeInstancesResponse once the instance is in the "running" state
     */
    private CompletableFuture<DescribeInstancesResponse> waitUntilInstanceRunningAsync(DescribeInstancesRequest describeRequest) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isRunning = false;
            DescribeInstancesResponse response = null;
            while (!isRunning) {
                try {
                    Thread.sleep(5000); // Wait for 5 seconds between checks
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                response = getAsyncClient().describeInstances(describeRequest).join();
                String state = response.reservations().get(0).instances().get(0).state().name().toString();
                if ("running".equalsIgnoreCase(state)) {
                    isRunning = true;
                }
            }

            return response;
        });
    }

    // snippet-start:[ec2.java2.start_stop_instance.stop]
    /**
     * Stops the EC2 instance with the specified ID asynchronously and waits for the instance to stop.
     *
     * @param instanceId the ID of the EC2 instance to stop
     * @return a {@link CompletableFuture} that completes when the instance has been stopped, or exceptionally if an error occurs
     */
    public CompletableFuture<Void> stopInstanceAsync(String instanceId) {
       StopInstancesRequest stopRequest = StopInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();

        DescribeInstancesRequest describeRequest = DescribeInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();

        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        logger.info("Stopping instance " + instanceId + " and waiting for it to stop.");
        getAsyncClient().stopInstances(stopRequest)
            .thenCompose(response -> waitUntilInstanceStoppedAsync(getAsyncClient(), describeRequest)) // Wait until the instance is stopped
            .thenAccept(response -> {
                logger.info("Successfully stopped instance " + instanceId);
                resultFuture.complete(null); // Complete the future successfully
            })
            .exceptionally(throwable -> {
                resultFuture.completeExceptionally(new RuntimeException("Failed to stop instance: " + throwable.getMessage(), throwable));
                return null;
            });

        return resultFuture;
    }
    // snippet-end:[ec2.java2.start_stop_instance.stop]

    private CompletableFuture<DescribeInstancesResponse> waitUntilInstanceStoppedAsync(Ec2AsyncClient ec2, DescribeInstancesRequest describeRequest) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isStopped = false;
            DescribeInstancesResponse response = null;
            while (!isStopped) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                response = ec2.describeInstances(describeRequest).join();
                String state = response.reservations().get(0).instances().get(0).state().name().toString();
                if ("stopped".equalsIgnoreCase(state)) {
                    isStopped = true;
                }
            }

            return response;
        });
    }

    // snippet-start:[ec2.java2.scenario.describe_instance.main]
    /**
     * Asynchronously describes the state of an EC2 instance.
     *
     * @param newInstanceId the ID of the EC2 instance to describe
     * @return a {@link CompletableFuture} that, when completed, contains a string describing the state of the EC2 instance
     */
    public CompletableFuture<String> describeEC2InstancesAsync(String newInstanceId) {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
            .instanceIds(newInstanceId)
            .build();

        return getAsyncClient().describeInstances(request)
            .thenApply(response -> {
                if (response == null || response.reservations().isEmpty()) {
                    throw new RuntimeException("No instances found for the given instance ID: " + newInstanceId);
                }

                return response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .filter(instance -> instance.instanceId().equals(newInstanceId))
                    .findFirst()
                    .map(instance -> {
                        return response.reservations().get(0).instances().get(0).publicIpAddress();
                    })
                    .orElseThrow(() -> new RuntimeException("Instance with ID " + newInstanceId + " not found."));
            })
            .exceptionally(ex -> {
                // Log the error and rethrow it as a RuntimeException.
                logger.info("Failed to describe instances: " + ex.getMessage());
                throw new RuntimeException("Failed to describe instances", ex);
            });
    }

    /**
     * Checks the state of an EC2 instance and retrieves its details if the instance is in the "RUNNING" state.
     *
     * @param request the {@link DescribeInstancesRequest} containing the details of the EC2 instance to check
     * @param resultFuture the {@link CompletableFuture} that will hold the public IP address of the instance when it's available
     */
    private void checkInstanceState(DescribeInstancesRequest request, CompletableFuture<String> resultFuture) {
        getAsyncClient().describeInstances(request)
            .thenAccept(response -> {
                String state = response.reservations().get(0).instances().get(0).state().name().name();
                if ("RUNNING".equals(state)) {
                    logger.info("Image id is " + response.reservations().get(0).instances().get(0).imageId());
                    logger.info("Instance type is " + response.reservations().get(0).instances().get(0).instanceType());
                    logger.info("Instance state is " + response.reservations().get(0).instances().get(0).state().name());
                    String pubAddress = response.reservations().get(0).instances().get(0).publicIpAddress();
                    logger.info("Instance address is " + pubAddress);
                    resultFuture.complete(pubAddress);
                } else {
                    try {
                        Thread.sleep(5000); // Wait for 5 seconds before checking again
                        checkInstanceState(request, resultFuture);
                    } catch (InterruptedException e) {
                        resultFuture.completeExceptionally(e);
                    }
                }
            })
            .exceptionally(throwable -> {
                resultFuture.completeExceptionally(new RuntimeException("Failed to describe EC2 instance: " + throwable.getMessage(), throwable));
                return null;
            });
    }
    // snippet-end:[ec2.java2.scenario.describe_instance.main]

    // snippet-start:[ec2.java2.create_instance.main]
    /**
     * Runs an EC2 instance asynchronously.
     *
     * @param instanceType The instance type to use for the EC2 instance.
     * @param keyName The name of the key pair to associate with the EC2 instance.
     * @param groupName The name of the security group to associate with the EC2 instance.
     * @param amiId The ID of the Amazon Machine Image (AMI) to use for the EC2 instance.
     * @return A {@link CompletableFuture} that completes with the ID of the started EC2 instance.
     * @throws RuntimeException If there is an error running the EC2 instance.
     */
    public CompletableFuture<String> runInstanceAsync(String instanceType, String keyName, String groupName, String amiId) {
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
            .instanceType(instanceType)
            .keyName(keyName)
            .securityGroups(groupName)
            .maxCount(1)
            .minCount(1)
            .imageId(amiId)
            .build();

        CompletableFuture<RunInstancesResponse> responseFuture = getAsyncClient().runInstances(runRequest);
        return responseFuture.thenCompose(response -> {
            String instanceIdVal = response.instances().get(0).instanceId();
            System.out.println("Going to start an EC2 instance and use a waiter to wait for it to be in running state");
            return getAsyncClient().waiter()
                .waitUntilInstanceExists(r -> r.instanceIds(instanceIdVal))
                .thenCompose(waitResponse -> getAsyncClient().waiter()
                    .waitUntilInstanceRunning(r -> r.instanceIds(instanceIdVal))
                    .thenApply(runningResponse -> instanceIdVal));
        }).exceptionally(throwable -> {
            // Handle any exceptions that occurred during the async call
            throw new RuntimeException("Failed to run EC2 instance: " + throwable.getMessage(), throwable);
        });
    }
    // snippet-end:[ec2.java2.create_instance.main]

    // snippet-start:[ec2.java2.scenario.describe_instance.type.main]
    /**
     * Asynchronously retrieves the instance types available in the current AWS region.
     * <p>
     * This method uses the AWS SDK's asynchronous API to fetch the available instance types
     * and then processes the response. It logs the memory information, network information,
     * and instance type for each instance type returned. Additionally, it returns a
     * {@link CompletableFuture} that resolves to the instance type string for the "t2.2xlarge"
     * instance type, if it is found in the response. If the "t2.2xlarge" instance type is not
     * found, an empty string is returned.
     * </p>
     *
     * @return a {@link CompletableFuture} that resolves to the instance type string for the
     * "t2.2xlarge" instance type, or an empty string if the instance type is not found
     */
    public CompletableFuture<String> getInstanceTypesAsync() {
        DescribeInstanceTypesRequest typesRequest = DescribeInstanceTypesRequest.builder()
            .maxResults(10)
            .build();

        CompletableFuture<DescribeInstanceTypesResponse> response = getAsyncClient().describeInstanceTypes(typesRequest);
        response.whenComplete((resp, ex) -> {
            if (resp != null) {
                List<InstanceTypeInfo> instanceTypes = resp.instanceTypes();
                for (InstanceTypeInfo type : instanceTypes) {
                    logger.info("The memory information of this type is " + type.memoryInfo().sizeInMiB());
                    logger.info("Network information is " + type.networkInfo().toString());
                    logger.info("Instance type is " + type.instanceType().toString());
                }
            } else {
                throw (RuntimeException) ex;
            }
        });

        return response.thenApply(resp -> {
            for (InstanceTypeInfo type : resp.instanceTypes()) {
                String instanceType = type.instanceType().toString();
                if (instanceType.equals("t2.2xlarge")) {
                    return instanceType;
                }
            }
            return "";
        });
    }
    // snippet-end:[ec2.java2.scenario.describe_instance.type.main]

    // snippet-start:[ec2.java2.describe_instances.main]
    /**
     * Asynchronously describes an AWS EC2 image with the specified image ID.
     *
     * @param imageId the ID of the image to be described
     * @return a {@link CompletableFuture} that, when completed, contains the ID of the described image
     * @throws RuntimeException if no images are found with the provided image ID, or if an error occurs during the AWS API call
     */
    public CompletableFuture<String> describeImageAsync(String imageId) {
        DescribeImagesRequest imagesRequest = DescribeImagesRequest.builder()
            .imageIds(imageId)
            .build();

        CompletableFuture<DescribeImagesResponse> response = getAsyncClient().describeImages(imagesRequest);
        response.whenComplete((resp, ex) -> {
            if (resp != null) {
                if (resp.images().isEmpty()) {
                    throw new RuntimeException("No images found with the provided image ID.");
                }
                logger.info("The description of the first image is " + resp.images().get(0).description());
                logger.info("The name of the first image is " + resp.images().get(0).name());
            } else {
                throw (RuntimeException) ex;
            }
        });

        return response.thenApply(resp -> resp.images().get(0).imageId());
    }
    // snippet-end:[ec2.java2.describe_instances.main]

    /**
     * Retrieves the parameter values asynchronously using the AWS Systems Manager (SSM) API.
     *
     * @return a {@link CompletableFuture} that holds the response from the SSM API call to get parameters by path
     */
    public CompletableFuture<GetParametersByPathResponse> getParaValuesAsync() {
        SsmAsyncClient ssmClient = SsmAsyncClient.builder()
            .region(Region.US_EAST_1)
            .build();

        GetParametersByPathRequest parameterRequest = GetParametersByPathRequest.builder()
            .path("/aws/service/ami-amazon-linux-latest")
            .build();

        // Create a CompletableFuture to hold the final result.
        CompletableFuture<GetParametersByPathResponse> responseFuture = new CompletableFuture<>();
        ssmClient.getParametersByPath(parameterRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    responseFuture.completeExceptionally(new RuntimeException("Failed to get parameters by path", exception));
                } else {
                    responseFuture.complete(response);
                }
            });

        return responseFuture;
    }

    // snippet-start:[ec2.java2.describe_security_groups.main]
    // snippet-start:[ec2.java2.scenario.describe_securitygroup.main]

    /**
     * Asynchronously describes the security groups for the specified group ID.
     *
     * @param groupName the name of the security group to describe
     * @return a {@link CompletableFuture} that represents the asynchronous operation
     *         of describing the security groups. The future will complete with a
     *         {@link DescribeSecurityGroupsResponse} object that contains the
     *         security group information.
     */
    public CompletableFuture<String> describeSecurityGroupArnByNameAsync(String groupName) {
        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
            .groupNames(groupName)
            .build();

        CompletableFuture<DescribeSecurityGroupsResponse> responseFuture = getAsyncClient().describeSecurityGroups(request);

        // Process the response to extract the ARN and handle any exceptions
        return responseFuture.thenApply(response -> {
            if (response.securityGroups().isEmpty()) {
                throw new RuntimeException("No security group found with the name: " + groupName);
            }

            // Assuming that group names are unique and we get only one result
            return response.securityGroups().get(0).groupId();
        }).whenComplete((arn, exception) -> {
            if (exception != null) {
                throw new RuntimeException("Failed to describe security group: " + exception.getMessage(), exception);
            }
        });
    }
    // snippet-end:[ec2.java2.scenario.describe_securitygroup.main]
    // snippet-end:[ec2.java2.describe_security_groups.main]

    // snippet-start:[ec2.java2.create_security_group.main]
    /**
     * Creates a new security group asynchronously with the specified group name, description, and VPC ID. It also
     * authorizes inbound traffic on ports 80 and 22 from the specified IP address.
     *
     * @param groupName    the name of the security group to create
     * @param groupDesc    the description of the security group
     * @param vpcId        the ID of the VPC in which to create the security group
     * @param myIpAddress  the IP address from which to allow inbound traffic (e.g., "192.168.1.1/0" to allow traffic from
     *                     any IP address in the 192.168.1.0/24 subnet)
     * @return a CompletableFuture that, when completed, returns the ID of the created security group
     * @throws RuntimeException if there was a failure creating the security group or authorizing the inbound traffic
     */
    public CompletableFuture<String> createSecurityGroupAsync(String groupName, String groupDesc, String vpcId, String myIpAddress) {
        CreateSecurityGroupRequest createRequest = CreateSecurityGroupRequest.builder()
            .groupName(groupName)
            .description(groupDesc)
            .vpcId(vpcId)
            .build();

        return getAsyncClient().createSecurityGroup(createRequest)
            .thenCompose(createResponse -> {
                String groupId = createResponse.groupId();
                IpRange ipRange = IpRange.builder()
                    .cidrIp(myIpAddress + "/32")
                    .build();

                IpPermission ipPerm = IpPermission.builder()
                    .ipProtocol("tcp")
                    .toPort(80)
                    .fromPort(80)
                    .ipRanges(ipRange)
                    .build();

                IpPermission ipPerm2 = IpPermission.builder()
                    .ipProtocol("tcp")
                    .toPort(22)
                    .fromPort(22)
                    .ipRanges(ipRange)
                    .build();

                AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
                    .groupName(groupName)
                    .ipPermissions(ipPerm, ipPerm2)
                    .build();

                return getAsyncClient().authorizeSecurityGroupIngress(authRequest)
                    .thenApply(authResponse -> groupId);
            })
            .whenComplete((result, exception) -> {
                if (exception != null) {
                    if (exception instanceof CompletionException && exception.getCause() instanceof Ec2Exception) {
                        throw (Ec2Exception) exception.getCause();
                    } else {
                        throw new RuntimeException("Failed to create security group: " + exception.getMessage(), exception);
                    }
                }
            });
    }
    // snippet-end:[ec2.java2.create_security_group.main]

    // snippet-start:[ec2.java2.describe_key_pairs.main]
    /**
     * Asynchronously describes the key pairs associated with the current AWS account.
     *
     * @return a {@link CompletableFuture} containing the {@link DescribeKeyPairsResponse} object, which provides
     * information about the key pairs.
     */
    public CompletableFuture<DescribeKeyPairsResponse> describeKeysAsync() {
        CompletableFuture<DescribeKeyPairsResponse> responseFuture = getAsyncClient().describeKeyPairs();
        responseFuture.whenComplete((response, exception) -> {
            if (exception != null) {
              throw new RuntimeException("Failed to describe key pairs: " + exception.getMessage(), exception);
            }
        });

        return responseFuture;
    }
    // snippet-end:[ec2.java2.describe_key_pairs.main]

    // snippet-start:[ec2.java2.create_key_pair.main]
    /**
     * Creates a new key pair asynchronously.
     *
     * @param keyName the name of the key pair to create
     * @param fileName the name of the file to write the key material to
     * @return a {@link CompletableFuture} that represents the asynchronous operation
     *         of creating the key pair and writing the key material to a file
     */
    public CompletableFuture<CreateKeyPairResponse> createKeyPairAsync(String keyName, String fileName) {
        CreateKeyPairRequest request = CreateKeyPairRequest.builder()
            .keyName(keyName)
            .build();

        CompletableFuture<CreateKeyPairResponse> responseFuture = getAsyncClient().createKeyPair(request);
        responseFuture.whenComplete((response, exception) -> {
            if (response != null) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                    writer.write(response.keyMaterial());
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write key material to file: " + e.getMessage(), e);
                }
            } else {
                throw new RuntimeException("Failed to create key pair: " + exception.getMessage(), exception);
            }
        });

        return responseFuture;
    }
   // snippet-end:[ec2.java2.create_key_pair.main]

    // snippet-start:[ec2.java2.describe_vpc.main]
    public CompletableFuture<Vpc> describeFirstEC2VpcAsync() {
        Filter myFilter = Filter.builder()
            .name("is-default") // Correct filter name
            .values("true")
            .build();

        return getAsyncClient()
            .describeVpcs(DescribeVpcsRequest.builder()
                .filters(myFilter)
                .build())
            .thenApply(response -> response.vpcs().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Default VPC not found")));
    }
    // snippet-end:[ec2.java2.describe_vpc.main]
}
// snippet-end:[ec2.java2.actions.main]