//snippet-sourcedescription:[RevokeGrant.kt demonstrates how to revoke a grant for the specified AWS Key Management Service (AWS KMS) key.]
//snippet-keyword:[AWS SDK for Kotlin]
//snippet-keyword:[Code Sample]
//snippet-service:[AWS Key Management Service]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[11/04/2021]
//snippet-sourceauthor:[scmacdon-aws]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.kotlin.kms

// snippet-start:[kms.kotlin_revoke_grant.import]
import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.RevokeGrantRequest
import kotlin.system.exitProcess
// snippet-end:[kms.kotlin_revoke_grant.import]

/**
To run this Kotlin code example, ensure that you have setup your development environment,
including your credentials.

For information, see this documentation topic:
https://docs.aws.amazon.com/sdk-for-kotlin/latest/developer-guide/setup.html
 */

suspend fun main(args: Array<String>) {

    val usage = """
        Usage:
            <aliasName>  
        Where:
            keyId - a unique identifier for the KMS key associated with the grant (for example, xxxxxbcd-12ab-34cd-56ef-1234567890ab).
            grantId - a grant id value of the grant revoke.
         """

    if (args.size != 2) {
        println(usage)
        exitProcess(0)
    }

    val keyId = args[0]
    val grantId = args[1]
    revokeKeyGrant(keyId, grantId)
   }

// snippet-start:[kms.kotlin_revoke_grant.main]
suspend fun revokeKeyGrant(keyIdVal: String?, grantIdVal: String?) {

         val request = RevokeGrantRequest {
             keyId = keyIdVal
             grantId = grantIdVal
         }

         KmsClient { region = "us-west-2" }.use { kmsClient ->
              kmsClient.revokeGrant(request)
              println("$grantIdVal was successfully revoked.")
        }
 }
// snippet-end:[kms.kotlin_revoke_grant.main]