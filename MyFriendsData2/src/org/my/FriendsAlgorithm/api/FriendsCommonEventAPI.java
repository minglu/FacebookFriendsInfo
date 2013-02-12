package org.my.FriendsAlgorithm.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.my.FriendsAlgorithm.utilities.DownloadHandler;
import org.my.FriendsAlgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.FriendsAlgorithm.utilities.DownloadHandler.NetworkConnectionException;
import org.osgi.service.log.LogService;

public class FriendsCommonEventAPI implements FaceBookAPI {
    private LogService logger;
	
	public FriendsCommonEventAPI(LogService logger)
	{
		this.logger = logger;	
	}
	@Override
	public String callAPI(String token, String id) {
		try{
			   //logger.log(LogService.LOG_INFO,"Inside call");
				URL url = new URL("https://graph.facebook.com/fql?q=SELECT+eid,+uid+FROM+event_member+WHERE+uid+IN(SELECT+uid2+FROM+friend+WHERE+uid1=me())+OR+uid=me()&"+token);
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
