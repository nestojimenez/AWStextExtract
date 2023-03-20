﻿// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier:  Apache-2.0

// snippet-start:[StepFunctions.dotnetv3.StepFunctionsBasics]

using System.Diagnostics;
using System.Diagnostics.CodeAnalysis;
using Amazon.IdentityManagement;
using Amazon.IdentityManagement.Model;
using Amazon.StepFunctions.Model;

namespace StepFunctionsBasics;

public class StepFunctionsBasics
{
    private static ILogger _logger = null!;
    private static IConfigurationRoot _configuration;
    private static IAmazonIdentityManagementService _iamService;

    static async Task Main(string[] args)
    {
        // Set up dependency injection for AWS StepFunctions.
        using var host = Host.CreateDefaultBuilder(args)
            .ConfigureLogging(logging =>
                logging.AddFilter("System", LogLevel.Debug)
                    .AddFilter<DebugLoggerProvider>("Microsoft", LogLevel.Information)
                    .AddFilter<ConsoleLoggerProvider>("Microsoft", LogLevel.Trace))
            .ConfigureServices((_, services) =>
            services.AddAWSService<IAmazonStepFunctions>()
                .AddAWSService<IAmazonIdentityManagementService>()
                .AddTransient<StepFunctionsWrapper>()
            )
            .Build();

        _logger = LoggerFactory.Create(builder => { builder.AddConsole(); })
            .CreateLogger<StepFunctionsBasics>();

        // Load configuration settings.
        _configuration = new ConfigurationBuilder()
            .SetBasePath(Directory.GetCurrentDirectory())
            .AddJsonFile("settings.json") // Load test settings from .json file.
            .AddJsonFile("settings.local.json",
                true) // Optionally load local settings.
            .Build();

        var activityName = _configuration["ActivityName"];
        var stateMachineName = _configuration["StateMachineName"];
        var executionName = _configuration["ExecutionName"];
        var roleName = _configuration["RoleName"];
        var repoBaseDir = _configuration["RepoBaseDir"];
        var jsonFilePath = _configuration["JsonFilePath"];
        var jsonFileName = _configuration["JsonFileName"];

        var uiMethods = new UiMethods();
        var stepFunctionsWrapper = host.Services.GetRequiredService<StepFunctionsWrapper>();

        _iamService = host.Services.GetRequiredService<IAmazonIdentityManagementService>();

        // Load definition for the state machine from a JSON file.
        var stateDefinition = File.ReadAllText($"{repoBaseDir}{jsonFilePath}{jsonFileName}");

        Console.Clear();
        uiMethods.DisplayOverview();
        uiMethods.PressEnter();

        uiMethods.DisplayTitle("Create activity");
        Console.WriteLine("Let's start by creating an activity.");
        var activityArn = await stepFunctionsWrapper.CreateActivity(activityName);

        // Find or creqte an IAM role that can be assumed by Step Functions.

        uiMethods.DisplayTitle("Create state machine");
        Console.WriteLine("Now we'll create a state machine.");

        var role = await GetOrCreateStateMachineRole(roleName);

        // Create the state machine.
        var stateMachineArn = await stepFunctionsWrapper.CreateStateMachine(stateMachineName, stateDefinition, role.Arn);
        uiMethods.PressEnter();

        Console.WriteLine($"The state machine has been created. Its Amazon Resource Name (ARN) is: {stateMachineArn}");

        var userName = string.Empty;
        Console.Write("Before we start the state machine, tell me what should ChatSFN call you? ");
        userName = Console.ReadLine();

        // Keep asking until the user enters a string value.
        while (string.IsNullOrEmpty(userName))
        {
            Console.Write("Enter your name: ");
            userName = Console.ReadLine();
        }

        Console.WriteLine($"Thank you, {userName}. Now let's get started...");
        uiMethods.PressEnter();

        uiMethods.DisplayTitle("ChatSFN");

        Console.WriteLine("Now we'll start execution of the state machine.");
        var isDone = false;
        var actionList = new List<string>();
        var response = new GetActivityTaskResponse();
        var taskToken = string.Empty;
        var userChoice = string.Empty;

        while (!isDone)
        {
            response = await stepFunctionsWrapper.GetActivityTaskAsync(activityArn, "MvpWorker");
            taskToken = response.TaskToken;
            Console.WriteLine($"ChatSFN: {response.Input}");
            Console.Write($"{userName}, please tell me your choice: ");
            userChoice = Console.ReadLine();
            if (userChoice.ToLower() == "done")
            {
                isDone = true;
            }

            Console.WriteLine($"You have selected {userChoice}");
            var taskJson = @"{""action"": """ + userChoice + @"""}";
            Console.WriteLine(taskJson);
            var taskSuccess = await stepFunctionsWrapper.SendTaskSuccess(taskToken, taskJson);
        }

        // Start the state machine execution.
        var executionArn = await stepFunctionsWrapper.StartExecution(executionName, executionJson, stateMachineArn);
        Console.WriteLine("State machine started.");

        var success = await stepFunctionsWrapper.StopExecution(executionArn);
        Console.WriteLine("State machine stopped.");
        uiMethods.PressEnter();

        Console.WriteLine("Now let's take a look at the execution values for the state machine.");

        // List the executions.
        var executions = await stepFunctionsWrapper.ListExecutions(stateMachineArn);

        uiMethods.DisplayTitle("Step function executions");
        executions.ForEach(execution =>
        {
            Console.WriteLine($"{execution.Name}\t{execution.StartDate} to {execution.StopDate}");
        });

        // Now delete the state machine and the activity.
        uiMethods.DisplayTitle("Clean up resources");
        Console.WriteLine("Deleting the state machine...");

        success = await stepFunctionsWrapper.DeleteStateMachine(stateMachineArn);
        Console.WriteLine("State machine deleted.");

        Console.WriteLine("Deleting the activity...");
        success = await stepFunctionsWrapper.DeleteActivity(activityArn);
        Console.WriteLine("Activity deleted.");

        Console.WriteLine("The Amazon Step Functions scenario is now complete.");
    }

    static async Task<Role> GetOrCreateStateMachineRole(string roleName)
    {
        // Define the policy document for the role.
        var stateMachineRolePolicy = @"{
         ""Version"": ""2012-10-17"",
        ""Statement"": [{
            ""Sid"": """",
            ""Effect"": ""Allow"",
            ""Principal"": {
                ""Service"": ""states.amazonaws.com""},
            ""Action"": ""sts:AssumeRole""}]}";

        var role = new Role();
        var roleExists = false;

        try
        {
            var getRoleResponse = await _iamService.GetRoleAsync(new GetRoleRequest { RoleName = roleName });
            roleExists = true;
            role = getRoleResponse.Role;
        }
        catch (NoSuchEntityException noRole)
        {
            // The role doesn't exist. Create it.
            Console.WriteLine($"Role, {roleName} doesn't exist. Creating it...");
        }

        if (!roleExists)
        {
            var request = new CreateRoleRequest
            {
                RoleName = roleName,
                AssumeRolePolicyDocument = stateMachineRolePolicy,
            };

            var createRoleResponse = await _iamService.CreateRoleAsync(request);
            role = createRoleResponse.Role;
        }

        return role;
    }
}

// snippet-end:[StepFunctions.dotnetv3.StepFunctionsBasics]