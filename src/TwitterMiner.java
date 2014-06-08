import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterMiner {

	private TwitterFactory tf;
	ConfigurationBuilder cb;
	protected Database db;

	public int pPSCounter = 0;
	public int tPSCounter = 0;
	public int tgPSCounter = 0;
	//Set up credentials and database auth
	public TwitterMiner(String dbURL, String dbUserName, String dbPassword, String oAuthConsumerKey, String oAuthConsumerSecret, String oAuthAccessToken, String oAuthAccessTokenSecret) throws SQLException
	{
		//Set up Twitter credentials
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(oAuthConsumerKey)
		.setOAuthConsumerSecret(oAuthConsumerSecret)
		.setOAuthAccessToken(oAuthAccessToken)
		.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		//Set up database
		db = new Database(dbURL, dbUserName, dbPassword);
		//Optional, but set up statistics viewer anyway
		Statistics stats = new Statistics(this);
	}
	// Don't bother using this class, the API is heavily restricted and I'm still trying to find an
	// effective way of querying
	public class TwitterQuery
	{
		private Twitter twitter;
		private String country = "";
		private List<Object> mineLater = new ArrayList<Object>();

		public TwitterQuery()
		{
			//Can only use cb.build() once, don't run this class and streaming class concurrently
			tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();
		}

		public void setCountry(String country)
		{
			this.country = country;
		}
		//Get top trends and create users out of each one
		public void mineTrends() throws InterruptedException
		{
			setCountry("United States");
			Trends tr = getTopTrends(getCountryId());
			for (int i = 0; i < tr.getTrends().length; i++)
			{
				Query query = new Query(tr.getTrends()[i].getName());
				QueryResult qr = getQueryResults(query);

				for (Status status : qr.getTweets())
				{
					User user = status.getUser();
					UserObject userObj = new UserObject(user, status);
					mineUser(userObj);
				}
			}
		}
		//Starts by mining followers then mining tweets
		public void mineUser(UserObject userToMine) throws InterruptedException
		{
			User user = userToMine.getUser();
			try {
				//PagableResponseList<User> followers;
				IDs followers;
				do
				{
					//Adding cursor element to try and catch twitter exception, slow process
					//but can't think of a better alternative at the moment
					followers = twitter.getFollowersIDs(user.getId(), userToMine.getCursor());
					//	ResponseList<User> users = twitter.lookupUsers(followers.getIDs());
					long[] followersList = followers.getIDs();
					for (long id : followersList)
					{
						if (id != 0)
						{
							//Usually run out of rate limit before finishing mining user's followers
							User follower = twitter.showUser(id);
							System.out.println("Creating following from : " + follower.getScreenName() + " for user " + user.getScreenName());
							UserObject userObject = new UserObject(follower);
							userObject.run();
							db.addFollowing(userObject.getUser().getScreenName(), user.getScreenName());
						}
					}
					//Experiment between getFollowersList() and getFollowersIDs
					//followers = twitter.getFollowersList(user.getId(), cursor);
					/*	for (User usr : users)
					{
						mineLater.add(usr);
						System.out.println("Creating following from : " + usr.getScreenName());
						createUser(usr);
						db.addFollowing(usr.getScreenName(), user.getScreenName());
					} */
					//Adjust cursor to next page
					userToMine.setCurser(followers.getNextCursor());
				} while(followers.hasNext());
				for (Status status : twitter.getUserTimeline(user.getScreenName(), new Paging(1, 500)))
				{
					//Mine user's most recent statuses to build a proper basis
					Tweet tweet = new Tweet(status);
					tweet.run();
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Thread.sleep(60000);
				//On exception, wait 1 minute to build up rate permissions and run where the miner left off
				mineUser(userToMine);
			}
		}

		public ResponseList<Location> getAllCountries()
		{
			try {
				ResponseList<Location> locations = twitter.getAvailableTrends();
				return locations;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		//Make sure to setCountry() before running this method
		public int getCountryId()
		{
			try {
				ResponseList<Location> locations = twitter.getAvailableTrends();
				for (Location location : locations) {
					if (location.getName().toString().equals(country))
					{
						return location.getWoeid();
					}
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		//Get top trends by code, must run getCountryId() before running this
		public Trends getTopTrends(int countryCode)
		{
			try {
				Trends trends = twitter.getPlaceTrends(countryCode);
				return trends;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Example, trends.getTrends()[i].getName())
			return null;
		}
		//Returns the results of searching for a tag or a user
		public QueryResult getQueryResults(Query topic)
		{
			QueryResult result;
			try {
				result = twitter.search(topic);
				return result;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Example, for (Status status : result.getTweets()) print "@" + status.getUser().getScreenName() + ":" + status.getText()
			return null;
		}
		//Experimenting between querying and streaming, use this to close Query credentials and build streamer
		public void closeQuery()
		{
			try {
				twitter.invalidateOAuth2Token();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public class TwitterStreamer
	{
		private TwitterStream twitterStream;
		//Build streamer
		public TwitterStreamer()
		{
			twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		}
		//Listen to public stream
		public void beginStreaming()
		{
			StatusListener listener = new StatusListener() {
				@Override
				public void onException(Exception arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onDeletionNotice(StatusDeletionNotice arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onScrubGeo(long arg0, long arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStallWarning(StallWarning arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStatus(Status status) {
					//On status, create new user and tweet
					User user = status.getUser();
					UserObject userObj = new UserObject(user, status);
					Tweet tweet = new Tweet(status);
					userObj.run();
					tweet.run();

				}

				@Override
				public void onTrackLimitationNotice(int arg0) {
					// TODO Auto-generated method stub

				}
			}; 
			twitterStream.addListener(listener);
			twitterStream.sample();
		}
	}

	public class UserObject implements Runnable
	{
		//Keep track of user location when mining large data
		long cursor = -1;
		long id;
		User user = null;
		Status status = null;

		@Override
		public void run()
		{
			if (status == null) createUser(user);
			else 
			{
				createUser(user, status);
			}
		}

		public UserObject(long id, long cursor)
		{
			this.cursor = cursor;
			this.id = id;
		}

		public UserObject(User user) {
			this.user = user;
		}

		public UserObject(User user, Status status)
		{
			this.user = user;
			this.status = status;
		}
		//Build user profile to add to database
		public void createUser(User user)
		{
			List<Object> profile = new ArrayList<Object>();
			profile.add(user.getScreenName());
			profile.add(user.getCreatedAt());
			profile.add(user.getFavouritesCount());
			profile.add(user.getFollowersCount());
			profile.add(user.getFriendsCount());
			profile.add(user.getLang());
			profile.add(user.getLocation());
			profile.add(user.getListedCount());
			profile.add(user.getStatusesCount());
			profile.add(user.getURL());
			profile.add("0");
			profile.add(user.getId());
			profile.add(user.getName());
			cleanResults(profile);
			System.out.println("Created user: " + profile.get(0));
			db.createProfile(profile);
			//Keep track of daily followers, update this often for useful statistics
			db.updateFollowingCount(user.getScreenName(), user.getFollowersCount());
			pPSCounter++; //Counter to watch profiles mined per second
		}
		//Same as above, except status is already given
		public void createUser(User user, Status status)
		{
			List<Object> profile = new ArrayList<Object>();
			profile.add(user.getScreenName());
			profile.add(user.getCreatedAt());
			profile.add(user.getFavouritesCount());
			profile.add(user.getFollowersCount());
			profile.add(user.getFriendsCount());
			profile.add(user.getLang());
			profile.add(user.getLocation());
			profile.add(user.getListedCount());
			profile.add(user.getStatusesCount());
			profile.add(user.getURL());
			String geo_pos = "0";
			if (status.getGeoLocation() != null) 
			{
				geo_pos += status.getGeoLocation().getLatitude();
				geo_pos += " " + status.getGeoLocation().getLongitude();
			}
			profile.add(geo_pos);
			profile.add(user.getId());
			profile.add(user.getName());
			cleanResults(profile);
			System.out.println("Created user: " + profile.get(0));
			db.createProfile(profile);
			db.updateFollowingCount(user.getScreenName(), user.getFollowersCount());
			pPSCounter++;
		}

		public User getUser()
		{
			return user;
		}

		public long getCursor()
		{
			return cursor;
		}

		public void setCurser(long num)
		{
			cursor = num;
		}
	}
	//Possibly add Tweet class as subclass to UserObject? Performance boost?
	public class Tweet implements Runnable
	{
		Status status = null;

		@Override
		public void run()
		{
			createTweet(status);
		}

		public Tweet(Status status)
		{
			this.status = status;
		}
		//Add information to list to write to database
		public void createTweet(Status status)
		{
			List<Object> tweet = new ArrayList<Object>();
			tweet.add(status.getId());
			tweet.add(status.getText());
			tweet.add(status.getCreatedAt());
			tweet.add(status.getRetweetCount());
			String origOwner = status.getUser().getScreenName();
			if (status.isRetweet())
			{
				Status tempStatus = status.getRetweetedStatus();
				while(tempStatus.isRetweet())
				{
					tempStatus = tempStatus.getRetweetedStatus();
				}
				origOwner = tempStatus.getUser().getScreenName();
			}
			tweet.add(origOwner);
			cleanResults(tweet);
			System.out.println("Creating tweet from user: " + status.getUser().getScreenName());
			db.createTweet(tweet);
			tPSCounter++; //Tweets per second counter
			createTag(status); //Check if there is a tag in the tweet
		}
		//Create tag if it exists
		public void createTag(Status status)
		{
			String text = status.getText();
			System.out.println("Tweeted text: " + status.getText());
			if (text.contains("#"))
			{
				String args[] = text.split(" ");
				for (int i = 0; i < args.length; i++)
				{
					if (!args[i].equals("") && args[i].charAt(0) == '#') 
					{
						String tag = args[i].replaceAll(",", " ");
						tag = tag.replaceAll("'", "''");
						System.out.println("Creating tag for: " + tag);
						tgPSCounter++;
						db.createTag(tag);
						System.out.println("Creating used tag for: " + status.getUser().getScreenName() + " on " + tag);
						db.usedTag(status.getUser().getScreenName(), tag, status.getCreatedAt());
						System.out.println("Updating tag activity for: " + tag);
						db.addTagActivity(tag, status.getCreatedAt());
					}
				}
			}
		}
	}
	//Remove punctuation since it messes up SQL querys. 
	//Todo: disgard or add font recognition for non-english languages and non-ASCII symbols
	public void cleanResults(List<Object> res)
	{
		System.out.println("Cleaning results...");
		for (int i = 0; i < res.size(); i++)
		{
			if (res.get(i) != null && res.get(i).getClass() == String.class)
			{
				res.set(i, res.get(i).toString().replaceAll("'", "''"));
			}
		}
	}
}
