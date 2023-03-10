/**
 * S3
 * DynamoDB
 */

import { Duration, RemovalPolicy } from "aws-cdk-lib";
import { AttributeType, Table } from "aws-cdk-lib/aws-dynamodb";
import { Bucket, HttpMethods, StorageClass } from "aws-cdk-lib/aws-s3";
import { Construct } from "constructs";

export class PamBuckets extends Construct {
  readonly storage = new Bucket(this, "storage-bucket", {
    removalPolicy: RemovalPolicy.DESTROY,
    cors: [
      {
        allowedHeaders: ["*"],
        allowedMethods: [HttpMethods.PUT],
        allowedOrigins: ["*"],
      },
    ],
  });
  readonly working = new Bucket(this, "working-bucket", {
    removalPolicy: RemovalPolicy.DESTROY,
  });

  constructor(scope: Construct, id: string) {
    super(scope, id);

    // Policy for Glacier storage class for objects with tag rekognition: complete
    this.storage.addLifecycleRule({
      tagFilters: { rekognition: "complete" },
      transitions: [
        {
          storageClass: StorageClass.GLACIER,
          transitionAfter: Duration.days(1),
        },
      ],
    });

    // Add 24-hour deletion policy
    this.working.addLifecycleRule({ expiration: Duration.days(1) });
  }
}

export interface PamTablesProps {}

export class PamTables extends Construct {
  readonly labels = new Table(this, "LabelsTable", {
    partitionKey: { name: "Label", type: AttributeType.STRING },
    removalPolicy: RemovalPolicy.DESTROY,
  });

  readonly jobs = new Table(this, "JobsTable", {
    partitionKey: { name: "JobId", type: AttributeType.STRING },
    removalPolicy: RemovalPolicy.DESTROY,
  });

  constructor(scope: Construct, id: string, props: PamTablesProps = {}) {
    super(scope, id);
  }
}
