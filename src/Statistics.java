import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.Timer;


public class Statistics implements ActionListener {

	private JLabel pPSLabel;
	private JLabel tPSLabel;
	private JLabel tgPSLabel;
	private JLabel totalProfiles;
	private JLabel totalTweets;
	private JLabel totalTags;

	private TwitterMiner miner;
	private int counter = 0;

	public Statistics(TwitterMiner miner)
	{
		this.miner = miner;

		JFrame frmTwitterMiner = new JFrame("Twitter Miner Stats");
		frmTwitterMiner.setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Dillon\\Desktop\\1402178574_282459.ico"));
		frmTwitterMiner.setTitle("Twitter Miner");
		frmTwitterMiner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTwitterMiner.setVisible(true);
		frmTwitterMiner.setLocationRelativeTo(null);
		frmTwitterMiner.setSize(426, 175);
		SpringLayout springLayout = new SpringLayout();
		frmTwitterMiner.getContentPane().setLayout(springLayout);

		pPSLabel = new JLabel("Connecting");
		springLayout.putConstraint(SpringLayout.NORTH, pPSLabel, 93, SpringLayout.NORTH, frmTwitterMiner.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, pPSLabel, 43, SpringLayout.WEST, frmTwitterMiner.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, pPSLabel, -10, SpringLayout.SOUTH, frmTwitterMiner.getContentPane());
		frmTwitterMiner.getContentPane().add(pPSLabel);

		tPSLabel = new JLabel("Connecting");
		springLayout.putConstraint(SpringLayout.WEST, tPSLabel, 175, SpringLayout.WEST, frmTwitterMiner.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, pPSLabel, -45, SpringLayout.WEST, tPSLabel);
		springLayout.putConstraint(SpringLayout.NORTH, tPSLabel, 0, SpringLayout.NORTH, pPSLabel);
		springLayout.putConstraint(SpringLayout.SOUTH, tPSLabel, -10, SpringLayout.SOUTH, frmTwitterMiner.getContentPane());
		frmTwitterMiner.getContentPane().add(tPSLabel);

		JLabel lblProfilesPerSecond = new JLabel("Profiles per second");
		springLayout.putConstraint(SpringLayout.SOUTH, lblProfilesPerSecond, -6, SpringLayout.NORTH, pPSLabel);
		springLayout.putConstraint(SpringLayout.EAST, lblProfilesPerSecond, -277, SpringLayout.EAST, frmTwitterMiner.getContentPane());
		frmTwitterMiner.getContentPane().add(lblProfilesPerSecond);

		JLabel lblTweetsPerSecond = new JLabel("Tweets per second");
		springLayout.putConstraint(SpringLayout.SOUTH, lblTweetsPerSecond, -6, SpringLayout.NORTH, pPSLabel);
		springLayout.putConstraint(SpringLayout.EAST, lblTweetsPerSecond, -143, SpringLayout.EAST, frmTwitterMiner.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, lblTweetsPerSecond, 25, SpringLayout.EAST, lblProfilesPerSecond);
		frmTwitterMiner.getContentPane().add(lblTweetsPerSecond);

		JLabel lblTagsPerSecond = new JLabel("Tags per second");
		springLayout.putConstraint(SpringLayout.SOUTH, lblTagsPerSecond, -6, SpringLayout.NORTH, pPSLabel);
		springLayout.putConstraint(SpringLayout.EAST, lblTagsPerSecond, -21, SpringLayout.EAST, frmTwitterMiner.getContentPane());
		frmTwitterMiner.getContentPane().add(lblTagsPerSecond);

		tgPSLabel = new JLabel("Connecting");
		springLayout.putConstraint(SpringLayout.EAST, tPSLabel, -48, SpringLayout.WEST, tgPSLabel);
		springLayout.putConstraint(SpringLayout.NORTH, tgPSLabel, 5, SpringLayout.NORTH, pPSLabel);
		springLayout.putConstraint(SpringLayout.EAST, tgPSLabel, -33, SpringLayout.EAST, frmTwitterMiner.getContentPane());
		frmTwitterMiner.getContentPane().add(tgPSLabel);

		JLabel lblProfiles = new JLabel("Profiles:");
		springLayout.putConstraint(SpringLayout.NORTH, lblProfiles, 10, SpringLayout.NORTH, frmTwitterMiner.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, lblProfiles, 0, SpringLayout.WEST, lblProfilesPerSecond);
		frmTwitterMiner.getContentPane().add(lblProfiles);

		totalProfiles = new JLabel("Connecting");
		springLayout.putConstraint(SpringLayout.NORTH, totalProfiles, 0, SpringLayout.NORTH, lblProfiles);
		springLayout.putConstraint(SpringLayout.WEST, totalProfiles, 6, SpringLayout.EAST, lblProfiles);
		frmTwitterMiner.getContentPane().add(totalProfiles);

		JLabel lblTweets = new JLabel("Tweets:");
		springLayout.putConstraint(SpringLayout.WEST, lblTweets, 0, SpringLayout.WEST, lblTweetsPerSecond);
		springLayout.putConstraint(SpringLayout.SOUTH, lblTweets, 0, SpringLayout.SOUTH, lblProfiles);
		frmTwitterMiner.getContentPane().add(lblTweets);

		totalTweets = new JLabel("Connecting");
		springLayout.putConstraint(SpringLayout.NORTH, totalTweets, 0, SpringLayout.NORTH, lblProfiles);
		springLayout.putConstraint(SpringLayout.WEST, totalTweets, 6, SpringLayout.EAST, lblTweets);
		frmTwitterMiner.getContentPane().add(totalTweets);

		JLabel lblTags = new JLabel("Tags:");
		springLayout.putConstraint(SpringLayout.NORTH, lblTags, 0, SpringLayout.NORTH, lblProfiles);
		springLayout.putConstraint(SpringLayout.WEST, lblTags, 0, SpringLayout.WEST, lblTagsPerSecond);
		frmTwitterMiner.getContentPane().add(lblTags);

		totalTags = new JLabel("Connecting");
		springLayout.putConstraint(SpringLayout.WEST, totalTags, 6, SpringLayout.EAST, lblTags);
		springLayout.putConstraint(SpringLayout.SOUTH, totalTags, 0, SpringLayout.SOUTH, lblProfiles);
		frmTwitterMiner.getContentPane().add(totalTags);

		Timer timer = new Timer(1000, this);
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		counter++;

		pPSLabel.setText(String.valueOf(miner.pPSCounter));
		tPSLabel.setText(String.valueOf(miner.tPSCounter));
		tgPSLabel.setText(String.valueOf(miner.tgPSCounter));
		if (counter == 5)
		{
			totalProfiles.setText(String.valueOf(miner.db.getTotalProfiles()));
			totalTweets.setText(String.valueOf(miner.db.getTotalTweets()));
			totalTags.setText(String.valueOf(miner.db.getTotalTags()));
			counter = 0;
		}
		miner.pPSCounter = 0;
		miner.tPSCounter = 0;
		miner.tgPSCounter = 0;
	}
}
