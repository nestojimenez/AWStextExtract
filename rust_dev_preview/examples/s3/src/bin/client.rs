/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#![allow(clippy::result_large_err)]

// snippet-start:[s3.rust.client-use]
use aws_config::meta::region::RegionProviderChain;
use aws_config::BehaviorMajorVersion;
use aws_sdk_s3::Client;
// snippet-end:[s3.rust.client-use]

/// Lists your buckets.
#[tokio::main]
async fn main() -> Result<(), aws_sdk_s3::Error> {
    // snippet-start:[s3.rust.client-client]
    let region_provider = RegionProviderChain::default_provider().or_else("us-east-1");
    let config = aws_config::from_env_with_version(BehaviorMajorVersion::latest())
        .region(region_provider)
        .load()
        .await;
    let client = Client::new(&config);
    // snippet-end:[s3.rust.client-client]

    let resp = client.list_buckets().send().await?;
    let buckets = resp.buckets();
    let num_buckets = buckets.len();

    for bucket in buckets {
        println!("{}", bucket.name().unwrap_or_default());
    }

    println!();
    println!("Found {} buckets.", num_buckets);

    Ok(())
}
