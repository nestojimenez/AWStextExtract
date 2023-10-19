/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import wrap from "fast-word-wrap";

export class Logger {
  constructor(lineLength = 80) {
    this.lineLength = lineLength;
  }

  /**
   * @param {string} message
   */
  log(message) {
    console.log(message);
    return Promise.resolve();
  }

  hr() {
    return "\n", "*".repeat(this.lineLength), "\n";
  }

  /**
   * @param {string} message
   */
  box(message) {
    const linePrefix = "*  ";
    const lineSuffix = "  *";

    const maxContentLength = this.lineLength - (linePrefix + lineSuffix).length;
    const chunks = message
      .split("\n")
      .map((l) => l && wrap(l, maxContentLength).split("\n"))
      .flat();

    return `
${"*".repeat(this.lineLength)}
${chunks
  .map(
    (c) =>
      `${linePrefix}${
        c + " ".repeat(maxContentLength - c.length)
      }${lineSuffix}`,
  )
  .join("\n")}
${"*".repeat(this.lineLength)}
`;
  }

  /**
   * Log a horizontal rule to the console. If a message is provided,
   * log a section header.
   * @param {string?} message
   */
  logSeparator(message) {
    if (!message) {
      console.log(this.hr());
    } else {
      console.log(this.box(message));
    }
  }
}
