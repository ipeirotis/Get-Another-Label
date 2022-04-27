This folder contains datasets that can be used to evaluate quality control algorithms for labeling applications in crowdsourcing

If you need help with running the program, please check the documentation at https://github.com/ipeirotis/Get-Another-Label/wiki/How-to-Run-Get-Another-Label where we give some sample instructions for two of the datasets.

In each folder you can find a README file that briefly describes the data set.

We try to keep the naming of the files in each dataset uniform. A description of the role of each input file can be found at https://github.com/ipeirotis/Get-Another-Label/wiki/Input-Files

For the categoriesfile, we typically include two files, the categories-noprior.txt and the categories-withprior.txt, so that you can experiment with the difference when the priors are estimated from the data, versus the case when they are explicitly speficied by the user.

Similarly, for the goldfile, we always include a gold-empty.txt file, which corresponds to the case of having no gold data. Sometimes, we also include files with names like gold-XXitems.txt, where we also pass to the algorithm the true answers for a few of the objects. In high-noise settings, this typically results in an improvement in the estimation.



* AdultContent is a toy, artificial dataset with 5 workers labeling 5 websites. It is used mainly to show how the GAL algorithm operates

* AdultContent2 contains approximately 100K judgements, classifying websites into 5 categories

* AdultContent3 contains approximately 50K judgements, for 500 sites classified each by 100 Mechanical Turk workers, into one of 4 categories

