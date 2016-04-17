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

public class Main {
    private static Scanner in;
    private static final String LINE = "___________________________________________________\n";
    private static int queryCount = 0;

    public static void main(String[] args) throws OAuthException {
        //UserAgent visible to reddit
        UserAgent myUserAgent = UserAgent.of("desktop", "eliFiveScript", "v0.1", "jaxzac_eli5");
        //OAuth2 credentials needed for reddit api access
        Credentials credentials = Credentials.script("jaxzac_eli5", "patterson", "ymtPEx6s5WRTBg", "GLLPDQMJdAc-93BomekPLU9Np8Y");
        RedditClient redditClient = new RedditClient(myUserAgent);
        OAuthData authData;

        in = new Scanner(System.in);

        //cool looking header for maximum cool
        System.out.print("       _ _ _____ ___  \n" +
                "      | (_) ____|__ \\   | a tool to determine if an answer on /r/eli5\n" +
                "   ___| |_| |__    ) |  | is readable by a 5-year-old\n" +
                "  / _ \\ | |___ \\  / /   |\n" +
                " |  __/ | |___) ||_|    | made by jaxon wright and zack patterson\n" +
                "  \\___|_|_|____/ (_)    | version 0.1\n" + LINE);

        System.out.print("connecting to reddit...");

        authData = redditClient.getOAuthHelper().easyAuth(credentials);
        redditClient.authenticate(authData);
        if (redditClient.getOAuthHelper().getAuthStatus().toString().equals("AUTHORIZED")) {
            System.out.println("\rsuccessfully connected");
        } else {
            System.out.println("\rconnection failed. Exiting");
            return;
        }
        searchQuery(redditClient);
    }

    private static void searchQuery(RedditClient redditClient) {
        queryCount++;
        String enterATopic = (queryCount == 1) ? "Enter a topic:" : "\nEnter another topic (-q to quit):";
        System.out.println(enterATopic);
        String search = in.nextLine();
        //User wants to quit
        if (search.equals("-q")) return;

        SubmissionSearchPaginator p = new SubmissionSearchPaginator(redditClient, search);
        p.setSubreddit("explainlikeimfive");
        p.setTimePeriod(TimePeriod.ALL);
        p.setSearchSorting(SubmissionSearchPaginator.SearchSort.RELEVANCE);

        if (p.next().isEmpty()) {
            System.out.println("No results. Try to be more specific");
            searchQuery(redditClient);
        }
        for (Submission link : p.next()) {
            System.out.println(LINE);
            System.out.printf("%s upvotes - %s (%s)\n", link.getScore(), link.getTitle(), link.getShortURL());
            System.out.print("\tcalculating 5-year-oldness");
            calculateReadability(redditClient, link);
        }
        searchQuery(redditClient);
    }

    private static void calculateReadability(RedditClient reddit, Submission sub) {
        double fleschReading;
        queryCount++;
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
