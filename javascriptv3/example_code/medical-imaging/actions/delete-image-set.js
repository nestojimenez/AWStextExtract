/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import {fileURLToPath} from "url";

// snippet-start:[medical-imaging.JavaScript.imageset.deleteImageSetV3]
import {DeleteImageSetCommand} from "@aws-sdk/client-medical-imaging";
import {medicalImagingClient} from "../libs/medicalImagingClient.js";

/**
 * @param {string} datastoreId - The data store ID.
 * @param {string} imageSetId - The image set ID.
 */
export const deleteImageSet = async (datastoreId = "xxxxxxxxxxxxxxxx", imageSetId = "xxxxxxxxxxxxxxxx") => {
    const response = await medicalImagingClient.send(
        new DeleteImageSetCommand({datastoreId: datastoreId, imageSetId: imageSetId})
    );
    console.log(response);
    // {
    //    '$metadata': {
    //         httpStatusCode: 200,
    //         requestId: '6267bbd2-eaa5-4a50-8ee8-8fddf535cf73',
    //         extendedRequestId: undefined,
    //         cfId: undefined,
    //         attempts: 1,
    //         totalRetryDelay: 0
    //     },
    //     datastoreId: 'xxxxxxxxxxxxxxxx',
    //     imageSetId: 'xxxxxxxxxxxxxxx',
    //     imageSetState: 'LOCKED',
    //     imageSetWorkflowStatus: 'DELETING'
    // }
    return response;
};
// snippet-end:[medical-imaging.JavaScript.imageset.deleteImageSetV3]

// Invoke main function if this file was run directly.
if (process.argv[1] === fileURLToPath(import.meta.url)) {
    await deleteImageSet("728f13a131f748bf8d87a55d5ef6c5af", "906e1c0ff5e9c69a14a9e4d36e0cea1e");
}
