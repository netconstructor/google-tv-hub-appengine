/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.jonathanhester.gtvhub.server;

import com.google.android.c2dm.server.PMF;
import com.google.appengine.api.datastore.Key;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Registration info.
 * 
 * An account may be associated with multiple phones, and a phone may be
 * associated with multiple accounts.
 * 
 * registrations lists different phones registered to that account.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DeviceInfo {
	private static final Logger log = Logger.getLogger(DeviceInfo.class
			.getName());

	/**
	 * User-email # device-id
	 * 
	 * Device-id can be specified by device, default is hash of abs(registration
	 * id).
	 * 
	 * user@example.com#1234
	 */
	@PrimaryKey
	@Persistent
	private Key key;

	/**
	 * The ID used for sending messages to.
	 */
	@Persistent
	private String deviceRegistrationID;

	/**
	 * Current supported types: (default) - ac2dm, regular froyo+ devices using
	 * C2DM protocol
	 * 
	 * New types may be defined - for example for sending to chrome.
	 */
	@Persistent
	private String type;

	/**
	 * For statistics - and to provide hints to the user.
	 */
	@Persistent
	private Date registrationTimestamp;

	@Persistent
	private Boolean debug;

	@Persistent
	private String code;

	public DeviceInfo(Key key, String deviceRegistrationID) {
		log.info("new DeviceInfo: key=" + key + ", deviceRegistrationId="
				+ deviceRegistrationID);
		this.key = key;
		this.deviceRegistrationID = deviceRegistrationID;
		this.setRegistrationTimestamp(new Date()); // now
	}

	public void setUniqueCode() {
		final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		int len = 7;
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		this.code = sb.toString();
	}

	public String getCode() {
		return this.code;
	}

	public DeviceInfo(Key key) {
		log.info("new DeviceInfo: key=" + key);
		this.key = key;
	}

	public Key getKey() {
		log.info("DeviceInfo: return key=" + key);
		return key;
	}

	public void setKey(Key key) {
		log.info("DeviceInfo: set key=" + key);
		this.key = key;
	}

	// Accessor methods for properties added later (hence can be null)

	public String getDeviceRegistrationID() {
		log.info("DeviceInfo: return deviceRegistrationID="
				+ deviceRegistrationID);
		return deviceRegistrationID;
	}

	public void setDeviceRegistrationID(String deviceRegistrationID) {
		log.info("DeviceInfo: set deviceRegistrationID=" + deviceRegistrationID);
		this.deviceRegistrationID = deviceRegistrationID;
	}

	public boolean getDebug() {
		return (debug != null ? debug.booleanValue() : false);
	}

	public void setDebug(boolean debug) {
		this.debug = new Boolean(debug);
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type != null ? type : "";
	}

	public void setRegistrationTimestamp(Date registrationTimestamp) {
		this.registrationTimestamp = registrationTimestamp;
	}

	public Date getRegistrationTimestamp() {
		return registrationTimestamp;
	}

	/**
	 * Helper function - will query all registrations for a user.
	 */
	@SuppressWarnings("unchecked")
	public static List<DeviceInfo> getDeviceInfoForUser(String code) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery("select from "
					+ DeviceInfo.class.getName() + " where code=='" + code
					+ "'");
			List<DeviceInfo> list = (List<DeviceInfo>) query.execute();
			ArrayList<DeviceInfo> myList = new ArrayList<DeviceInfo>();
			for (DeviceInfo di : list) {
				myList.add(di);
			}
			query.closeAll();
			log.info("Return " + myList.size() + " devices for user " + code);
			return myList;
		} finally {
			pm.close();
		}
	}

	@Override
	public String toString() {
		return "DeviceInfo[key=" + key + ", deviceRegistrationID="
				+ deviceRegistrationID + ", type=" + type
				+ ", registrationTimestamp=" + registrationTimestamp
				+ ", debug=" + debug + "]";
	}
}
