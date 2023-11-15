/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#![allow(clippy::result_large_err)]

use aws_config::meta::region::RegionProviderChain;
use aws_config::BehaviorVersion;
use aws_sdk_kinesis::{config::Region, meta::PKG_VERSION, Client, Error};
use clap::Parser;

#[derive(Debug, Parser)]
struct Opt {
    /// The AWS Region.
    #[structopt(short, long)]
    region: Option<String>,

    /// The name of the stream to delete.
    #[structopt(short, long)]
    stream_name: String,

    /// Whether to display additional information
    #[structopt(short, long)]
    verbose: bool,
}

// Deletes a stream.
// snippet-start:[kinesis.rust.delete-stream]
async fn remove_stream(client: &Client, stream: &str) -> Result<(), Error> {
    client.delete_stream().stream_name(stream).send().await?;

    println!("Deleted stream.");

    Ok(())
}
// snippet-end:[kinesis.rust.delete-stream]

/// Deletes an Amazon Kinesis data stream.
/// # Arguments
///
/// * `-s STREAM-NAME` - The name of the stream.
/// * `[-r REGION]` - The Region in which the client is created.
///    If not supplied, uses the value of the **AWS_REGION** environment variable.
///    If the environment variable is not set, defaults to **us-west-2**.
/// * `[-v]` - Whether to display additional information.
#[tokio::main]
async fn main() -> Result<(), Error> {
    tracing_subscriber::fmt::init();
    let Opt {
        stream_name,
        region,
        verbose,
    } = Opt::parse();

    let region_provider = RegionProviderChain::first_try(region.map(Region::new))
        .or_default_provider()
        .or_else(Region::new("us-west-2"));
    println!();

    if verbose {
        println!("Kinesis client version: {}", PKG_VERSION);
        println!(
            "Region:                 {}",
            region_provider.region().await.unwrap().as_ref()
        );
        println!("Stream name:            {}", &stream_name);
        println!();
    }

    let shared_config = aws_config::defaults(BehaviorVersion::latest())
        .region(region_provider)
        .load()
        .await;
    let client = Client::new(&shared_config);

    remove_stream(&client, &stream_name).await
}
