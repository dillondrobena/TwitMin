import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Database {

	private Statement statement;
	private Connection dbCon;

	public Database(String dbURL, String dbUserName, String dbPassword) throws SQLException
	{
		//Connect to the database
		dbCon = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
		statement = dbCon.createStatement();
		Statement statement = dbCon.createStatement(); //Create statement to add queries
		System.out.println(dbCon != null ? "Connection established." : "Connection could not be made.");	
	}
	//Best way I can find to format standard date to SQL compatible date, not sure if this works
	//for different Java verions
	@SuppressWarnings("deprecation")
	public String cleanDate(java.util.Date uncleanDate)
	{
		String str[] = uncleanDate.toGMTString().split(" ");
		String date = str[0] + "-" + str[1] + "-" + str[2];
		return date;
	}
	//Clean all single quotations and spaces to SQL appropriate format
	public String cleanQuery(String uncleanQuery)
	{
		String query = uncleanQuery.replace('\'', ' ');
		return query;
	}
	//Create user profile
	public int createProfile(List<Object> profile)
	{
		String query = "";
		try { //See if profile already exists
			ResultSet rs = statement.executeQuery("select * from profile where screen_name = '" + profile.get(0) + "'");
			if (rs.next() == false)
			{
				profile.set(1, cleanDate((java.util.Date) profile.get(1)));
				profile.add(10, cleanDate(new java.util.Date()));
				query = "insert into profile values (";
				for (int i = 0; i < profile.size(); i++)
				{
					if (profile.get(i) != null && profile.get(i).getClass() != Integer.class) query += "'" + profile.get(i) + "'";
					else query += profile.get(i);
					if (i != profile.size() - 1) query += ",";
				}
				query += ")";
				statement.execute(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//	System.out.println(query);
			return -1;
		}
		return 0;
	}
	//Create new tag
	public int createTag(String name)
	{
		String query = "insert into tags values ('" + name + "')";
		try { //Test if tag already exists
			ResultSet rs = statement.executeQuery("select * from tags where name = '" + name + "'");
			if (rs.next() == false)
			{
				statement.execute(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	//Create new tweet
	public int createTweet(List<Object> tweet)
	{
		try { //Test if tweet already exists
			ResultSet rs = statement.executeQuery("select * from tweets where id = " + tweet.get(0));
			if (rs.next() == false)
			{
				tweet.set(2, cleanDate((java.util.Date) tweet.get(2)));
				String query = "insert into tweets values (";
				for (int i = 0; i < tweet.size(); i++)
				{
					if (tweet.get(i) != null && tweet.get(i).getClass() != Integer.class && tweet.get(i).getClass() != Long.class) query += "'" + tweet.get(i) + "'";
					else query += tweet.get(i);
					if (i != tweet.size() - 1) query += ",";
				}
				query += ")";
				statement.execute(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	//Keep track of who uses what tag
	public int usedTag(String name, String tag, java.util.Date date)
	{
		int newCount = 1;
		try {
			ResultSet rs = statement.executeQuery("select count from used_tag where name = '" + name + "'" + " AND tag = '" + tag + "' AND used_on = '" + cleanDate(date) + "'");
			if (rs.next() == false)
			{
				System.out.println("Creating new tag activity...");
				String updatedOn = cleanDate(new java.util.Date());
				String query = "insert into used_tag values ('" + name + "','" + tag + "',1,'" + updatedOn + "','" + cleanDate(date) + "')";
				statement.execute(query);
			}
			else
			{
				System.out.println("Updating tag...");
				int count = rs.getInt(1);
				newCount = ++count;
				System.out.println(count);
				String updatedOn = cleanDate(new java.util.Date());
				String query = "update used_tag set count = " + newCount + ", as_of = '" + updatedOn + "' where name = '" + name + "' AND tag = '" + tag + "' AND used_on = '" + cleanDate(date) + "'";
				System.out.println(query);
				statement.execute(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	//Update followers a user has
	public int updateFollowingCount(String user, int followers)
	{
		String query = "insert into following_count values ('" + user + "','" + cleanDate(new java.util.Date()) + "'," + followers + ")";
		try {
			ResultSet rs = statement.executeQuery("select * from following_count where name = '" + user + "' AND as_of = '" + cleanDate(new java.util.Date()) + "'");
			if (rs.next() == false)
			{
				statement.execute(query);
			}
			else
			{
				statement.execute("update following_count set count = " + followers + " where name = '" + user + "' AND as_of = '" + cleanDate(new java.util.Date()) + "'");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	//Create a new following between users
	public int addFollowing(String user, String target)
	{
		String query = "insert into following values ('" + user + "','" + target + "')";
		try { //Test if user is already following
			ResultSet rs = statement.executeQuery("select * from following where name = '" + user + "'" + " AND is_following = '" + target + "'");
			if (rs.next() == false)
			{
				statement.execute(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	//Keep track of tag popularity
	public int addTagActivity(String name, java.util.Date date)
	{
		try { //See if already exists
			ResultSet rs = statement.executeQuery("select * from tag_activity where name = '" + name + "'" +  " AND tag_date = '" + cleanDate(date) + "'");
			if (rs.next() == false)
			{
				String query = "insert into tag_activity values ('" + name + "','" + cleanDate(date) + "',1)";
				statement.execute(query);
			}
			else
			{
				int count = rs.getInt(3);
				String tempName = rs.getString(1);
				java.util.Date tempDate = rs.getDate(2);
				count++;
				System.out.println(tempName);
				System.out.println(tempDate);
				String query = "update tag_activity set count = " + count + " where name = '" + tempName + "' AND tag_date = '" + cleanDate(tempDate) + "'";
				statement.execute(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	public int getTotalProfiles()
	{
		String query = "select count(*) from profile";
		try {
			ResultSet rs = statement.executeQuery(query);
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	public int getTotalTweets()
	{
		String query = "select count(*) from tweets";
		try {
			ResultSet rs = statement.executeQuery(query);
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	public int getTotalTags()
	{
		String query = "select count(*) from tags";
		try {
			ResultSet rs = statement.executeQuery(query);
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

}