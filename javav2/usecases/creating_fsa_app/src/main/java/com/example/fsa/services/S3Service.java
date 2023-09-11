/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.fsa.services;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class S3Service {

    // Put the audio file into the Amazon S3 bucket.
    public String putAudio(InputStream is, String bucket, String key) throws IOException {
        S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

        PutObjectRequest putOb = PutObjectRequest.builder()
            .bucket(bucket)
            .contentType("audio/mp3")
            .key(key)
            .build();

        s3.putObject(putOb, RequestBody.fromBytes(inputStreamToBytes(is)));
        return key;
    }

    public static byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }
}