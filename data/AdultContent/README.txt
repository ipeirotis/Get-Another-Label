Example Application: Porn or Not Porn? 

This is a toy dataset, mainly for demonstration and basic testing purposes

We have five workers (worker1 to worker5), five objects (url1 to url5), and two classes (porn and notporn). 

The correct (but unknown) label for url1, url3, and url5 is notporn; for url2 and url4 is porn.

The goal is to find the correct class of each example using the labels provided by the workers.

The workers have varying levels of quality.

* worker1 is lazy, and gives the answer porn all the time
* worker2 is ok but not great. For example, labeled object5 as porn even though it is notporn
* worker3 and worker4 are high-quality workers
* worker5 is malicious, giving always the incorrect answer, trying to fool the system

In this example, we know the correct (gold) classification for two of the examples:

* url1 is notporn
* url2 is porn

