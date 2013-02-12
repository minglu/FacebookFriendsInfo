package org.my.FriendsAlgorithm.facade;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.my.FriendsAlgorithm.model.FriendData;
import org.my.FriendsAlgorithm.utilities.CSVWriter;
import org.my.FriendsAlgorithm.utilities.DownloadHandler;
import org.my.FriendsAlgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.FriendsAlgorithm.utilities.DownloadHandler.NetworkConnectionException;
import org.my.FriendsAlgorithm.api.FaceBookAPI;
import org.my.FriendsAlgorithm.api.MyDetailsAPI;
import org.osgi.service.log.LogService;

public class Facade {
	private CSVWriter csv;
	private LogService logger;

	public Facade(LogService logger) {
		this.logger = logger;
	}

	public String getAccessToken() {
		try {
			URI url = new URI(
					"https://www.facebook.com/dialog/oauth?client_id=283202715139589"
							+ "&redirect_uri=https://morning-fjord-1741.herokuapp.com/token.php&scope=manage_friendlists"
							+ "&response_type=token"
							+ "&scope=email,user_about_me,user_activities,user_birthday,user_education_history,"
							+ "user_events,user_hometown,user_interests,user_likes,user_groups,user_location,user_religion_politics,friends_events,read_friendlists");
			Desktop.getDesktop().browse(url);
		} catch (URISyntaxException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (IOException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		}

		String input = JOptionPane.showInputDialog("Enter Access Token:");
		return input;
	}

	// check login
	public String checkLogin() {
		try {
			URL url = new URL(
					"https://morning-fjord-1741.herokuapp.com/CheckLogin.php");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			return DownloadHandler.getResponse(connection);
		} catch (IOException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (InvalidUrlException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (NetworkConnectionException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		}

		return "0";
	}

	// writes the CSV file
	public void writeCSVFile(HashMap<String, FriendData> map)
			throws IOException {

		final JFileChooser fc = new JFileChooser();
		int userSelection = fc.showSaveDialog(null);
		File fileToSave = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			fileToSave = fc.getSelectedFile();
			System.out.println("Save as file: " + fileToSave.getAbsolutePath());
		}

		CSVWriter writer = new CSVWriter(fileToSave.getAbsolutePath());
		String[] entries = { "Person Name", "Facebook Id", "Status", "Gender",
				"Hometown", "Birthday", "Interests", "Religion",
				"Political View", "relationship_status", "Events" };
		writer.writeNext(entries);
		//logger.log(LogService.LOG_INFO,"written first line");
		for (Map.Entry<String, FriendData> entry : map.entrySet()) {

			FriendData data = entry.getValue();
			////logger.log(LogService.LOG_INFO,"In Data");
			// convert event array into string
			List<String> eventArray =  data.getEventList();
			String eventStr = "";
			if (!eventArray.isEmpty()) {				
				for (String event : eventArray) {
					eventStr = event + ";";
				}
				eventStr = eventStr.substring(0, eventStr.length() - 1);
			}
			//logger.log(LogService.LOG_INFO,"Event String="+eventStr);
			String[] nameList = { data.getName(), data.getId(),
					data.getStatus(), data.getGender(),
					data.getHomeTownLocation(), data.getBirthday(),
					data.getInterest(), data.getReligion(),
					data.getPoliticalView(), data.getRelationShipStatus(),
					eventStr };
			
			//logger.log(LogService.LOG_INFO,data.getName()+ data.getId());
			writer.writeNext(nameList);
			//logger.log(LogService.LOG_INFO,"written line");
		}
		writer.close();
	}

	public String getMyName(String token) throws JSONException {
		FaceBookAPI mydetails = new MyDetailsAPI(logger);
		String data = mydetails.callAPI(token, "");
		JSONObject obj = new JSONObject(new JSONTokener(data));
		return obj.getString("name");
	}

	public String getMyId(String token) throws JSONException {
		FaceBookAPI mydetails = new MyDetailsAPI(logger);
		String data = mydetails.callAPI(token, "");
		JSONObject obj = new JSONObject(new JSONTokener(data));
		return obj.getString("id");
	}

}
