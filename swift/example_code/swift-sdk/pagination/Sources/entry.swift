// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
//
// An example to demonstrate the use of pagination with the AWS SDK
// for Swift.

import AWSS3
import Foundation

@main
struct PaginatorExample {
    static func main() async {
        let PAGE_SIZE = 10
        let client: S3Client

        do {
            client = try await S3Client()
        } catch {
            print("ERROR: Unable to create the Amazon S3 client.")
            return
        }

        // Start pagination by using the `Paginated` version of the
        // `listBuckets(input:)` function. Each page has up to 10 buckets in
        // it.

        let pages = client.listBucketsPaginated(
            input: ListBucketsInput(maxBuckets: PAGE_SIZE)
        )

        // Go through the pages, printing each page's buckets to the console.
        // The paginator handles the continuation tokens automatically.

        var pageNumber = 0

        do {
            for try await page in pages {
                pageNumber += 1

                guard let pageBuckets = page.buckets else {
                    print("ERROR: No buckets returned in page \(pageNumber)")
                    continue
                }

                print("\nPage \(pageNumber):")

                // Print this page's bucket names.

                for bucket in pageBuckets {
                    print("  " + (bucket.name ?? "<unknown>"))
                }
            }
        } catch {
            print("ERROR: Unable to process bucket list pages.")
        }
    }
}
