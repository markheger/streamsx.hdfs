package com.ibm.streamsx.hdfs;
import com.ibm.streams.operator.metrics.Metric.Kind;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;

@com.ibm.streams.operator.model.PrimitiveOperator(name="HDFS2DirectoryScan", namespace="com.ibm.streamsx.hdfs", description="The **HDFS2DirectoryScan** operator scans a Hadoop Distributed File System directory for new or modified files. \n \nThe `HDFS2DirectoryScan` is similar to the `DirectoryScan` operator. \nThe `HDFS2DirectoryScan` operator repeatedly scans an HDFS directory and writes the names of new or modified files \nthat are found in the directory to the output port. The operator sleeps between scans. \n\n# Behavior in a consistent region \n\nThe `HDFS2DirectoryScan` operator can participate in a consistent region. \nThe operator can be at the start of a consistent region if there is no input port. \nThe operator supports periodic and operator-driven consistent region policies. \n\nIf consistent region policy is set as operator driven, the operator initiates a drain after each tuple is submitted. \nThis allows for a consistent state to be established after a file is fully processed. \nIf consistent region policy is set as periodic, the operator respects the period setting and establishes consistent states accordingly. \nThis means that multiple files can be processed before a consistent state is established. \n\nAt checkpoint, the operator saves the last submitted filename and its modification timestamp to the checkpoint. \nUpon application failures, the operator resubmits all files that are newer than the last submitted file at checkpoint. \n\n# Exceptions \n\nThe operator terminates in the following cases: \n* The operator cannot connect to HDFS. \n* The **strictMode** parameter is true but the directory is not found. \n* The path that is given by the directory name exists, but is an ordinary file and not a directory. \n* HDFS failed to give a list of files in the directory. \n* The pattern that is specified in the pattern parameter fails to compile. \n\n+ Examples \n\nThis example uses the `HDFS2DirectoryScan` operator to scan the HDFS directory On IBM Cloud.  The **hdfsUser** and **hdfsPassword** parameters are used to provide the username and password for authentication. \n \n    (stream<rstring filename> Files) as HDFS2DirectoryScan_1 = HDFS2DirectoryScan() \n    { \n        param \n            directory     : \"/user/clsadmin/works\"; \n            hdfsUri       : \"webhdfs://hdfsServer:8443\"; \n            hdfsPassword  : \"password\"; \n            hdfsUser      : \"clsadmin\"; \n            sleepTime     : 2.0; \n    } \n\nThis example uses the `HDFS2DirectoryScan` operator to scan the HDFS directory On IBM Cloud.  The **hdfsUser** and **hdfsPassword** parameters are now difined in credentials JSON string. \n    param \n        expression<rstring> $credentials : getSubmissionTimeValue(\"credentials\", \"{\n            \\\"user\\\"     : \\\"clsadmin\\\",\n            \\\"password\\\" : \\\"IAE-password\\\",\n            \\\"webhdfs\\\"  : \\\"webhdfs://ip-address:8443\\\"\n       }\"\n\n\n        (stream<rstring filename> Files) as HDFS2DirectoryScan_2 = HDFS2DirectoryScan() \n        { \n            param \n                directory     : \"/user/clsadmin/works\"; \n                credentials   : $credentials; \n                sleepTime     : 2.0; \n        } \n\nThis example uses the `HDFS2DirectoryScan` operator to scan a HDFS directory every two seconds. \nThe **hdfsUri** parameter in this case overrides the value that is specified by the `fs.defaultFS` option in the `core-site.xml`. \n\n\n    (stream<rstring filename> Files) as HDFS2DirectoryScan_3 = HDFS2DirectoryScan() \n    { \n        param \n            directory     : \"/user/myuser/\"; \n            hdfsUri       : \"hdfs://hdfsServer:8020\"; \n            sleepTime     : 2.0; \n    } \n")
@com.ibm.streams.operator.model.Icons(location32="impl/java/icons/HDFS2DirScan_32.gif", location16="impl/java/icons/HDFS2DirScan_16.gif")
@com.ibm.streams.operator.model.InputPorts(value={@com.ibm.streams.operator.model.InputPortSet(description="The `HDFS2DirectoryScan` operator has an optional control input port. You can use this port to change the directory that the operator scans at run time without restarting or recompiling the application. \nThe expected schema for the input port is of tuple<rstring directory>, a schema containing a single attribute of type rstring. \nIf a directory scan is in progress when a tuple is received, the scan completes and a new scan starts immediately after and uses the new directory that was specified. \nIf the operator is sleeping, the operator starts scanning the new directory immediately after it receives an input tuple. \n", cardinality=1, optional=true, controlPort=true, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@com.ibm.streams.operator.model.OutputPorts(value={@com.ibm.streams.operator.model.OutputPortSet(description="    The `HDFS2DirectoryScan` operator has one output port. \nThis port provides tuples of type rstring that are encoded in UTF-8 and represent the file names that are found in the directory, one file name per tuple.  The file names do not occur in any particular order. \nThe port is non-mutating and punctuation free. \n", cardinality=1, optional=false, windowPunctuationOutputMode=WindowPunctuationOutputMode.Free)})
@com.ibm.streams.operator.model.SharedLoader()
@com.ibm.streams.operator.internal.model.ShadowClass("com.ibm.streamsx.hdfs.HDFS2DirectoryScan")
@javax.annotation.Generated("com.ibm.streams.operator.internal.model.processors.ShadowClassGenerator")
public class HDFS2DirectoryScan$StreamsModel extends com.ibm.streams.operator.AbstractOperator
 {

@com.ibm.streams.operator.model.Parameter(name="hdfsUri", optional=true, description="This parameter specifies the uniform resource identifier (URI) that you can use to connect to \nthe HDFS file system.  The URI has the following format:\n* To access HDFS locally or remotely, use `hdfs://hdfshost:hdfsport` \n* To access GPFS locally, use `gpfs:///`. \n* To access GPFS remotely, use `webhdfs://hdfshost:webhdfsport`. \n* To access HDFS via a web connection for HDFS deployed on IBM Analytics Engine, use `webhdfs://webhdfshost:webhdfsport`. \n\nIf this parameter is not specified, the operator expects that the HDFS URI is specified as the `fs.defaultFS` or `fs.default.name` property in the `core-site.xml` HDFS configuration file.  The operator expects the `core-site.xml` \nfile to be in `$HADOOP_HOME/../hadoop-conf` or `$HADOOP_HOME/etc/hadoop`  or in the directory specified by the **configPath** parameter. \n**Note:** For connections to HDFS on IBM Analytics Engine, the `$HADOOP_HOME` environment variable is not supported and so either  **hdfsUri** or **configPath**  must be specified.")
@com.ibm.streams.operator.internal.model.MethodParameters({"hdfsUri"})
public void setHdfsUri(java.lang.String hdfsUri) {}

@com.ibm.streams.operator.model.Parameter(name="hdfsUser", optional=true, description="This parameter specifies the user ID to use when you connect to the HDFS file system. \nIf this parameter is not specified, the operator uses the instance owner ID to connect to HDFS. \nWhen connecting to Hadoop instances on IBM Analytics Engine, this parameter must be specified otherwise the connection will be unsuccessful. \nWhen you use Kerberos authentication, the operator authenticates with the Hadoop file system as the instance owner by using the \nvalues that are specified in the **authPrincipal** and **authKeytab** parameters.  After successful authentication, the \noperator uses the user ID that is specified by the **hdfsUser** parameter to perform all other operations on the file system.")
@com.ibm.streams.operator.internal.model.MethodParameters({"hdfsUser"})
public void setHdfsUser(java.lang.String hdfsUser) {}

@com.ibm.streams.operator.model.Parameter(name="hadfsPassword", optional=true, description="This parameter specifies the password to use when you connecting to a Hadoop instance deployed on IBM Analytics Engine. \nIf this parameter is not specified, attempts to connect to a Hadoop instance deployed on IBM Analytics Engine will cause an exception.")
@com.ibm.streams.operator.internal.model.MethodParameters({"hadfsPassword"})
public void setHdfsPassword(java.lang.String hadfsPassword) {}

@com.ibm.streams.operator.model.Parameter(name="reconnectionPolicy", optional=true, description="This optional parameter specifies the policy that is used by the operator to handle HDFS connection failures. \nThe valid values are: `NoRetry`, `InfiniteRetry`, and `BoundedRetry`. The default value is `BoundedRetry`. \nIf `NoRetry` is specified and a HDFS connection failure occurs, the operator does not try to connect to the HDFS again. \nThe operator shuts down at startup time if the initial connection attempt fails. \nIf `BoundedRetry` is specified and a HDFS connection failure occurs, the operator tries to connect to the HDFS again up to a maximum number of times. \nThe maximum number of connection attempts is specified in the **reconnectionBound** parameter.  The sequence of connection attempts occurs at startup time. \nIf a connection does not exist, the sequence of connection attempts also occurs before each operator is run. \nIf `InfiniteRetry` is specified, the operator continues to try and connect indefinitely until a connection is made. \nThis behavior blocks all other operator operations while a connection is not successful. \nFor example, if an incorrect connection password is specified in the connection configuration document, the operator remains in an infinite startup loop until a shutdown is requested.")
@com.ibm.streams.operator.internal.model.MethodParameters({"reconnectionPolicy"})
public void setReconnectionPolicy(java.lang.String reconnectionPolicy) {}

@com.ibm.streams.operator.model.Parameter(name="reconnectionBound", optional=true, description="This optional parameter specifies the number of successive connection attempts that occur when a connection fails or a disconnect occurs. \nIt is used only when the **reconnectionPolicy** parameter is set to `BoundedRetry`; otherwise, it is ignored. The default value is `5`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"reconnectionBound"})
public void setReconnectionBound(int reconnectionBound) {}

@com.ibm.streams.operator.model.Parameter(name="reconnectionInterval", optional=true, description="This optional parameter specifies the amount of time (in seconds) that the operator waits between successive connection attempts. \nIt is used only when the **reconnectionPolicy** parameter is set to `BoundedRetry` or `InfiniteRetry`; othewise, it is ignored.  The default value is `10`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"reconnectionInterval"})
public void setReconnectionInterval(double reconnectionInterval) {}

@com.ibm.streams.operator.model.Parameter(name="authPrincipal", optional=true, description="This parameter specifies the Kerberos principal that you use for authentication. \nThis value is set to the principal that is created for the IBM Streams instance owner. \nYou must specify this parameter if you want to use Kerberos authentication.")
@com.ibm.streams.operator.internal.model.MethodParameters({"authPrincipal"})
public void setAuthPrincipal(java.lang.String authPrincipal) {}

@com.ibm.streams.operator.model.Parameter(name="authKeytab", optional=true, description="This parameter specifies the file that contains the encrypted keys for the user that is specified by the **authPrincipal** parameter. \nThe operator uses this keytab file to authenticate the user. \nThe keytab file is generated by the administrator.  You must specify this parameter to use Kerberos authentication.")
@com.ibm.streams.operator.internal.model.MethodParameters({"authKeytab"})
public void setAuthKeytab(java.lang.String authKeytab) {}

@com.ibm.streams.operator.model.Parameter(name="credFile", optional=true, description="This parameter specifies a file that contains login credentials. The credentials are used to connect to GPFS remotely by using the `webhdfs://hdfshost:webhdfsport` schema.  The credentials file must contain information about how to authenticate with IBM Analytics Engine when using the webhdfs schema. \nFor example, the file must contain the user name and password for an IBM Analytics Engine user. \nWhen connecting to HDFS instances deployed on IBM Analytics Engine, \nthe credentials are provided using the **hdfsUser** and **hdfsPassword** parameters.")
@com.ibm.streams.operator.internal.model.MethodParameters({"credFile"})
public void setCredFile(java.lang.String credFile) {}

@com.ibm.streams.operator.model.Parameter(name="configPath", optional=true, description="This parameter specifies the path to the directory that contains the `core-site.xml` file, which is an HDFS\nconfiguration file. If this parameter is not specified, by default the operator looks for the `core-site.xml` file in the following locations:\n* `$HADOOP_HOME/etc/hadoop`\n* `$HADOOP_HOME/conf`\n* `$HADOOP_HOME/lib` \n* `$HADOOP_HOME/`\n**Note:** For connections to Hadoop instances deployed on IBM Analytics Engine, the `$HADOOP_HOME` environment variable is not supported and should not be used.")
@com.ibm.streams.operator.internal.model.MethodParameters({"configPath"})
public void setConfigPath(java.lang.String configPath) {}

@com.ibm.streams.operator.model.Parameter(name="keyStorePath", optional=true, description="This optional parameter is only supported when connecting to a Hadoop instance deployed on IBM Analytics Engine. \nIt specifies the path to the keystore file, which is in PEM format. The keystore file is used when making a secure connection to the HDFS server and must contain the public certificate of the HDFS server that will be connected to. \n**Note: If this parameter is omitted, invalid certificates for secure connections will be accepted.**  If the keystore file does not exist, or if the certificate it contains is invalid, the operator terminates.. \nThe location of the keystore file can be absolute path on the filesystem or a path that is relative to the application directory. \nSee the section on 'SSL Configuration' in the main page of this toolkit's documentation for information on how to configure the keystore. \nThe location of the keystore file can be absolute path on the filesystem or a path that is relative to the application directory.")
@com.ibm.streams.operator.internal.model.MethodParameters({"keyStorePath"})
public void setKeyStorePath(java.lang.String keyStorePath) {}

@com.ibm.streams.operator.model.Parameter(name="keyStorePassword", optional=true, description="This optional parameter is only supported when connecting to a Hadoop instance deployed on IBM Analytics Engine. \nIt specifies the password for the keystore file. This attribute is specified when the **keyStore** attribute is specified and the keystore file is protected by a password. \nIf the keyStorePassword is invalid the operator terminates.")
@com.ibm.streams.operator.internal.model.MethodParameters({"keyStorePassword"})
public void setKeyStorePassword(java.lang.String keyStorePassword) {}

@com.ibm.streams.operator.model.Parameter(name="libPath", optional=true, description="This optional parameter specifies the absolute path to the directory that contains the Hadoop library files. \nIf this parameter is omitted and `$HADOOP_HOME` is not set, the apache hadoop specific libraries within the `impl/lib/ext` folder of the toolkit will be used. \nWhen specified, this parameter takes precedence over the `$HADOOP_HOME` environment variable and the libraries within the folder indicated by `$HADOOP_HOME` will not be used.")
@com.ibm.streams.operator.internal.model.MethodParameters({"libPath"})
public void setLibPath(java.lang.String libPath) {}

@com.ibm.streams.operator.model.Parameter(name="policyFilePath", optional=true, description="This optional parameter is relevant when connecting to IBM Analytics Engine on IBM Cloud. \nIt specifies the path to the directory that contains the Java Cryptography Extension policy files (US_export_policy.jar and local_policy.jar). \nThe policy files enable the Java operators to use encryption with key sizes beyond the limits specified by the JDK. \nSee the section on 'Policy file configuration' in the main page of this toolkit's documentation for information on how to configure the policy files. \nIf this parameter is omitted the JVM default policy files will be used. When specified, this parameter takes precedence over the JVM default policy files. \n\n**Note:** This parameter changes a JVM property. If you set this property, be sure it is set to the same value in all HDFS operators that are in the same PE. \nThe location of the policy file directory can be absolute path on the file system or a path that is relative to the application directory.")
@com.ibm.streams.operator.internal.model.MethodParameters({"policyFilePath"})
public void setPolicyFilePath(java.lang.String policyFilePath) {}

@com.ibm.streams.operator.model.Parameter(name="credentials", optional=true, description="This optional parameter specifies the JSON string that contains the hdfs credentials: **user**, **password** and **hdfsUri** or **webhdfs**. \n\nThis parameter can also be specified in an application configuration.\n\nThe JSON string must to have the following format:\n\n    {\n        \"user\"     : \"clsadmin\",\n        \"password\" : \"IAE-password\",\n        \"webhdfs\"  : \"webhdfs://ip-address:8443\"\n    }\n")
@com.ibm.streams.operator.internal.model.MethodParameters({"credentials"})
public void setcredentials(java.lang.String credentials) {}

@com.ibm.streams.operator.model.Parameter(name="appConfigName", optional=true, description="This optional parameter specifies the name of the application configuration that contains HDFS connection related configuration parameters.  The 'credentials', 'hdfsUser' and 'hdfsPassword' and 'hdfsUrl' parameter can be set in an application configuration.  If a value is specified in the application configuration and as operator parameter, the application configuration parameter value takes precedence. ")
@com.ibm.streams.operator.internal.model.MethodParameters({"appConfigName"})
public void setAppConfigName(java.lang.String appConfigName) {}

@com.ibm.streams.operator.model.Parameter(name="directory", optional=true, description="This optional parameter specifies the name of the directory to be scanned. \nIf the name starts with a slash, it is considered an absolute directory that you want to scan. If it does not start with a slash, it is considered a relative directory, relative to the '/user/*userid*/ directory. This parameter is mandatory if the input port is not specified. \n")
@com.ibm.streams.operator.internal.model.MethodParameters({"directory"})
public void setDirectory(java.lang.String directory) {}

@com.ibm.streams.operator.model.Parameter(name="pattern", optional=true, description="This optional parameter limits the file names that are listed to the names that match the specified regular expression. \nThe `HDFS2DirectoryScan` operator ignores file names that do not match the specified regular expression. \n")
@com.ibm.streams.operator.internal.model.MethodParameters({"pattern"})
public void setPattern(java.lang.String pattern) {}

@com.ibm.streams.operator.model.Parameter(name="initDelay", optional=true, description="This parameter specifies the time to wait in seconds before the operator reads the first file. \nThe default value is `0`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"initDelay"})
public void setInitDelay(double initDelay) {}

@com.ibm.streams.operator.model.Parameter(name="sleepTime", optional=true, description="This optional parameter specifies the minimum time between directory scans. The default value is 5.0 seconds. \n")
@com.ibm.streams.operator.internal.model.MethodParameters({"sleepTime"})
public void setSleepTime(double sleepTime) {}

@com.ibm.streams.operator.model.Parameter(name="strictMode", optional=true, description="This optional parameter determines whether the operator reports an error if the directory to be scanned does not exist. \nIf you set this parameter to true and the specified directory does not exist or there is a problem accessing the directory, the operator reports an error and terminates. \nIf you set this parameter to false and the specified directory does not exist or there is a problem accessing the directory, the operator treats it as an empty directory and does not report an error. \n")
@com.ibm.streams.operator.internal.model.MethodParameters({"strictMode"})
public void setStrictMode(boolean strictMode) {}
}