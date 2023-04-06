﻿// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier:  Apache-2.0

// snippet-start:[MediaConvert.dotnetv3.CreateJobSetup]

using Microsoft.Extensions.Configuration;
using Amazon.MediaConvert;
using Amazon.MediaConvert.Model;
using MediaConvertActions;

namespace CreateJob;

/// <summary>
/// Create an AWS Elemental MediaConvert job.
/// </summary>
public class CreateJob
{
    static async Task Main(string[] args)
    {
        var _configuration = new ConfigurationBuilder()
            .SetBasePath(Directory.GetCurrentDirectory())
            .AddJsonFile("settings.json") // Load settings from .json file.
            .AddJsonFile("settings.local.json", true) // Optionally, load local settings.
            .Build();

        // MediaConvert role ARN. 
        // For information on creating this role, see
        // https://docs.aws.amazon.com/mediaconvert/latest/ug/creating-the-iam-role-in-mediaconvert-configured.html
        var mediaConvertRole = _configuration["mediaConvertRoleARN"];

        // Include the file input and output locations in settings.json or settings.local.json.
        var fileInput = _configuration["fileInput"];
        var fileOutput = _configuration["fileOutput"];

        // Load the customer endpoint, if it is known.
        // Once you know what your Region-specific endpoint is, set it here, or in your settings.local.json file.
        var mediaConvertEndpoint = _configuration["mediaConvertEndpoint"];

        Console.WriteLine("Welcome to the MediaConvert Create Job example.");
        // If we do not have the customer-specific endpoint, request it here.
        if (string.IsNullOrEmpty(mediaConvertEndpoint))
        {
            Console.WriteLine("Getting customer-specific MediaConvert endpoint.");
            AmazonMediaConvertClient client = new AmazonMediaConvertClient();
            DescribeEndpointsRequest describeRequest = new DescribeEndpointsRequest();
            DescribeEndpointsResponse describeResponse = await client.DescribeEndpointsAsync(describeRequest);
            mediaConvertEndpoint = describeResponse.Endpoints[0].Url;
        }
        Console.WriteLine(new string('-', 80));
        Console.WriteLine($"Using endpoint {mediaConvertEndpoint}.");
        Console.WriteLine(new string('-', 80));
        // Since we have a service url for MediaConvert, we do not
        // need to set RegionEndpoint. If we do, the ServiceURL will
        // be overwritten.
        AmazonMediaConvertConfig mcConfig = new AmazonMediaConvertConfig
        {
            ServiceURL = mediaConvertEndpoint,
        };

        AmazonMediaConvertClient mcClient = new AmazonMediaConvertClient(mcConfig);

        var wrapper = new MediaConvertWrapper(mcClient);
        Console.WriteLine(new string('-', 80));
        Console.WriteLine($"Creating job for input file {fileInput}.");
        var jobId = await wrapper.CreateJob(mediaConvertRole!, fileInput!, fileOutput!);
        Console.WriteLine($"Created job with Job ID: {jobId}");
        Console.WriteLine(new string('-', 80));

        Console.WriteLine(new string('-', 80));
        Console.WriteLine($"Getting job information for Job ID {jobId}");
        var job = await wrapper.GetJobById(jobId);
        Console.WriteLine($"Job {job.Id} created on {job.CreatedAt:d} has status {job.Status}.");
        Console.WriteLine(new string('-', 80));

        Console.WriteLine(new string('-', 80));
        Console.WriteLine($"Listing all complete jobs.");
        var completeJobs = await wrapper.ListAllJobsByStatus(JobStatus.COMPLETE);
        completeJobs.ForEach(j =>
        {
            Console.WriteLine($"Job {j.Id} created on {j.CreatedAt:d} has status {j.Status}.");
        });
        Console.WriteLine(new string('-', 80));
        Console.WriteLine("MediaConvert Create Job example complete.");
        Console.WriteLine(new string('-', 80));
    }
}

// snippet-end:[MediaConvert.dotnetv3.CreateJobSetup]