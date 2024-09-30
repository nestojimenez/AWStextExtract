// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.iotsitewise.scenario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.services.iotsitewise.model.AssetModelPropertySummary;
import software.amazon.awssdk.services.iotsitewise.model.BatchPutAssetPropertyValueResponse;
import software.amazon.awssdk.services.iotsitewise.model.CreateGatewayRequest;
import software.amazon.awssdk.services.iotsitewise.model.CreateGatewayResponse;
import software.amazon.awssdk.services.iotsitewise.model.DeleteGatewayRequest;
import software.amazon.awssdk.services.iotsitewise.model.DeleteGatewayResponse;
import software.amazon.awssdk.services.iotsitewise.model.DescribeGatewayRequest;
import software.amazon.awssdk.services.iotsitewise.model.DescribeGatewayResponse;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.iotsitewise.IoTSiteWiseAsyncClient;
import software.amazon.awssdk.services.iotsitewise.model.AssetModelPropertyDefinition;
import software.amazon.awssdk.services.iotsitewise.model.AssetModelSummary;
import software.amazon.awssdk.services.iotsitewise.model.AssetPropertyValue;
import software.amazon.awssdk.services.iotsitewise.model.BatchPutAssetPropertyValueRequest;
import software.amazon.awssdk.services.iotsitewise.model.CreateAssetModelRequest;
import software.amazon.awssdk.services.iotsitewise.model.CreateAssetModelResponse;
import software.amazon.awssdk.services.iotsitewise.model.CreateAssetRequest;
import software.amazon.awssdk.services.iotsitewise.model.CreateAssetResponse;
import software.amazon.awssdk.services.iotsitewise.model.CreatePortalRequest;
import software.amazon.awssdk.services.iotsitewise.model.DeleteAssetModelRequest;
import software.amazon.awssdk.services.iotsitewise.model.DeleteAssetModelResponse;
import software.amazon.awssdk.services.iotsitewise.model.DeleteAssetRequest;
import software.amazon.awssdk.services.iotsitewise.model.DeleteAssetResponse;
import software.amazon.awssdk.services.iotsitewise.model.DeletePortalRequest;
import software.amazon.awssdk.services.iotsitewise.model.DeletePortalResponse;
import software.amazon.awssdk.services.iotsitewise.model.DescribePortalRequest;
import software.amazon.awssdk.services.iotsitewise.model.GatewayPlatform;
import software.amazon.awssdk.services.iotsitewise.model.GetAssetPropertyValueRequest;
import software.amazon.awssdk.services.iotsitewise.model.GreengrassV2;
import software.amazon.awssdk.services.iotsitewise.model.ListAssetModelPropertiesRequest;
import software.amazon.awssdk.services.iotsitewise.model.ListAssetModelsRequest;
import software.amazon.awssdk.services.iotsitewise.model.Measurement;
import software.amazon.awssdk.services.iotsitewise.model.PropertyDataType;
import software.amazon.awssdk.services.iotsitewise.model.PropertyType;
import software.amazon.awssdk.services.iotsitewise.model.PutAssetPropertyValueEntry;
import software.amazon.awssdk.services.iotsitewise.model.TimeInNanos;
import software.amazon.awssdk.services.iotsitewise.model.Variant;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

// snippet-start:[iotsitewise.java2.actions.main]
public class SitewiseActions {

    private static final Logger logger = LoggerFactory.getLogger(SitewiseActions.class);

    private static IoTSiteWiseAsyncClient ioTSiteWiseAsyncClient;

