import com.ipeirotis.readability.engine.Readability;
import com.ipeirotis.readability.enums.MetricType;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.Scanner;

/**
 * Main class for the eli5? program. All of the high-level logic is done here.
 * <p>
 * This whole project was created for CIS 365 - Artificial Intelligence to create a NLP agent
 *
 * @author Jaxon Wright, Zack Patterson
 */
public class Main {
    private static Scanner in; //read user input
    private static final String LINE = "___________________________________________________\n";
    private static int queryCount = 0; //How many queries have been made in the session
    private static int maxCount = -1; //Max count of queries to display. -1 is no max count
    private static int currCount = 0; //Current count of results queried
    //Default submission sorting
    private static SubmissionSearchPaginator.SearchSort sortFlag = SubmissionSearchPaginator.SearchSort.RELEVANCE;

    /**
     * Main class that is run initially.
     * @param args default args
     * @throws OAuthException
     */
    public static void main(String[] args) throws OAuthException {
        //UserAgent visible to reddit
        UserAgent myUserAgent = UserAgent.of("desktop", "eliFiveScript", "v0.1", "jaxzac_eli5");
        //OAuth2 credentials needed for reddit api access
        Credentials credentials = Credentials.script("jaxzac_eli5", "patterson", "ymtPEx6s5WRTBg", "GLLPDQMJdAc-93BomekPLU9Np8Y");
        //Reddit Client used to connect to reddit API
        RedditClient redditClient = new RedditClient(myUserAgent);
        OAuthData authData; //Authentication data used to connect

        //cool looking header for maximum coolness
        System.out.print("       _ _ _____ ___  \n" +
                "      | (_) ____|__ \\   | a tool to determine if an answer on /r/eli5\n" +
                "   ___| |_| |__    ) |  | is readable by a 5-year-old\n" +
                "  / _ \\ | |___ \\  / /   |\n" +
                " |  __/ | |___) ||_|    | made by jaxon wright and zack patterson\n" +
                "  \\___|_|_|____/ (_)    | version 0.1\n" + LINE);

        System.out.print("connecting to reddit...");

        authData = redditClient.getOAuthHelper().easyAuth(credentials); //apply authentication credentials
        redditClient.authenticate(authData); //authenticate with reddit's servers

        if (redditClient.getOAuthHelper().getAuthStatus().toString().equals("AUTHORIZED")) {
            System.out.println("\rsuccessfully connected");
        } else {
            System.out.println("\rconnection failed. Exiting");
            return;
        }
        searchQuery(redditClient);
    }

    /**
     * Prints help dialog to show usage
     */
    private static void printHelpDialog() {
        System.out.println("Here are different usages:");
        System.out.println("\tSearch terms: [search terms]");
        System.out.println("\tSort results (default: relevance): -sort [hot, top, new] [search terms]");
        System.out.println("\tShow a max of # results: -maxcount [#] [search terms]");
        System.out.println("\tSort and show max: -sort [hot, top, new] -maxcount [#] [search terms]");
    }

    /**
     * parses flags inside of user searches and strips them out before sent to reddit
     *
     * @param search       the search query entered by the user
     * @param redditClient the current reddit client connected to reddit
     */
    private static String parseFlags(String search, RedditClient redditClient) {
        String ret = "";
        if (search.equals("-q")) System.exit(0);
        if (search.equals("-h") || search.equals("-help")) { //user needs help
            printHelpDialog();
            searchQuery(redditClient);
        }
        //sorting modification
        if (search.startsWith("-sort relevance")) {
            //already sorted by relevance by default
            search = search.replaceFirst("-sort relevance ", "");
        } else if (search.startsWith("-sort top")) {
            sortFlag = SubmissionSearchPaginator.SearchSort.TOP;
            search = search.replaceFirst("-sort top ", "");
        } else if (search.startsWith("-sort new")) {
            sortFlag = SubmissionSearchPaginator.SearchSort.NEW;
            search = search.replaceFirst("-sort new ", "");
        } else if (search.startsWith("-sort hot")) {
            sortFlag = SubmissionSearchPaginator.SearchSort.HOT;
            search = search.replaceFirst("-sort hot ", "");
        } else if (search.startsWith("-sort")) {
            System.out.println("Unknown sorting value. Please use top, new or hot");
            searchQuery(redditClient);
        }
        ret = search;
        if (ret.startsWith("-maxcount")) {
            maxCount = Integer.parseInt(search.substring(10, search.indexOf(" ", 10)));
            ret = search.substring(search.indexOf(" ", 10) + 1);
        }
        return ret;
    }

