<img src="https://github.com/JaxonWright/jaxonwright.github.io/blob/master/eli5logo.png?raw=true" width="150px" alt="eli5" />
===================

About
-----
This project was created for CIS 365 Artificial Intelligence at Grand Valley State University by Jaxon Wright and Zack Patterson. The project scrapes posts on the eli5 (Explain Like I'm Five) subreddit on Reddit. The goal is to take the user's query and determine the reading level of the highest-rated comments and presents it to the user, so that they can make informed decisions about threads based on how easy the explanations are to understand.

Use It
---
Follow these steps to use the program:

#### Windows
 1. Download the [eli5.zip](https://git.io/vwtcl)
 2. Extract the file
 3. open *run.bat*

#### Linux
 1. Download the [eli5.zip](https://git.io/vwtcl)
 2. Extract the file
 3. enter "java -jar [path to jar]" in terminal
 
Yes, it runs in the command line. Deal with it.

### Commands
 1. [search terms]
 2. -sort [hot, top, new] [search terms]
 3. -maxcount [#] [search terms]
 4. -sort [hot, top, new] -maxcount [#] [search terms]
 5. -help

3rd Party Libraries Used
----

 - [JRAW (Java Reddit API Wrapper) by thatJavaNerd](https://github.com/thatJavaNerd/JRAW)
	 - Used to easily connect to the Reddit API through the java application
 - [ReadabilityMetrics API by Panos Ipeirotis](https://github.com/ipeirotis/ReadabilityMetrics)
	 - Used to calculate comment readability by using the Flesch Reading Ease Index.
