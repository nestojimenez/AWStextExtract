import boto3
import random
import logging
import coloredlogs
from prettytable import PrettyTable
from datetime import datetime, timedelta

# Constants
BUCKET_PREFIX = "py-object-locking"
FILE_CONTENT = "This is a test file for S3 Object Locking."
RANDOM_SUFFIX = str(random.randint(100, 999))
LOG_FORMAT = '%(asctime)s [%(levelname)s] %(message)s'

# Configure logging
logger = logging.getLogger(__name__)
coloredlogs.install(level='DEBUG', logger=logger, fmt=LOG_FORMAT)


def create_buckets(s3_client):
    """Create S3 buckets with different configurations and save their names to a file."""
    buckets = {
        "no_lock": f"{BUCKET_PREFIX}-no-lock-{RANDOM_SUFFIX}",
        "lock_enabled": f"{BUCKET_PREFIX}-lock-enabled-{RANDOM_SUFFIX}",
        "retention": f"{BUCKET_PREFIX}-retention-after-creation-{RANDOM_SUFFIX}"
    }

    logger.info("Starting bucket creation with random suffix: %s", RANDOM_SUFFIX)
    for name, bucket in buckets.items():
        try:
            logger.debug("Creating bucket [%s]: %s", name, bucket)
            s3_client.create_bucket(Bucket=bucket)
        except Exception as e:
            logger.error("Failed to create bucket [%s]: %s", bucket, e)

    logger.info("Enabling versioning and object lock configuration on necessary buckets.")
    for name in ["lock_enabled", "retention"]:
        try:
            logger.debug("Enabling versioning for bucket [%s]: %s", name, buckets[name])
            s3_client.put_bucket_versioning(
                Bucket=buckets[name],
                VersioningConfiguration={'Status': 'Enabled'}
            )
        except Exception as e:
            logger.error("Failed to enable versioning for bucket [%s]: %s", buckets[name], e)

    try:
        logger.debug("Enabling object lock configuration for bucket [lock_enabled]: %s", buckets["lock_enabled"])
        s3_client.put_object_lock_configuration(
            Bucket=buckets["lock_enabled"],
            ObjectLockConfiguration={'ObjectLockEnabled': 'Enabled'}
        )
    except Exception as e:
        logger.error("Failed to enable object lock for bucket [%s]: %s", buckets["lock_enabled"], e)

    logger.info("Buckets created and configured successfully: %s", buckets)
    _save_bucket_names_to_file(buckets)
    _print_bucket_summary(buckets)
    print()

    return buckets


def _save_bucket_names_to_file(buckets):
    """Save the bucket names to a file."""
    with open('buckets.txt', 'w') as f:
        for name, bucket in buckets.items():
            f.write(f"{name}={bucket}\n")


def _print_bucket_summary(buckets):
    """Print a summary table of the created buckets."""
    summary_table = PrettyTable()
    summary_table.field_names = ["Bucket Name", "Object Lock", "Default Retention", "Bucket Versioning"]
    summary_table.align = "l"
    summary_table.add_row([buckets["no_lock"], "Disabled", "Disabled", "Disabled"])
    summary_table.add_row([buckets["lock_enabled"], "Enabled", "Disabled", "Enabled"])
    summary_table.add_row([buckets["retention"], "Disabled", "Disabled", "Enabled"])

    print("\nSummary of Buckets Created:")
    print(summary_table)


def populate_buckets(s3_client, buckets):
    """Upload test files to each bucket."""
    logger.info("Starting to populate buckets with test files.")
    for bucket in buckets.values():
        file_table = PrettyTable()
        file_table.field_names = ["File Name", "Last Modified", "Size", "Storage Class", "Legal Hold", "Retention"]
        file_table.align = "l"
        for i in range(2):
            key = f"file{i}.txt"
            try:
                logger.debug("Uploading file [%s] to bucket [%s]", key, bucket)
                s3_client.put_object(Bucket=bucket, Key=key, Body=FILE_CONTENT)

                # Mock the file details for display purposes
                last_modified = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                size = "42.0 B"
                storage_class = "Standard"
                legal_hold = "OFF"
                retention = "None"

                file_table.add_row([key, last_modified, size, storage_class, legal_hold, retention])
            except Exception as e:
                logger.error("Failed to upload file [%s] to bucket [%s]: %s", key, bucket, e)
        print(f"Summary of Files Uploaded to {bucket}:")
        print(file_table)
        print()