    /**
     * Handles the user's search query
     * @param redditClient reddit client that is connected to reddit API
     */
    private static void searchQuery(RedditClient redditClient) {
        currCount = 0; //reset to zero
        maxCount = -1; //reset to unlimited
        sortFlag = SubmissionSearchPaginator.SearchSort.RELEVANCE; //reset sorting flag
        queryCount++;
        String enterATopic = (queryCount == 1) ? "Enter a topic:" : "\nEnter another topic (-q to quit):";

        System.out.println(enterATopic);
        in = new Scanner(System.in); //Read the user input
        String search = in.nextLine();

        search = parseFlags(search, redditClient);

        SubmissionSearchPaginator p = new SubmissionSearchPaginator(redditClient, search);
        p.setSubreddit("explainlikeimfive");
        p.setTimePeriod(TimePeriod.ALL);
        p.setSearchSorting(sortFlag);

        if (p.next().isEmpty()) {
            System.out.println("No results. Try to be more specific");
            searchQuery(redditClient);
        }
        for (Submission link : p.next()) {
            if (currCount==maxCount) break;
            System.out.println(LINE);
            System.out.printf("%s upvotes - %s (%s)\n", link.getScore(), link.getTitle(), link.getShortURL());
            System.out.print("\tcalculating 5-year-oldness");
            calculateReadability(redditClient, link);
            currCount++;
        }
        searchQuery(redditClient);
    }

    /**
     * Determines how readable the top three comments of a submission are.
     * @param reddit current reddit client connected to reddit
     * @param sub current submission to analyze
     */
    private static void calculateReadability(RedditClient reddit, Submission sub) {
        double fleschReading;
        //Query submission individually to get comments
        Submission s = reddit.getSubmission(sub.getId());
        //Get the top comment
        if (!s.getComments().isEmpty()) {
            for (int i = 0; i < s.getComments().getImmediateSize(); i++) {
                if (i == 3) break;
                CommentNode topNode = s.getComments().get(i);

                Readability r = new Readability(topNode.getComment().getBody());
                System.out.print("\r"); //remove the calculating text
                fleschReading = r.getMetric(MetricType.FLESCH_READING);
                switch (i) {
                    case 0:
                        System.out.print("\tThe best answer ");
                        break;
                    case 1:
                        System.out.print("\tThe second-best ");
                        break;
                    case 2:
                        System.out.print("\tThe third-best ");
                        break;
                }
                System.out.print("by /u/" + topNode.getComment().getAuthor());
                System.out.print(" is at a " + calcReadingGrade(fleschReading) + " reading level\n");
            }
        } else {
            System.out.print("\tNo answers yet");
        }
    }

    /**
     * Returns an easier-to-understand result of the Flesch readability algorithm
     * @param score flesch reading score
     * @return value more readable by the average person
     */
    private static String calcReadingGrade(double score) {
        if (score > 90) return "5th grade (Very easy)";
        if (score > 80) return "6th grade (Easy)";
        if (score > 70) return "7th grade (Fairly easy)";
        if (score > 60) return "middle school (Plain English)";
        if (score > 50) return "high school (Fairly difficult)";
        if (score > 30) return "college (Difficult)";
        return "college graduate (Very difficult)";
    }
}
