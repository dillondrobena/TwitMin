import java.sql.SQLException;

//Controller and entry point for twitter miner
public class Controller {
	
	//All of these are required to run the miner!
	private static String dbURL;
	private static String dbUserName;
	private static String dbPassword;
	private static String OAuthConsumerKey;
	private static String OAuthConsumerSecret;
	private static String OAuthAccessToken;
	private static String OAuthAccessTokenSecret;

	public static void main(String[] args) throws SQLException, InterruptedException {
		dbURL = args[0];
		dbUserName = args[1];
		dbPassword = args[2];
		OAuthConsumerKey = args[3];
		OAuthConsumerSecret = args[4];
		OAuthAccessToken = args[5];
		OAuthAccessTokenSecret = args[6];
		
		//Create base class, pass it database and Twitter credentials
		TwitterMiner tw = new TwitterMiner(dbURL, dbUserName, dbPassword, OAuthConsumerKey, OAuthConsumerSecret, OAuthAccessToken, OAuthAccessTokenSecret);
		TwitterMiner.TwitterStreamer ts = tw.new TwitterStreamer();
		ts.beginStreaming();
		//Don't bother using TwitterMiner.TwitterQuery as it very limited at the moment
		//Stick with streaming data
		
	}
}