def update_retention_policy(s3_client, bucket):
    """Update the retention policy for a specific bucket."""
    logger.info("Updating retention policy for bucket: %s", bucket)
    try:
        s3_client.put_object_lock_configuration(
            Bucket=bucket,
            ObjectLockConfiguration={
                'ObjectLockEnabled': 'Enabled',
                'Rule': {
                    'DefaultRetention': {
                        'Mode': 'GOVERNANCE',
                        'Years': 1
                    }
                }
            }
        )
        logger.debug("Retention policy updated successfully for bucket: %s", bucket)
        retain_until = (datetime.now() + timedelta(days=365)).strftime('%Y-%m-%dT%H:%M:%SZ')
        _print_retention_policy_update(bucket, None, None, retain_until)
        print()
    except Exception as e:
        logger.error("Failed to update retention policy for bucket [%s]: %s", bucket, e)


def set_legal_hold(s3_client, bucket, key):
    """Set a legal hold on a specific file in a bucket."""
    print()
    logger.info("Setting legal hold on file [%s] in bucket [%s]", key, bucket)
    try:
        before_status = "OFF"
        after_status = "ON"
        s3_client.put_object_legal_hold(
            Bucket=bucket,
            Key=key,
            LegalHold={'Status': after_status}
        )
        logger.debug("Legal hold set successfully on file [%s] in bucket [%s]", key, bucket)
        _print_legal_hold_update(bucket, key, before_status, after_status)
    except Exception as e:
        logger.error("Failed to set legal hold on file [%s] in bucket [%s]: %s", key, bucket, e)


def set_retention(s3_client, bucket, key, days):
    """Set a retention policy on a specific file in a bucket."""
    retain_until = (datetime.now() + timedelta(days=days)).strftime('%Y-%m-%dT%H:%M:%SZ')
    print()
    logger.info("Setting retention policy on file [%s] in bucket [%s] for %d days", key, bucket, days)
    logger.debug("Retention date: %s", retain_until)
    try:
        before_retention = "None"
        s3_client.put_object_retention(
            Bucket=bucket,
            Key=key,
            Retention={
                'Mode': 'GOVERNANCE',
                'RetainUntilDate': retain_until
            },
            BypassGovernanceRetention=True
        )
        logger.debug("Retention policy set successfully on file [%s] in bucket [%s]", key, bucket)
        _print_retention_policy_update(bucket, key, before_retention, retain_until)
    except Exception as e:
        logger.error("Failed to set retention policy on file [%s] in bucket [%s]: %s", key, bucket, e)


def _print_retention_policy_update(bucket, key, before_retention, after_retention):
    """Print a summary table of the retention policy updates."""
    retention_table = PrettyTable()
    retention_table.field_names = ["Bucket", "Object", "Object Lock Enabled", "Retention Mode",
                                   "Retention Until (BEFORE)", "Retention Until (AFTER)"]
    retention_table.align = "l"

    # Populate the table with the BEFORE and AFTER information
    retention_table.add_row([bucket, key, "Enabled", "GOVERNANCE", before_retention, after_retention])

    logger.info("Retention Policy Updates:")
    print(retention_table)


def _print_legal_hold_update(bucket, key, before_status, after_status):
    """Print a summary table of the legal hold updates."""
    legal_hold_table = PrettyTable()
    legal_hold_table.field_names = ["Bucket", "Object", "Legal Hold Status (BEFORE)", "Legal Hold Status (AFTER)"]
    legal_hold_table.align = "l"

    # Populate the table with the BEFORE and AFTER information
    legal_hold_table.add_row([bucket, key, before_status, after_status])

    logger.info("Legal Hold Updates:")
    print(legal_hold_table)


def print_bucket_details(buckets):
    """Print details of the created buckets."""
    bucket_table = PrettyTable()
    bucket_table.field_names = ["Bucket Name", "Configuration"]
    bucket_table.align = "l"
    bucket_table.add_row([buckets["no_lock"], "No Lock"])
    bucket_table.add_row([buckets["lock_enabled"], "Lock Enabled"])
    bucket_table.add_row([buckets["retention"], "Retention After Creation"])
    print(bucket_table)


# Example usage
if __name__ == "__main__":
    s3_client = boto3.client('s3')
    buckets = create_buckets(s3_client)
    print_bucket_details(buckets)
    populate_buckets(s3_client, buckets)
    update_retention_policy(s3_client, buckets["retention"])
    set_legal_hold(s3_client, buckets["lock_enabled"], "file0.txt")
    set_retention(s3_client, buckets["lock_enabled"], "file1.txt", 30)
