/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.fsa.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.fsa.services.PollyService;
import com.example.fsa.services.S3Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class PollyHandler implements RequestHandler<Map<String, Object>, String> {
    @Override
    public String handleRequest(Map<String, Object> requestObject, Context context) {
        S3Service s3Service = new S3Service();
        PollyService pollyService = new PollyService();
        String myValues = requestObject.toString();
        context.getLogger().log("*** ALL values: " +myValues);
        String translatedText = getTranslatedText(myValues);
        String key = getKeyName(myValues);
        context.getLogger().log("*** About to get bucket");
        String bucket = getBucketName(myValues);
        context.getLogger().log("*** My Bucket: " +bucket);
        String newFileName = convertFileEx(key);
        context.getLogger().log("*** Translated Text: " +translatedText +" and new key is "+newFileName);
        try {
            InputStream is = pollyService.synthesize(translatedText);
            String audioFile = s3Service.putAudio(is, bucket, newFileName);
            context.getLogger().log("You have successfully added the " +audioFile +"  in the S3 bucket");
            return audioFile ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // This method extracts the value following translated_text using Reg Exps.
    private String getTranslatedText(String myString) {
        // Define the regular expression pattern to match the "translated_text" key-value pair.
        String pattern = "translated_text=([^,}]+)";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(myString);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    // This method extracts the key using Reg Exps.
    private static String getKeyName(String input) {
        String pattern = "object=([^,}]+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        if (m.find()) {
            System.out.println("Found value: " + m.group(1));
            return m.group(1);
        }
        return "";
    }

    // This method extracts the bucket using Reg Exps.
    private static String getBucketName(String input) {
        String pattern = "bucket=([^,]+)";
         Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        if (m.find()) {
            System.out.println("Found value: " + m.group(1));
            return m.group(1);
        }
        return "";
    }

    // Replaces the file extension to mp3.
    public static String convertFileEx(String originalFileName) {
        String newExtension = "mp3";

        // Get the index of the last dot (.) in the filename.
        int lastIndex = originalFileName.lastIndexOf(".");
        if (lastIndex > 0) {
            // Extract the file name without extension.
            String fileNameWithoutExtension = originalFileName.substring(0, lastIndex);
            return fileNameWithoutExtension + "." + newExtension;
        }
        return "";
    }
}