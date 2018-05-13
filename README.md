<img src="http://i.imgur.com/VNfY78v.png" width="150px" alt="eli5" />
===================

About
-----
This project was created for CIS 365 Artificial Intelligence at Grand Valley State University by Jaxon Wright and Zack Patterson. The project scrapes posts on the eli5 (Explain Like I'm Five) subreddit on Reddit. The goal is to take the user's query and determine the reading level of the highest-rated comments and presents it to the user, so that they can make informed decisions about threads based on how easy the explanations are to understand.

Preview
---
Here is what the program looks like. Formatted pretty nicely for a terminal program, eh?

<img src="https://i.imgur.com/VVYclGs.png" width="500px" alt="screen1" />

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
regular search

    [search terms]
sort the results (default is relevance)

    -sort [hot, top, new] [search terms]
limit the max amount of results

    -maxcount [#] [search terms]
sort and limit max amout of results

    -sort [hot, top, new] -maxcount [#] [search terms]
display commands

    -help

3rd Party Libraries Used
----

 - [JRAW (Java Reddit API Wrapper) by thatJavaNerd](https://github.com/thatJavaNerd/JRAW)
	 - Used to easily connect to the Reddit API through the java application
 - [ReadabilityMetrics API by Panos Ipeirotis](https://github.com/ipeirotis/ReadabilityMetrics)
	 - Used to calculate comment readability by using the Flesch Reading Ease Index.
