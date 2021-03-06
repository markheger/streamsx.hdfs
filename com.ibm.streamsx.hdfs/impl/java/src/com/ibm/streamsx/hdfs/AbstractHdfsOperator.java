/*******************************************************************************
 * Copyright (C) 2017-2019, International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/

package com.ibm.streamsx.hdfs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.ibm.json.java.JSONObject;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.*;

import com.ibm.streams.operator.AbstractOperator;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.OperatorContext.ContextCheck;
import com.ibm.streams.operator.compile.OperatorContextChecker;
import com.ibm.streams.operator.logging.LogLevel;
import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.state.StateHandler;
import com.ibm.streamsx.hdfs.client.HdfsJavaClient;
import com.ibm.streamsx.hdfs.client.IHdfsClient;

public abstract class AbstractHdfsOperator extends AbstractOperator implements StateHandler {

	private static final String CLASS_NAME = "com.ibm.streamsx.hdfs.AbstractHdfsOperator";

	/** Create a logger specific to this class */
	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME);

	private static Logger TRACE = Logger.getLogger(CLASS_NAME);

	// Common parameters and variables for connection
	private IHdfsClient fHdfsClient;
	public FileSystem fs = null;
	private String fHdfsUri = null;
	private String fHdfsUser = null;
	private String fHdfsPassword = null;
	private String fAuthPrincipal = null;
	private String fAuthKeytab = null;
	private String fCredFile = null;
	private String fConfigPath = null;

	private String fReconnectionPolicy = IHdfsConstants.RECONNPOLICY_BOUNDEDRETRY;
	// This optional parameter reconnectionBound specifies the number of successive connection
	// that will be attempted for this operator.
	// It can appear only when the reconnectionPolicy parameter is set to BoundedRetry
	// and cannot appear otherwise.  If not present the default value is 5
	private int fReconnectionBound = IHdfsConstants.RECONN_BOUND_DEFAULT;
	// This optional parameter reconnectionInterval specifies the time period in seconds which
	// the operator will be wait before trying to reconnect.
	// If not specified, the default value is 10.0.
	private double fReconnectionInterval = IHdfsConstants.RECONN_INTERVAL_DEFAULT;
	// This parameter specifies the json string that contains the user, password
	// and hdfsUrl.
	private String credentials = null;;
	// The name of the application configuration object
	private String appConfigName = null;
	// data from application config object
	Map<String, String> appConfig = null;

	// Other variables
	protected Thread processThread = null;
	protected boolean shutdownRequested = false;
	private String fKeyStorePath;
	private String fKeyStorePassword;
	private String fLibPath; // Used to allow the user to override the hadoop
							 // home environment variable
	private String fPolicyFilePath;

	@Override
	public synchronized void initialize(OperatorContext context) throws Exception {
		super.initialize(context);
		setJavaSystemProperty();
		loadAppConfig(context);
		if (credentials != null) {
			if (!this.getCredentials(credentials)){
				return;
			}
		}

		if (fCredFile != null) {
			if (!this.getCredentialsFormFile(fCredFile)){
				return;
			}
		}
	
		setupClassPaths(context);
		addConfigPathToClassPaths(context);
		createConnection();

	}

	/** 
	 * set policy file path and https.protocols in JAVA system properties 
	 */
	private void setJavaSystemProperty() {
		String policyFilePath = getAbsolutePath(getPolicyFilePath());
		if (policyFilePath != null) {
			TRACE.log(TraceLevel.INFO, "Policy file path: " + policyFilePath);
			System.setProperty("com.ibm.security.jurisdictionPolicyDir", policyFilePath);
		}
		System.setProperty("https.protocols", "TLSv1.2");
		String httpsProtocol = System.getProperty("https.protocols");
		TRACE.log(TraceLevel.INFO, "streamsx.hdfs https.protocols " + httpsProtocol);
	}

	/**
	 * read the application config into a map
	 * 
	 * @param context the operator context
	 */
	protected void loadAppConfig(OperatorContext context) {

		// if no appconfig name is specified, create empty map
		if (appConfigName == null) {
			appConfig = new HashMap<String, String>();
			return;
		}

		appConfig = context.getPE().getApplicationConfiguration(appConfigName);
		if (appConfig.isEmpty()) {
			LOGGER.log(LogLevel.WARN, "Application config not found or empty: " + appConfigName);
		}

		for (Map.Entry<String, String> kv : appConfig.entrySet()) {
			TRACE.log(TraceLevel.DEBUG, "Found application config entry: " + kv.getKey() + "=" + kv.getValue());
		}

		if (null != appConfig.get("credentials")) {
			credentials = appConfig.get("credentials");
		}
	}

	
	/**
	 * read the credentials from file and set fHdfsUser, fHdfsPassword and  fHdfsUrl.
	 * @param credFile
	 */
	public boolean getCredentialsFormFile(String credFile) throws IOException {
		
        String credentials = null;    
        try
        {
        	credentials = new String ( Files.readAllBytes( Paths.get(getAbsolutePath(credFile)) ) );
        }
        catch (IOException e)
        {
        	LOGGER.log(LogLevel.ERROR, "The credentials file " + getAbsolutePath(credFile) + "does not exist." );
        	return false;
        }

        if ((credentials != null ) &&  (!credentials.isEmpty())) {
        	return getCredentials(credentials);
        }
        return false;
       }
	
	
	/**
	 * read the credentials and set fHdfsUser, fHfsPassword and fHdfsUrl.
	 * 
	 * @param credentials
	 */
	public boolean getCredentials(String credentials) throws IOException {
		String jsonString = credentials;
		try {
			JSONObject obj = JSONObject.parse(jsonString);
			fHdfsUser = (String) obj.get("user");
			if (fHdfsUser == null || fHdfsUser.trim().isEmpty()) {
				fHdfsUser = (String) obj.get("hdfsUser");
				if (fHdfsUser == null || fHdfsUser.trim().isEmpty()) {
					LOGGER.log(LogLevel.ERROR, Messages.getString("'fHdfsUser' is required to create HDFS connection."));
					throw new Exception(Messages.getString("'fHdfsUser' is required to create HDFS connection."));
				}
			}

			fHdfsPassword = (String) obj.get("password");
			if (fHdfsPassword == null || fHdfsPassword.trim().isEmpty()) {
				fHdfsPassword = (String) obj.get("hdfsPassword");
				if (fHdfsPassword == null || fHdfsPassword.trim().isEmpty()) {
					LOGGER.log(LogLevel.ERROR, Messages.getString(
						"'fHdfsPassword' is required to create HDFS connection."));
					throw new Exception(Messages.getString("'fHdfsPassword' is required to create HDFS connection."));
				}
			}

			fHdfsUri = (String) obj.get("webhdfs");
			if (fHdfsUri == null || fHdfsUri.trim().isEmpty()) {
				fHdfsUri = (String) obj.get("hdfsUri");
				if (fHdfsUri == null || fHdfsUri.trim().isEmpty()) {				
					LOGGER.log(LogLevel.ERROR, Messages.getString("'fHdfsUri' is required to create HDFS connection."));
					throw new Exception(Messages.getString("'fHdfsUri' is required to create HDFS connection."));
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * set the class path for Hadoop libraries 
	 * 
	 * @param context
	 */
	private void setupClassPaths(OperatorContext context) {

		ArrayList<String> libList = new ArrayList<>();
		String HADOOP_HOME = System.getenv("HADOOP_HOME");
		if (getLibPath() != null) {
			String user_defined_path = getLibPath() + "/*";
			TRACE.log(TraceLevel.INFO, "Adding " + user_defined_path + " to classpath");
			libList.add(user_defined_path);
		} else {
			// add class path for delivered jar files from ./impl/lib/ext/ directory
			String default_dir = context.getToolkitDirectory() + "/impl/lib/ext/*";
			TRACE.log(TraceLevel.INFO, "Adding /impl/lib/ext/* to classpath");
			libList.add(default_dir);

			if (HADOOP_HOME != null) {
				// if no config path and no HdfsUri is defined it checks the
				// HADOOP_HOME/config directory for default core-site.xml file
				if ((fConfigPath == null) && (fHdfsUri == null)) {
					libList.add(HADOOP_HOME + "/conf");
					libList.add(HADOOP_HOME + "/../hadoop-conf");
					libList.add(HADOOP_HOME + "/etc/hadoop");
					libList.add(HADOOP_HOME + "/*");
					libList.add(HADOOP_HOME + "/../hadoop-hdfs");
					libList.add(HADOOP_HOME + "/lib/*");
					libList.add(HADOOP_HOME + "/client/*");
				}
				
				

			}
		}
		for (int i = 0; i < libList.size(); i++) {
			TRACE.log(TraceLevel.INFO, "calss path list " + i + " : " + libList.get(i));
		}

		try {
			context.addClassLibraries(libList.toArray(new String[0]));

		} catch (MalformedURLException e) {
			LOGGER.log(TraceLevel.ERROR, "LIB_LOAD_ERROR", e);
		}
	}
	
	private void addConfigPathToClassPaths(OperatorContext context) {

		ArrayList<String> libList = new ArrayList<>();
		if (getConfigPath() != null) {
			String user_defined_config_path = getAbsolutePath(getConfigPath())+ "/*";
			TRACE.log(TraceLevel.INFO, "Adding " + user_defined_config_path + " to classpath");
			libList.add(user_defined_config_path);
		}

		for (int i = 0; i < libList.size(); i++) {
			TRACE.log(TraceLevel.INFO, "calss path list " + i + " : " + libList.get(i));
		}

		try {
			context.addClassLibraries(libList.toArray(new String[0]));

		} catch (MalformedURLException e) {
			LOGGER.log(TraceLevel.ERROR, "LIB_LOAD_ERROR", e);
		}
	}
	
	
	/** createConnection creates a connection to the hadoop file system. */
	private synchronized void createConnection() throws Exception {
		// Delay in miliseconds as specified in fReconnectionInterval parameter
		final long delay = TimeUnit.MILLISECONDS.convert((long) fReconnectionInterval, TimeUnit.SECONDS);
		LOGGER.log(TraceLevel.INFO, "createConnection  ReconnectionPolicy " + fReconnectionPolicy + "  ReconnectionBound "
				+ fReconnectionBound + "  ReconnectionInterval " + fReconnectionInterval);
		if (fReconnectionPolicy == IHdfsConstants.RECONNPOLICY_NORETRY) {
			fReconnectionBound = 1;
		}

		if (fReconnectionPolicy == IHdfsConstants.RECONNPOLICY_INFINITERETRY) {
			fReconnectionBound = 9999;
		}

		for (int nConnectionAttempts = 0; nConnectionAttempts < fReconnectionBound; nConnectionAttempts++) {
			LOGGER.log(TraceLevel.INFO, "createConnection   nConnectionAttempts is: " + nConnectionAttempts + " delay "
					+ delay);
			try {
				fHdfsClient = createHdfsClient();
				fs = fHdfsClient.connect(getHdfsUri(), getHdfsUser(), getAbsolutePath(getConfigPath()));
				LOGGER.log(TraceLevel.INFO, Messages.getString("HDFS_CLIENT_AUTH_CONNECT", fHdfsUri));
				break;
			} catch (Exception e) {
				LOGGER.log(TraceLevel.ERROR, Messages.getString("HDFS_CLIENT_AUTH_CONNECT", e.toString()));
				Thread.sleep(delay);
			}
		}

	}

	
	/*
	 * The method checkParameters
	 */
	@ContextCheck(compile = true)
	public static void checkParameters(OperatorContextChecker checker) {
		// If credFile is set as parameter, hdfsUser, hdfsPassword and hdfsUrl can not be set
		checker.checkExcludedParameters("hdfsUser", "credFile");
		checker.checkExcludedParameters("hdfsPassword", "credFile");
		checker.checkExcludedParameters("hdfsUrl", "credFile");

		// If credentials is set as parameter, hdfsUser, hdfsPassword and hdfsUrl can not be set.
		checker.checkExcludedParameters("hdfsUser", "credentials");
		checker.checkExcludedParameters("hdfsPassword", "credentials");
		checker.checkExcludedParameters("hdfsUri", "credentials");

		// If credentials is set as parameter, credFile can not be set
		checker.checkExcludedParameters("credFile", "credentials");
		
		// check reconnection related parameters
		checker.checkDependentParameters("reconnectionBound", "reconnectionPolicy");
		checker.checkDependentParameters("reconnectionInterval", "reconnectionPolicy");

	}

	
	@Parameter(name = IHdfsConstants.PARAM_HDFS_URI, optional = true, description = IHdfsConstants.DESC_HDFS_URL)
	public void setHdfsUri(String hdfsUri) {
		TRACE.log(TraceLevel.DEBUG, "setHdfsUri: " + hdfsUri);
		fHdfsUri = hdfsUri;
	}

	public String getHdfsUri() {
		return fHdfsUri;
	}

	
	@Parameter(name = IHdfsConstants.PARAM_HDFS_USER, optional = true, description = IHdfsConstants.DESC_HDFS_USER)
	public void setHdfsUser(String hdfsUser) {
		this.fHdfsUser = hdfsUser;
	}

	public String getHdfsUser() {
		return fHdfsUser;
	}

	
	@Parameter(name = IHdfsConstants.PARAM_HDFS_PASSWORD, optional = true, description = IHdfsConstants.DESC_HDFS_PASSWORD)
	public void setHdfsPassword(String hdfsPassword) {
		fHdfsPassword = hdfsPassword;
	}

	public String getHdfsPassword() {
		return fHdfsPassword;
	}


	// Parameter reconnectionPolicy
	@Parameter(name = IHdfsConstants.PARAM_REC_POLICY, optional = true, description = IHdfsConstants.DESC_REC_POLICY)
	public void setReconnectionPolicy(String reconnectionPolicy) {
		this.fReconnectionPolicy = reconnectionPolicy;
	}

	public String getReconnectionPolicy() {
		return fReconnectionPolicy;
	}

	
	// Parameter reconnectionBound
	@Parameter(name = IHdfsConstants.PARAM_REC_BOUND, optional = true, description = IHdfsConstants.DESC_REC_BOUND)
	public void setReconnectionBound(int reconnectionBound) {
		this.fReconnectionBound = reconnectionBound;
	}

	public int getReconnectionBound() {
		return fReconnectionBound;
	}

	// Parameter reconnectionInterval
	@Parameter(name = IHdfsConstants.PARAM_REC_INTERVAL, optional = true, description = IHdfsConstants.DESC_REC_INTERVAL)
	public void setReconnectionInterval(double reconnectionInterval) {
		this.fReconnectionInterval = reconnectionInterval;
	}

	public double getReconnectionInterval() {
		return fReconnectionInterval;
	}

	// Parameter authPrincipal
	@Parameter(name = IHdfsConstants.PARAM_AUTH_PRINCIPAL, optional = true, description = IHdfsConstants.DESC_PRINCIPAL)
	public void setAuthPrincipal(String authPrincipal) {
		this.fAuthPrincipal = authPrincipal;
	}

	public String getAuthPrincipal() {
		return fAuthPrincipal;
	}
	
	// Parameter authKeytab
	@Parameter(name = IHdfsConstants.PARAM_AUTH_KEYTAB, optional = true, description = IHdfsConstants.DESC_AUTH_KEY)
	public void setAuthKeytab(String authKeytab) {
		this.fAuthKeytab = authKeytab;
	}

	public String getAuthKeytab() {
		return fAuthKeytab;
	}

	// Parameter CredFile
	@Parameter(name = IHdfsConstants.PARAM_CRED_FILE, optional = true, description = IHdfsConstants.DESC_CRED_FILE)
	public void setCredFile(String credFile) {
		this.fCredFile = credFile;
	}

	public String getCredFile() {
		return fCredFile;
	}

	// Parameter ConfigPath
	@Parameter(name = IHdfsConstants.PARAM_CONFIG_PATH, optional = true, description = IHdfsConstants.DESC_CONFIG_PATH)
	public void setConfigPath(String configPath) {
		this.fConfigPath = configPath;
	}

	public String getConfigPath() {
		return fConfigPath;
	}

	// Parameter keyStorePath
	@Parameter(name = IHdfsConstants.PARAM_KEY_STOR_PATH, optional = true, description = IHdfsConstants.DESC_KEY_STOR_PATH)
	public void setKeyStorePath(String keyStorePath) {
		fKeyStorePath = keyStorePath;
	}

	public String getKeyStorePath() {
		return fKeyStorePath;
	}

	// Parameter keyStorePassword
	@Parameter(name = IHdfsConstants.PARAM_KEY_STOR_PASSWORD, optional = true, description = IHdfsConstants.DESC_KEY_STOR_PASSWORD)
	public void setKeyStorePassword(String keyStorePassword) {
		fKeyStorePassword = keyStorePassword;
	}

	public String getKeyStorePassword() {
		return fKeyStorePassword;
	}

	// Parameter libPath
	@Parameter(name = IHdfsConstants.PARAM_LIB_PATH, optional = true, description = IHdfsConstants.DESC_LIB_PATH)
	public void setLibPath(String libPath) {
		fLibPath = libPath;
	}

	public String getLibPath() {
		return fLibPath;
	}

	// Parameter policyFilePath
	@Parameter(name = IHdfsConstants.PARAM_POLICY_FILE_PATH ,optional = true, description = IHdfsConstants.DESC_POLICY_FILE_PATH)
	public void setPolicyFilePath(String policyFilePath) {
		fPolicyFilePath = policyFilePath;
	}

	public String getPolicyFilePath() {
		return fPolicyFilePath;
	}

	// Parameter credentials
	@Parameter(name = IHdfsConstants.PARAM_CREDENTIALS, optional = true, description = IHdfsConstants.DESC_CREDENTIALS)
	public void setcredentials(String credentials) {
		this.credentials = credentials;
	}

	public String getCredentials() {
		return this.credentials;
	}

	
	// Parameter appConfigName
	@Parameter(name = IHdfsConstants.PARAM_APP_CONFIG_NAME, optional = true, description = IHdfsConstants.DESC_APP_CONFIG_NAME)
	public void setAppConfigName(String appConfigName) {
		this.appConfigName = appConfigName;
	}

	public String getAppConfigName() {
		return this.appConfigName;
	}

	@Override
	public void allPortsReady() throws Exception {
		super.allPortsReady();
		if (processThread != null) {
			startProcessing();
		}
	}

	protected synchronized void startProcessing() {
		processThread.start();
	}

	/** By default, this does nothing. */
	protected void process() throws Exception {

	}

	public void shutdown() throws Exception {

		shutdownRequested = true;
		if (fHdfsClient != null) {
			fHdfsClient.disconnect();
		}

		super.shutdown();
	}

	protected Thread createProcessThread() {
		Thread toReturn = getOperatorContext().getThreadFactory().newThread(new Runnable() {

			@Override
			public void run() {
				try {
					process();
				} catch (Exception e) {
					LOGGER.log(TraceLevel.ERROR, e.getMessage());
					// if we get to the point where we got an exception
					// here we should rethrow the exception to cause the
					// operator to shut down.
					throw new RuntimeException(e);
				}
			}
		});
		toReturn.setDaemon(false);
		return toReturn;
	}

	protected IHdfsClient createHdfsClient() throws Exception {
		IHdfsClient client = new HdfsJavaClient();

		client.setConnectionProperty(IHdfsConstants.PARAM_KEY_STOR_PATH, getAbsolutePath(getKeyStorePath()));
		client.setConnectionProperty(IHdfsConstants.PARAM_KEY_STOR_PASSWORD, getKeyStorePassword());

		client.setConnectionProperty(IHdfsConstants.PARAM_HDFS_PASSWORD, getHdfsPassword());
		client.setConnectionProperty(IHdfsConstants.PARAM_AUTH_PRINCIPAL, getAuthPrincipal());
		client.setConnectionProperty(IHdfsConstants.PARAM_AUTH_KEYTAB, getAbsolutePath(getAuthKeytab()));

		return client;
	}

	protected String getAbsolutePath(String filePath) {
		if (filePath == null)
			return null;

		Path p = new Path(filePath);
		if (p.isAbsolute()) {
			return filePath;
		} else {
			File f = new File(getOperatorContext().getPE().getApplicationDirectory(), filePath);
			return f.getAbsolutePath();
		}
	}

	protected IHdfsClient getHdfsClient() {
		return fHdfsClient;
	}

}
