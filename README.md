## Description

Mechanical Turk and other "micro-outsourcing" platforms allow the easy collection of annotation data from a wide variety of onliner workers. Unfortunately, the results returned back from the workers are of imperfect quality: No worker is perfect and each worker commits different types of errors. Some workers are nothing more than spammers doing minimal, if any, real work.

The goal of this project is to create tools that allow requesters to infer the "true" results and the underlying quality of the workers, for a given task, by examining the labelers submitted by the workers.

The requesters will also be able to examine which workers are doing a good work for the given task, and reward these workers appropriately. The tool also provides the ability to detect spammers, allowing the requester to potentially block the spammer worker from working any future projects.

The ideas behind this work are described in Quality Management on Amazon Mechanical Turk, by Ipeirotis, Wang, and Provost, which was presented at HCOMP 2010.

## Downloading

[This Link](http://ipeirotis-assets.s3.amazonaws.com/get-another-label/get-another-label-latest.zip) contains the latest version, straight from our Continuous Integration builds.

## How to Run

For details on how to run the algorithm see https://github.com/ipeirotis/Get-Another-Label/wiki/How-to-Run-Get-Another-Label

## Deprecation warning

 The 2.x series his is the last stand-alone version of the code. 
 
 Soon, we will make available a REST-based web service that has significant additional functionality (support for streaming and incremental updates, persistence, etc). Stay tuned. The web service will be available at http://www.project-troia.com/
 
## Miscellaneous

A web-accessible demo of is available at http://qmturk.appspot.com/

A .NET version of the code, by Julian Urbano, is available at http://code.google.com/p/get-another-label-dotnet/
