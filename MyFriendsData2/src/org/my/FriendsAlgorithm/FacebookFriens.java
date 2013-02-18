package org.my.FriendsAlgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.framework.data.Data;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.my.FriendsAlgorithm.api.FaceBookAPI;
import org.my.FriendsAlgorithm.api.MyFriendsDataAPI;
import org.my.FriendsAlgorithm.facade.Facade;
import org.my.FriendsAlgorithm.model.FriendData;
import org.my.FriendsAlgorithm.api.FriendsEventAPI;
import org.my.FriendsAlgorithm.api.FriendsCommonEventAPI;
import org.osgi.service.log.LogService;

public class FacebookFriens implements Algorithm {
	private Data[] data;
	private Dictionary parameters;
	private CIShellContext ciShellContext;
	private LogService logger;
	private HashMap<String, FriendData> idDataList;
	private Facade facade;

	public FacebookFriens(Data[] data, Dictionary parameters,
			CIShellContext ciShellContext) {
		this.data = data;
		this.parameters = parameters;
		this.ciShellContext = ciShellContext;
		this.logger = (LogService) ciShellContext.getService(LogService.class
				.getName());
		facade = new Facade(this.logger);
		idDataList = new HashMap<String, FriendData>();

	}

	public Data[] execute() throws AlgorithmExecutionException {

		this.logger.log(LogService.LOG_INFO, "Call to Facebook API");
		this.logger
				.log(LogService.LOG_WARNING,
						"The use of the Facebook API is governed by following policies:");
		this.logger
				.log(LogService.LOG_WARNING,
						"This is a Facebook application that helps "
								+ "user export data out of Facebook for reuse in Visualization or any possible method of "
								+ "digital story telling. Data is exported in csv format. ");
		this.logger
				.log(LogService.LOG_WARNING,
						"According to Facebook's Statement of Rights and Responsibility. "
								+ "You own all of the content and information you post on Facebook, and you can control how it is shared through your privacy and application settings.");
		this.logger
				.log(LogService.LOG_INFO, "Please refer the following link:");
		this.logger.log(LogService.LOG_WARNING,
				"https://developers.facebook.com/policy");

		getFriendsData();
		return null;
	}

	void getFriendsData() {
		String token = facade.getAccessToken();
		this.logger.log(LogService.LOG_INFO, "Access Token: " + token);
		if (token == null)
			return;
		String data = "access_token=" + token;
		String myName = "";
		String myId = "";
		try {
			myName = facade.getMyName(data);
			myId = facade.getMyId(data);
		} catch (JSONException e1) {
			logger.log(LogService.LOG_INFO, e1.getMessage());
		}

		JSONObject obj;
		try {

			FaceBookAPI fb = new MyFriendsDataAPI(logger);
			String val = fb.callAPI(data, "");
			//logger.log(LogService.LOG_INFO, "value=" + val);
			JSONObject friendsObj = new JSONObject(val);

			JSONArray friendsArray = friendsObj.getJSONArray("data");
			int len = friendsArray.length();
			for (int i = 0; i < len; i++) {
               // retrive values from each of the persons
				JSONObject currentResult = friendsArray.getJSONObject(i);
				Long id = currentResult.getLong("uid");
				FriendData fd = new FriendData(id.toString());
				fd.setName(currentResult.getString("name"));
				fd.setId(id.toString());
				fd.setGender(currentResult.getString("sex"));
				if (!currentResult.isNull("hometown_location")) {
					JSONObject htObj =currentResult
					.getJSONObject("hometown_location");
					String name = htObj.getString("name");
					String state="";
					if(!htObj.isNull("state"))
					{
					  state = htObj.getString("state");
					}
					String country = htObj.getString("country");
					String zip = htObj.getString("zip");
					String htid = ((Long)htObj.getLong("id")).toString();
					String homeTown = name +";"+state +";"+ country+";"+zip+";" +"id =" + htid;
					homeTown = homeTown.replace(',',';');
					fd.setHomeTownLocation(homeTown);
					//logger.log(LogService.LOG_INFO,"Hometown="+homeTown );
				}
				if (!currentResult.isNull("status")) {
					JSONObject jsonObj = currentResult.getJSONObject("status");
					String message =jsonObj.getString("message");
					fd.setStatus(message);
				}
				if (!currentResult.isNull("interests")) {
					fd.setInterest(currentResult.getString("interests").replace(",", "|"));
				}
				if (!currentResult.isNull("birthday")) {
					 String birthday =currentResult.getString("birthday").replace(',', '.');
					fd.setBirthday(birthday);
				}
				if (!currentResult.isNull("religion")) {
					fd.setReligion(currentResult.getString("religion"));
				}
				if (!currentResult.isNull("political")) {
					fd.setPoliticalView(currentResult.getString("political"));
				}
				if (!currentResult.isNull("relationship_status")) {
					fd.setRelationShipStatus(currentResult.getString("relationship_status"));
				}
				idDataList.put(id.toString(), fd);
			}
			fb = new FriendsEventAPI(logger);
			obj = new JSONObject(fb.callAPI(data, ""));
			JSONArray jsonArrayEvent = obj.getJSONArray("data");
			HashMap<Long, String> eventIdToName = new HashMap<Long, String>();
			for (int i = 0; i < jsonArrayEvent.length(); i++) {
				JSONObject currentResult = jsonArrayEvent.getJSONObject(i);
				String eventName = currentResult.getString("name");
				Long id = currentResult.getLong("eid");
				eventIdToName.put(id, eventName);
			}
			// call FriendsCommonEventAPI
			fb = new FriendsCommonEventAPI(logger);
			obj = new JSONObject(fb.callAPI(data, ""));
			JSONArray jsonArrayCommonEvent = obj.getJSONArray("data");

			for (int i = 0; i < jsonArrayCommonEvent.length(); i++) {
				JSONObject currentResult = jsonArrayCommonEvent
						.getJSONObject(i);
				Long uid = currentResult.getLong("uid");
				Long eid = currentResult.getLong("eid");

				FriendData fd = idDataList.get(uid.toString());
				if(fd == null)continue;
				//logger.log(LogService.LOG_INFO,"Eventid ="+eventIdToName.get(eid));
				fd.addEvent(eventIdToName.get(eid).replace("|", ","));
			}             			
			facade.writeCSVFile(idDataList);
		} catch (JSONException e) {
			logger.log(LogService.LOG_INFO, e.getMessage());
		} catch (IOException e) {
			logger.log(LogService.LOG_INFO, e.getMessage());
		}
	}
}