    private static IoTSiteWiseAsyncClient getAsyncClient() {
        if (ioTSiteWiseAsyncClient == null) {
            SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(100)
                .connectionTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();

            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMinutes(2))
                .apiCallAttemptTimeout(Duration.ofSeconds(90))
                .retryStrategy(RetryMode.STANDARD)
                .build();

            ioTSiteWiseAsyncClient = IoTSiteWiseAsyncClient.builder()
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .build();
        }
        return ioTSiteWiseAsyncClient;
    }

    // snippet-start:[sitewise.java2_create_asset_model.main]
    /**
     * Creates an asset model.
     *
     * @param name the name of the asset model to create.
     * @return a {@link CompletableFuture} that completes with the created {@link CreateAssetModelResponse} when the operation is complete.
     * @throws RuntimeException if an error occurs while creating the asset model.
     */
    public CompletableFuture<CreateAssetModelResponse> createAssetModelAsync(String name) {
        PropertyType humidity = PropertyType.builder()
            .measurement(Measurement.builder().build())
            .build();

        PropertyType temperaturePropertyType = PropertyType.builder()
            .measurement(Measurement.builder().build())
            .build();

        AssetModelPropertyDefinition temperatureProperty = AssetModelPropertyDefinition.builder()
            .name("Temperature")
            .dataType(PropertyDataType.DOUBLE)
            .type(temperaturePropertyType)
            .build();

        AssetModelPropertyDefinition humidityProperty = AssetModelPropertyDefinition.builder()
            .name("Humidity")
            .dataType(PropertyDataType.DOUBLE)
            .type(humidity)
            .build();

        CreateAssetModelRequest createAssetModelRequest = CreateAssetModelRequest.builder()
            .assetModelName(name)
            .assetModelDescription("This is my asset model")
            .assetModelProperties(temperatureProperty, humidityProperty)
            .build();

        return getAsyncClient().createAssetModel(createAssetModelRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("Failed to create asset model: {} ", exception.getMessage());
                }
            });
    }
    // snippet-end:[sitewise.java2_create_asset_model.main]

    // snippet-start:[sitewise.java2_create_asset.main]
    /**
     * Creates an asset with the specified name and model Id.
     *
     * @param assetName     the name of the asset to create.
     * @param assetModelId the Id of the asset model to associate with the asset.
     * @return a {@link CompletableFuture} that completes with the {@link CreateAssetResponse} when the asset creation is complete.
     * @throws RuntimeException if the asset creation fails.
     */
    public CompletableFuture<CreateAssetResponse> createAssetAsync(String assetName, String assetModelId) {
        CreateAssetRequest createAssetRequest = CreateAssetRequest.builder()
            .assetModelId(assetModelId)
            .assetDescription("Created using the AWS SDK for Java")
            .assetName(assetName)
            .build();

        return getAsyncClient().createAsset(createAssetRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("Failed to create asset: {}", exception.getMessage());
                }
            });
    }
    // snippet-end:[sitewise.java2_create_asset.main]

    // snippet-start:[sitewise.java2_put_batch_property.main]
    /**
     * Sends data to the SiteWise service.
     *
     * @param assetId        the ID of the asset to which the data will be sent.
     * @param tempPropertyId the ID of the temperature property.
     * @param humidityPropId the ID of the humidity property.
     * @return a CompletableFuture representing the response from the SiteWise service.
     */
    public CompletableFuture<BatchPutAssetPropertyValueResponse> sendDataToSiteWiseAsync(String assetId, String tempPropertyId, String humidityPropId) {
        Map<String, Double> sampleData = generateSampleData();
        long timestamp = Instant.now().toEpochMilli();

        TimeInNanos time = TimeInNanos.builder()
            .timeInSeconds(timestamp / 1000)
            .offsetInNanos((int) ((timestamp % 1000) * 1000000))
            .build();

        BatchPutAssetPropertyValueRequest request = BatchPutAssetPropertyValueRequest.builder()
            .entries(Arrays.asList(
                PutAssetPropertyValueEntry.builder()
                    .entryId("entry-3")
                    .assetId(assetId)
                    .propertyId(tempPropertyId)
                    .propertyValues(Arrays.asList(
                        AssetPropertyValue.builder()
                            .value(Variant.builder()
                                .doubleValue(sampleData.get("Temperature"))
                                .build())
                            .timestamp(time)
                            .build()
                    ))
                    .build(),
                PutAssetPropertyValueEntry.builder()
                    .entryId("entry-4")
                    .assetId(assetId)
                    .propertyId(humidityPropId)
                    .propertyValues(Arrays.asList(
                        AssetPropertyValue.builder()
                            .value(Variant.builder()
                                .doubleValue(sampleData.get("Humidity"))
                                .build())
                            .timestamp(time)
                            .build()
                    ))
                    .build()
            ))
            .build();

        return getAsyncClient().batchPutAssetPropertyValue(request)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("An exception occurred: {}", exception.getCause().getMessage());
                } else {
                    logger.info("Data sent successfully.");
                }
            });
    }
    // snippet-end:[sitewise.java2_put_batch_property.main]

    // snippet-start:[sitewise.java2_get_property.main]
    /**
     * Fetches the value of an asset property.
     *
     * @param propId  the ID of the asset property to fetch.
     * @param assetId the ID of the asset to fetch the property value for.
     * @throws RuntimeException if an error occurs while fetching the property value.
     */
    public CompletableFuture<Double> getAssetPropValueAsync(String propId, String assetId) {
        GetAssetPropertyValueRequest assetPropertyValueRequest = GetAssetPropertyValueRequest.builder()
            .propertyId(propId)
            .assetId(assetId)
            .build();

        return getAsyncClient().getAssetPropertyValue(assetPropertyValueRequest)
       .handle((response, exception) -> {
            if (exception != null) {
                logger.error("Error occurred while fetching property value: " + exception.getMessage(), exception);
                throw (RuntimeException) exception;
            }
                return response.propertyValue().value().doubleValue();
       });
    }
    // snippet-end:[sitewise.java2_get_property.main]

    // snippet-start:[sitewise.java2.describe.asset.model.main]
    /**
     * Retrieves the property IDs associated with a specific asset model.
     *
     * @param assetModelId the ID of the asset model to retrieve the property IDs for.
     * @return a {@link CompletableFuture} that, when completed, contains a {@link Map} mapping the property names to their corresponding IDs.
     * @throws CompletionException if an error occurs while retrieving the asset model properties.
     */
    public CompletableFuture<Map<String, String>> getPropertyIds(String assetModelId) {
        ListAssetModelPropertiesRequest modelPropertiesRequest = ListAssetModelPropertiesRequest.builder().assetModelId(assetModelId).build();
        return getAsyncClient().listAssetModelProperties(modelPropertiesRequest)
            .handle((response, throwable) -> {
                if (response != null) {
                    return response.assetModelPropertySummaries().stream()
                        .collect(Collectors
                            .toMap(AssetModelPropertySummary::name, AssetModelPropertySummary::id));
                } else {
                    throw (CompletionException) throwable.getCause();
                }
            });
    }
    // snippet-end:[sitewise.java2.describe.asset.model.main]

    // snippet-start:[sitewise.java2.delete.asset.main]
    /**
     * Deletes an asset.
     *
     * @param assetId the ID of the asset to be deleted.
     * @return a {@link CompletableFuture} that represents the asynchronous operation of deleting the asset.
     * @throws RuntimeException if the asset deletion fails.
     */
    public CompletableFuture<DeleteAssetResponse> deleteAssetAsync(String assetId) {
        DeleteAssetRequest deleteAssetRequest = DeleteAssetRequest.builder()
            .assetId(assetId)
            .build();

        return getAsyncClient().deleteAsset(deleteAssetRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("An error occurred deleting asset with id: {}", assetId);
                } else {
                    logger.info("Request to delete the asset completed successfully.");
                }
            });
    }
    // snippet-end:[sitewise.java2.delete.asset.main]

    // snippet-start:[sitewise.java2.delete.asset.model.main]
    /**
     * Deletes an Asset Model with the specified ID.
     *
     * @param assetModelId the ID of the Asset Model to delete.
     * @return a {@link CompletableFuture} that completes with the {@link DeleteAssetModelResponse} when the operation is complete.
     * @throws RuntimeException if the operation fails, containing the error message and the underlying exception.
     */
    public CompletableFuture<DeleteAssetModelResponse> deleteAssetModelAsync(String assetModelId) {
        DeleteAssetModelRequest deleteAssetModelRequest = DeleteAssetModelRequest.builder()
            .assetModelId(assetModelId)
            .build();

        return getAsyncClient().deleteAssetModel(deleteAssetModelRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("Failed to delete asset model with ID:{}.", exception.getMessage());
                }
            });
    }
    // snippet-end:[sitewise.java2.delete.asset.model.main]

    // snippet-start:[sitewise.java2.create.portal.main]
    /**
     * Creates a new IoT SiteWise portal.
     *
     * @param portalName   the name of the portal to create.
     * @param iamRole      the IAM role ARN to use for the portal.
     * @param contactEmail the email address of the portal contact.
     * @return a {@link CompletableFuture} that completes with the portal ID when the portal is created successfully, or throws a {@link RuntimeException} if the creation fails.
     */
    public CompletableFuture<String> createPortalAsync(String portalName, String iamRole, String contactEmail) {
        CreatePortalRequest createPortalRequest = CreatePortalRequest.builder()
            .portalName(portalName)
            .portalDescription("This is my custom IoT SiteWise portal.")
            .portalContactEmail(contactEmail)
            .roleArn(iamRole)
            .build();

        return getAsyncClient().createPortal(createPortalRequest)
            .handle((response, exception) -> {
                if (exception != null) {
                    logger.error("Failed to create portal: {} ", exception.getCause().getMessage());
                    throw (RuntimeException) exception;
                }
                return response.portalId();
            });
    }
    // snippet-end:[sitewise.java2.create.portal.main]

    // snippet-start:[sitewise.java2.delete.portal.main]
    /**
     * Deletes a portal.
     *
     * @param portalId the ID of the portal to be deleted.
     * @return a {@link CompletableFuture} containing the {@link DeletePortalResponse} when the operation is complete.
     * @throws RuntimeException if the portal deletion fails, with the error message and the underlying exception.
     */
    public CompletableFuture<DeletePortalResponse> deletePortalAsync(String portalId) {
        DeletePortalRequest deletePortalRequest = DeletePortalRequest.builder()
            .portalId(portalId)
            .build();

        return getAsyncClient().deletePortal(deletePortalRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("Failed to delete portal with ID: " + portalId + ". Error: " + exception.getMessage(), exception);
                }
            });
    }
    // snippet-end:[sitewise.java2.delete.portal.main]

    // snippet-start:[sitewise.java2.list.asset.model.main]
    /**
     * Retrieves the asset model ID for the given asset model name.
     *
     * @param assetModelName the name of the asset model to retrieve the ID for.
     * @return a {@link CompletableFuture} that, when completed, contains the asset model ID, or {@code null} if the asset model is not found.
     */
    public CompletableFuture<String> getAssetModelIdAsync(String assetModelName) {
        ListAssetModelsRequest listAssetModelsRequest = ListAssetModelsRequest.builder().build();
        return getAsyncClient().listAssetModels(listAssetModelsRequest)
            .handle((listAssetModelsResponse, exception) -> {
                if (exception != null) {
                    throw new RuntimeException("Failed to retrieve Asset Model ARN: " + exception.getMessage(), exception);
                }
                for (AssetModelSummary assetModelSummary : listAssetModelsResponse.assetModelSummaries()) {
                    if (assetModelSummary.name().equals(assetModelName)) {
                        return assetModelSummary.id();
                    }
                }
                return null;
            });
    }
    // snippet-end:[sitewise.java2.list.asset.model.main]

    // snippet-start:[sitewise.java2.describe.portal.main]
    /**
     * Describes a portal.
     *
     * @param portalId the ID of the portal to describe.
     * @return a {@link CompletableFuture} that, when completed, will contain the URL of the described portal.
     * @throws RuntimeException if the portal description operation fails.
     */
    public CompletableFuture<String> describePortalAsync(String portalId) {
        DescribePortalRequest request = DescribePortalRequest.builder()
            .portalId(portalId)
            .build();

        return getAsyncClient().describePortal(request)
            .handle((response, exception) -> {
                if (exception != null) {
                    throw new RuntimeException("Failed to describe portal: " + exception.getMessage(), exception);
                }
                return response.portalStartUrl();
            });
    }
    // snippet-end:[sitewise.java2.describe.portal.main]

    // snippet-start:[sitewise.java2.create.gateway.main]
    /**
     * Creates a new IoT Sitewise gateway.
     *
     * @return a {@link CompletableFuture} containing the {@link CreateGatewayResponse} representing the created gateway.
     * @throws RuntimeException if there was an error creating the gateway.
     */
    public CompletableFuture<String> createGatewayAsync(String gatewayName, String myThing) {
        GreengrassV2 gg = GreengrassV2.builder()
            .coreDeviceThingName(myThing)
            .build();

        GatewayPlatform platform = GatewayPlatform.builder()
            .greengrassV2(gg)
            .build();

        Map<String, String> tag = new HashMap<>();
        tag.put("Environment", "Production");

        CreateGatewayRequest createGatewayRequest = CreateGatewayRequest.builder()
            .gatewayName(gatewayName)
            .gatewayPlatform(platform)
            .tags(tag)
            .build();

        return getAsyncClient().createGateway(createGatewayRequest)
            .handle((response, exception) -> {
                if (exception != null) {
                    logger.error("Error creating the gateway.");
                    throw (RuntimeException) exception;
                }
                logger.info("The ARN of the gateway is {}" ,  response.gatewayArn());
                return response.gatewayId();
            });
    }
    // snippet-end:[sitewise.java2.create.gateway.main]

    // snippet-start:[sitewise.java2.delete.gateway.main]
    /**
     * Deletes the specified gateway.
     *
     * @param gatewayARN the ARN of the gateway to delete.
     * @return a CompletableFuture containing the response of the delete operation.
     * @throws RuntimeException if an error occurs during the delete operation.
     */
    public CompletableFuture<DeleteGatewayResponse> deleteGatewayAsync(String gatewayARN) {
        DeleteGatewayRequest deleteGatewayRequest = DeleteGatewayRequest.builder()
            .gatewayId(gatewayARN)
            .build();

        return getAsyncClient().deleteGateway(deleteGatewayRequest)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("Failed to delete gateway: " + exception.getMessage(), exception);
                } else {
                    logger.info("The Gateway was deleted successfully.");
                }
            });
    }
    // snippet-end:[sitewise.java2.delete.gateway.main]

    // snippet-start:[sitewise.java2.describe.gateway.main]
    /**
     * Describes the specified gateway.
     *
     * @param gatewayId the ID of the gateway to describe.
     * @return a {@link CompletableFuture} that represents the asynchronous operation
     * which will complete with a {@link DescribeGatewayResponse} containing
     * information about the specified gateway.
     */
    public CompletableFuture<DescribeGatewayResponse> describeGatewayAsync(String gatewayId) {
        DescribeGatewayRequest request = DescribeGatewayRequest.builder()
            .gatewayId(gatewayId)
            .build();

        return getAsyncClient().describeGateway(request)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    logger.error("An error occurred during the describeGateway method", exception);
                }
            });
    }
    // snippet-end:[sitewise.java2.describe.gateway.main]

    private static Map<String, Double> generateSampleData() {
        Map<String, Double> data = new HashMap<>();
        data.put("Temperature", 23.5);
        data.put("Humidity", 65.0);
        return data;
    }
}
// snippet-end:[iotsitewise.java2.actions.main]