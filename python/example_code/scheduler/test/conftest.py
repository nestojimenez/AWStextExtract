# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

"""
Contains common test fixtures used to run unit tests.
"""

import sys

import boto3
import pytest

import scheduler_scenario
from scheduler_wrapper import SchedulerWrapper

# This is needed so Python can find test_tools on the path.
sys.path.append("../..")
from test_tools.fixtures.common import *


class ScenarioData:
    def __init__(self, scheduler_client, cloud_formation_resource, scheduler_stubber, cloud_formation_stubber):
        self.scheduler_client = scheduler_client
        self.cloud_formation_resource= cloud_formation_resource
        self.scheduler_stubber = scheduler_stubber
        self.cloud_formation_stubber = cloud_formation_stubber
        self.scenario = scheduler_scenario.SchedulerScenario(
            scheduler_wrapper=SchedulerWrapper(self.scheduler_client),
        cloud_formation_resource=self.cloud_formation_resource,
        )


@pytest.fixture
def scenario_data(make_stubber):
    scheduler_client = boto3.client("scheduler")
    scheduler_stubber = make_stubber(scheduler_client)
    cloud_formation_resource = boto3.resource("cloudformation")
    cloud_formation_stubber = make_stubber(cloud_formation_resource.meta.client)
    return ScenarioData(scheduler_client, cloud_formation_resource, scheduler_stubber, cloud_formation_stubber)

@pytest.fixture
def mock_wait(monkeypatch):
    return