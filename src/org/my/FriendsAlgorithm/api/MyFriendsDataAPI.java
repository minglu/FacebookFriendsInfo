package org.my.FriendsAlgorithm.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.my.FriendsAlgorithm.utilities.DownloadHandler;
import org.my.FriendsAlgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.FriendsAlgorithm.utilities.DownloadHandler.NetworkConnectionException;
import org.osgi.service.log.LogService;

public class MyFriendsDataAPI implements FaceBookAPI {
    private LogService logger;
	public MyFriendsDataAPI(LogService logger)
	{
		this.logger =logger;
	}
	@Override
	public String callAPI(String token, String id) {
		try{
			URL url = new URL("https://graph.facebook.com/fql?q=select+uid,name,status,hometown_location,religion,political,birthday,relationship_status,sex,interests+from+user+where+uid+in+(select+uid2+from+friend+where+uid1=me())&"+token);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();		
			connection.setRequestMethod("GET");	
			return DownloadHandler.getResponse(connection);
		} catch (IOException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (InvalidUrlException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} catch (NetworkConnectionException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		} 
		return "No data";	
	}

}
