/// A class containing functions to access Amazon Simple Storage Service (S3).
/// The functions are defined by the protocol ``S3SessionProtocol``, which is
/// then implemented by S3Session, which simply forwards requests to the AWS
/// SDK for Swift.
///
/// For testing purposes (see the file `S3SessionTests.swift`), the protocol
/// is implemented by ``S3SessionMock``.
///
/// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
/// SPDX-License-Identifier: Apache-2.0

import Foundation
import ClientRuntime
import AWSS3

// snippet-start:[s3.swift.listbuckets.s3sessionprotocol]
/// A protocol defining the Amazon S3 functions we want to mock
/// during testing.
public protocol S3SessionProtocol {
    func listBuckets(input: ListBucketsInput) async throws -> ListBucketsOutputResponse
}
// snippet-end:[s3.swift.listbuckets.s3sessionprotocol]

// snippet-start:[s3.swift.listbuckets.s3session]
/// An implementation of ``S3SessionProtocol`` that calls right through to
/// equivalent functions in the AWS SDK for Swift.
public struct S3Session: S3SessionProtocol {
    let client: S3Client
    let awsRegion: String

    /// Initialize the session to use the specified AWS Region.
    ///
    /// - Parameter region: The AWS Region to use. Default is `us-east-1`.
    init(region: String = "us-east-1") throws {
        self.awsRegion = region

        // Create an ``S3Client`` to use for AWS SDK for Swift calls.
        self.client = try S3Client(region: awsRegion)
    }

    /// Call through to the ``S3Client`` function `listBuckets()`.
    /// - Parameter input: The input to pass through to the SDK function
    ///   `listBuckets()`.
    /// - Returns: A ``ListBucketsOutputResponse`` with the returned data.
    public func listBuckets(input: ListBucketsInput) async throws
            -> ListBucketsOutputResponse {
        return try await self.client.listBuckets(input: input)
    }
}
// snippet-end:[s3.swift.listbuckets.s3session]

// snippet-start:[s3.swift.listbuckets.s3manager]
/// A class that uses an object that implements ``S3SessionProtocol`` to
/// access Amazon S3.
public class S3Manager {
    var client: S3SessionProtocol

    // snippet-start:[s3.swift.listbuckets.s3manager.init]
    /// Initialize the ``S3Manager`` to call Amazon S3 functions using the
    /// specified object that implements ``S3SessionProtocol``.
    ///
    /// - Parameter session: The session object to use when calling Amazon S3.
    init(session: S3SessionProtocol) {
        self.client = session
    }
    // snippet-end1:[s3.swift.listbuckets.s3manager.init]

    // snippet-start:[s3.swift.listbuckets.getallbuckets]
    /// Return an array containing information about every available bucket.
    /// 
    /// - Returns: An array of ``S3ClientTypes.Bucket`` objects describing
    ///   each bucket.
    public func getAllBuckets() async throws -> [S3ClientTypes.Bucket] {
        let output = try await client.listBuckets(input: ListBucketsInput())

        guard let buckets = output.buckets else {
            return []
        }
        return buckets
    }
    // snippet-end:[s3.swift.listbuckets.getallbuckets]
}
// snippet-end:[s3.swift.listbuckets.s3manager]
