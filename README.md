# Description

Mechanical Turk and other "micro-outsourcing" platforms allow the easy collection of annotation data from a wide variety of onliner workers. Unfortunately, the results returned back from the workers are of imperfect quality: No worker is perfect and each worker commits different types of errors. Some workers are nothing more than spammers doing minimal, if any, real work.

The goal of this project is to create tools that allow requesters to infer the "true" results and the underlying quality of the workers, for a given task, by examining the labelers submitted by the workers.

The requesters will also be able to examine which workers are doing a good work for the given task, and reward these workers appropriately. The tool also provides the ability to detect spammers, allowing the requester to potentially block the spammer worker from working any future projects.

The ideas behind this work are described in Quality Management on Amazon Mechanical Turk, by Ipeirotis, Wang, and Provost, which was presented at HCOMP 2010.

## How to Run

For details on how to run the algorithm see https://github.com/ipeirotis/Get-Another-Label/wiki/How-to-Run-Get-Another-Label


### New features in Version 2.1

* In the file object-probabilities.txt, we now report the expected misclassification cost of each example either following the DawidSkene (DS Cost) or following a naive vote (MajorityVote Cost), and we also list the misclassification cost if we just classify the item according to the prior probabilities (NoVote Cost). Using these numbers, we can estimate the improvement in data quality as a result of the labeling process. If the DS Cost is 0, then the algorithm is confident that the item has the correct label. The higher the DS cost (and the closer it is to the NoVote cost) the lower the improvement in data quality.


### New features in Version 2.0

* Better organization of output files with more information about the workers' quality
* Code refactored and works with data files that contain millions of entries

### New features in Version 1.0

* We now report a "quality score" that ranges from 0% to 100%
* We now show the number of objects labeled by each worker.

### New features in Version 0.1

* Ability to upload labels for objects with known classification (i.e., "gold" data) for improved estimation of labeler's quality. See http://behind-the-enemy-lines.blogspot.com/2010/09/worker-evaluation-in-crowdsourcing-gold.html for a longer discussion
* Ability to customize classification costs. For example, classifying "porn" as "not porn" may be costlier than classifying "not porn" as "porn": the first error may expose children to porn, while the second simply blocks some users from seeing the resource.

## Miscellaneous

A web-accessible demo of this code is available at http://qmturk.appspot.com/

A .NET version of the code, by Julian Urbano, is available at http://code.google.com/p/get-another-label-dotnet/

## Deprecation warning

 The 2.x series his is the last stand-alone version of the code. 
 
 Soon, we will make available a REST-based web service that has significant additional functionality (support for streaming and incremental updates, persistence, etc). 
 
 Stay tuned. The code, which is currently under developerment, is available at https://github.com/ipeirotis/DSaS
