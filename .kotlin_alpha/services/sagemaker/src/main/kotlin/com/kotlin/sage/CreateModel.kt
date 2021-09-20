//snippet-sourcedescription:[CreateModel.kt demonstrates how to create a model in Amazon SageMaker.]
//snippet-keyword:[AWS SDK for Kotlin]
//snippet-keyword:[Code Sample]
//snippet-keyword:[Amazon SageMaker]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[9/20/2021]
//snippet-sourceauthor:[scmacdon - AWS]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.kotlin.sage

//snippet-start:[sagemaker.kotlin.create_model.import]
import aws.sdk.kotlin.services.sagemaker.SageMakerClient
import aws.sdk.kotlin.services.sagemaker.model.ContainerDefinition
import aws.sdk.kotlin.services.sagemaker.model.CreateModelRequest
import aws.sdk.kotlin.services.sagemaker.model.ContainerMode
import aws.sdk.kotlin.services.sagemaker.model.SageMakerException
import kotlin.system.exitProcess
//snippet-end:[sagemaker.kotlin.create_model.import]

/**
To run this Kotlin code example, ensure that you have setup your development environment,
including your credentials.

For information, see this documentation topic:
https://docs.aws.amazon.com/sdk-for-kotlin/latest/developer-guide/setup.html
 */
suspend fun main(args:Array<String>) {

    val usage = """
    Usage:
        <dataUrl> <image> <modelName> <executionRoleArn>

    Where:
        dataUrl - the Amazon S3 path where the model artifacts, which result from model training, are stored.
        image - the Amazon EC2 Container Registry (Amazon ECR) path where inference code is stored (for example, xxxxx5047983.dkr.ecr.us-west-2.amazonaws.com/train).
        modelName - the name of the model.
        executionRoleArn - the Amazon Resource Name (ARN) of the IAM role that Amazon SageMaker can assume to access model artifacts (for example, arn:aws:iam::xxxxx5047983:role/service-role/AmazonSageMaker-ExecutionRole-20200627T12xxxx).

    """

    if (args.size != 4) {
       println(usage)
        exitProcess(1)
    }

    val dataUrl = args[0]
    val image = args[1]
    val modelName = args[2]
    val executionRoleArn = args[3]

    val sageMakerClient = SageMakerClient{region = "us-west-2" }
    createSagemakerModel(sageMakerClient, dataUrl, image, modelName, executionRoleArn)
    sageMakerClient.close()
}

//snippet-start:[sagemaker.kotlin.create_model.main]
suspend fun createSagemakerModel(
        sageMakerClient: SageMakerClient,
        dataUrl: String?,
        imageVal: String?,
        modelNameVal: String?,
        executionRoleArnVal: String?) {

        try {

            val containerDefinition = ContainerDefinition {
              modelDataUrl = dataUrl
              image = imageVal
              mode = ContainerMode.SingleModel
            }

           val modelRequest = CreateModelRequest {
              modelName = modelNameVal
              executionRoleArn = executionRoleArnVal
              primaryContainer = containerDefinition
           }

           val response = sageMakerClient.createModel(modelRequest)
           println("The ARN of the model is ${response.modelArn}")

       } catch (e: SageMakerException) {
         println(e.message)
         sageMakerClient.close()
         exitProcess(0)
       }
  }
//snippet-end:[sagemaker.kotlin.create_model.main]