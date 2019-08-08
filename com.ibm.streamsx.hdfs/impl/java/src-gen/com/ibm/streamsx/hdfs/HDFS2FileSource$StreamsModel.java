package com.ibm.streamsx.hdfs;
import com.ibm.streams.operator.metrics.Metric.Kind;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;

@com.ibm.streams.operator.model.PrimitiveOperator(name="HDFS2FileSource", namespace="com.ibm.streamsx.hdfs", description="The `HDFS2FileSource` operator reads files from a Hadoop Distributed File System (HDFS)\n\nThe operator opens a file on HDFS and sends out its contents in tuple format on its output port. \n\nIf the optional input port is not specified, the operator reads the HDFS file that is specified in the **file** parameter and \nprovides the file contents on the output port.  If the optional input port is configured, the operator reads the files that are \nnamed by the attribute in the tuples that arrive on its input port and places a punctuation marker between each file. \n\n # Behavior in a consistent region \nThe `HDFS2FileSource` operator can participate in a consistent region. \nThe operator can be at the start of a consistent region if there is no input port. \nThe operator supports periodic and operator-driven consistent region policies. \nIf the consistent region policy is set as operator driven, the operator initiates a drain after a file is fully read. \nIf the consistent region policy is set as periodic, the operator respects the period setting and establishes consistent states accordingly. \nThis means that multiple consistent states can be established before a file is fully read. \n\nAt checkpoint, the operator saves the current file name and file cursor location. \nIf the operator does not have an input port, upon application failures, the operator resets the file cursor back to the checkpointed location, and starts replaying tuples from the cursor location. \nIf the operator has an input port and is in a consistent region, the operator relies on its upstream operators to properly reply the filenames for it to re-read the files from the beginning. \n\n# Exceptions\n\nThe `HDFS2FileSource` operator terminates in the following cases:\n* The operator cannot connect to HDFS. \n* The file cannot be opened. \n* The file does not exist. \n* The file becomes unreadable. \n* A tuple cannot be created from the file contents (such as a problem with the file format). \n\n+ Examples\n\nThis example uses the `HDFS2DirectoryScan` operator to scan the HDFS directory every two seconds and the `HDFS2FileSource`\noperator to read the files that are output by the `HDFS2DirectoryScan` operator. \n\n//// HDFS2DirectoryScan operator scans /user/myser/ directory from HDFS every 2.0 seconds\n\n    (stream<rstring filename>; Files) as HDFS2DirectoryScan_1 = HDFS2DirectoryScan(){\n        param\n            directory     : \"/user/myuser/\"; \n            hdfsUri: \"hdfs : //hdfsServer:1883\"; \n            hdfsUser: \"streamsadmin\"; \n            hdfsPassword: \"Password\"; \n            sleepTime     : 2.0; \n    }\n\n    // HDFS2FileSource operator reads from files discovered by HDFS2DirectoryScan operator\n    //If the **keyStorePath** and **keyStorePassword** are omitted, the operator will accept all certificates as valid\n    (stream<rstring data> FileContent) as HDFS2FileSource_2 =    HDFS2FileSource(Files){\n         param\n            hdfsUri: \"hdfs://hdfsSever:8020\"; \n            hdfsUser: \"streamsadmin\"; \n           hdfsPassword: \"Password\"; \n    }\n\nThe following example shows the operator configured to access a HDFS instance on IBM Analytics Engine to read a file specified by the *file* parameter. \nThe **hdfsUser** and **hdfsPassword** are the username and password that have access to the Hadoop instance. \n\n    stream<rstring data> FileContent) as HDFS2FileSource_2 = HDFS2FileSource(){\n        param\n            hdfsUri: \"webhdfs://server_host_name:port\"; \n            file   : \"/user/streamsadmin/myfile.txt\"; \n            hdfsUser: \"streamsadmin\"; \n            hdfsPassword: \"Password\"; \n            keyStorePassword: \"storepass\"; \n            keyStorePath: \"etc/store.jks\"; \n    }\n")
@com.ibm.streams.operator.model.Icons(location32="impl/java/icons/HDFS2FileSource_32.gif", location16="impl/java/icons/HDFS2FileSource_16.gif")
@com.ibm.streams.operator.model.InputPorts(value={@com.ibm.streams.operator.model.InputPortSet(description="The `HDFS2FileSource` operator has one optional input port. If an input port is specified, the operator expects\nan input tuple with a single attribute of type rstring. The input tuples contain the file names that the operator opens for reading. \nThe input port is non-mutating. \n", cardinality=1, optional=true, controlPort=false, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@com.ibm.streams.operator.model.OutputPorts(value={@com.ibm.streams.operator.model.OutputPortSet(description="The `HDFS2FileSource` operator has one output port.  The tuples on the output port contain the data that is read from the files. \nThe operator supports two modes of reading.  To read a file line-by-line, the expected output schema of the output port is tuple<rstring line> or tuple<ustring line>. \nTo read a file as binary, the expected output schema of the output port is tuple<blob data>.  Use the blockSize parameter to control how much data to retrieve on each read. \nThe operator includes a punctuation marker at the conclusion of each file. The output port is mutating.", cardinality=1, optional=false, windowPunctuationOutputMode=WindowPunctuationOutputMode.Generating)})
@com.ibm.streams.operator.model.SharedLoader()
@com.ibm.streams.operator.internal.model.ShadowClass("com.ibm.streamsx.hdfs.HDFS2FileSource")
@javax.annotation.Generated("com.ibm.streams.operator.internal.model.processors.ShadowClassGenerator")
public class HDFS2FileSource$StreamsModel extends com.ibm.streams.operator.AbstractOperator
 {

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the uniform resource identifier (URI) that you can use to connect to \nthe HDFS file system.  The URI has the following format:\n* To access HDFS locally or remotely, use `hdfs://hdfshost:hdfsport` \n* To access GPFS locally, use `gpfs:///`. \n* To access GPFS remotely, use `webhdfs://hdfshost:webhdfsport`. \n* To access HDFS via a web connection for HDFS deployed on IBM Analytics Engine, use `webhdfs://webhdfshost:webhdfsport`. \n\nIf this parameter is not specified, the operator expects that the HDFS URI is specified as the `fs.defaultFS` or `fs.default.name` property in the `core-site.xml` HDFS configuration file.  The operator expects the `core-site.xml` \nfile to be in `$HADOOP_HOME/../hadoop-conf` or `$HADOOP_HOME/etc/hadoop`  or in the directory specified by the **configPath** parameter. \n**Note:** For connections to HDFS on IBM Analytics Engine, the `$HADOOP_HOME` environment variable is not supported and so either  **hdfsUri** or **configPath**  must be specified.")
@com.ibm.streams.operator.internal.model.MethodParameters({"hdfsUri"})
public void setHdfsUri(java.lang.String hdfsUri) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the user ID to use when you connect to the HDFS file system. \nIf this parameter is not specified, the operator uses the instance owner ID to connect to HDFS. \nWhen connecting to Hadoop instances on IBM Analytics Engine, this parameter must be specified otherwise the connection will be unsuccessful. \nWhen you use Kerberos authentication, the operator authenticates with the Hadoop file system as the instance owner by using the \nvalues that are specified in the **authPrincipal** and **authKeytab** parameters.  After successful authentication, the \noperator uses the user ID that is specified by the **hdfsUser** parameter to perform all other operations on the file system.")
@com.ibm.streams.operator.internal.model.MethodParameters({"hdfsUser"})
public void setHdfsUser(java.lang.String hdfsUser) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter specifies the policy that is used by the operator to handle HDFS connection failures. \nThe valid values are: `NoRetry`, `InfiniteRetry`, and `BoundedRetry`. The default value is `BoundedRetry`. \nIf `NoRetry` is specified and a HDFS connection failure occurs, the operator does not try to connect to the HDFS again. \nThe operator shuts down at startup time if the initial connection attempt fails. \nIf `BoundedRetry` is specified and a HDFS connection failure occurs, the operator tries to connect to the HDFS again up to a maximum number of times. \nThe maximum number of connection attempts is specified in the **reconnectionBound** parameter.  The sequence of connection attempts occurs at startup time. \nIf a connection does not exist, the sequence of connection attempts also occurs before each operator is run. \nIf `InfiniteRetry` is specified, the operator continues to try and connect indefinitely until a connection is made. \nThis behavior blocks all other operator operations while a connection is not successful. \nFor example, if an incorrect connection password is specified in the connection configuration document, the operator remains in an infinite startup loop until a shutdown is requested.")
@com.ibm.streams.operator.internal.model.MethodParameters({"reconnectionPolicy"})
public void setReconnectionPolicy(java.lang.String reconnectionPolicy) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter specifies the number of successive connection attempts that occur when a connection fails or a disconnect occurs. \nIt is used only when the **reconnectionPolicy** parameter is set to `BoundedRetry`; otherwise, it is ignored. The default value is `5`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"reconnectionBound"})
public void setReconnectionBound(int reconnectionBound) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter specifies the amount of time (in seconds) that the operator waits between successive connection attempts. \nIt is used only when the **reconnectionPolicy** parameter is set to `BoundedRetry` or `InfiniteRetry`; othewise, it is ignored.  The default value is `10`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"reconnectionInterval"})
public void setReconnectionInterval(double reconnectionInterval) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the Kerberos principal that you use for authentication. \nThis value is set to the principal that is created for the IBM Streams instance owner. \nYou must specify this parameter if you want to use Kerberos authentication.")
@com.ibm.streams.operator.internal.model.MethodParameters({"authPrincipal"})
public void setAuthPrincipal(java.lang.String authPrincipal) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the file that contains the encrypted keys for the user that is specified by the **authPrincipal** parameter. \nThe operator uses this keytab file to authenticate the user. \nThe keytab file is generated by the administrator.  You must specify this parameter to use Kerberos authentication.")
@com.ibm.streams.operator.internal.model.MethodParameters({"authKeytab"})
public void setAuthKeytab(java.lang.String authKeytab) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies a file that contains login credentials. The credentials are used to connect to GPFS remotely by using the `webhdfs://hdfshost:webhdfsport` schema.  The credentials file must contain information about how to authenticate with IBM Analytics Engine when using the webhdfs schema. \nFor example, the file must contain the user name and password for an IBM Analytics Engine user. \nWhen connecting to HDFS instances deployed on IBM Analytics Engine, \nthe credentials are provided using the **hdfsUser** and **hdfsPassword** parameters.")
@com.ibm.streams.operator.internal.model.MethodParameters({"credFile"})
public void setCredFile(java.lang.String credFile) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the path to the directory that contains the `core-site.xml` file, which is an HDFS\nconfiguration file. If this parameter is not specified, by default the operator looks for the `core-site.xml` file in the following locations:\n* `$HADOOP_HOME/etc/hadoop`\n* `$HADOOP_HOME/conf`\n* `$HADOOP_HOME/lib` \n* `$HADOOP_HOME/`\n**Note:** For connections to Hadoop instances deployed on IBM Analytics Engine, the `$HADOOP_HOME` environment variable is not supported and should not be used.")
@com.ibm.streams.operator.internal.model.MethodParameters({"configPath"})
public void setConfigPath(java.lang.String configPath) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the password to use when you connecting to a Hadoop instance deployed on IBM Analytics Engine. \nIf this parameter is not specified, attempts to connect to a Hadoop instance deployed on IBM Analytics Engine will cause an exception.")
@com.ibm.streams.operator.internal.model.MethodParameters({"hadfsPassword"})
public void setHdfsPassword(java.lang.String hadfsPassword) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter is only supported when connecting to a Hadoop instance deployed on IBM Analytics Engine. \nIt specifies the path to the keystore file, which is in PEM format. The keystore file is used when making a secure connection to the HDFS server and must contain the public certificate of the HDFS server that will be connected to. \n**Note: If this parameter is omitted, invalid certificates for secure connections will be accepted.**  If the keystore file does not exist, or if the certificate it contains is invalid, the operator terminates.. \nThe location of the keystore file can be absolute path on the filesystem or a path that is relative to the application directory. \nSee the section on 'SSL Configuration' in the main page of this toolkit's documentation for information on how to configure the keystore. \nThe location of the keystore file can be absolute path on the filesystem or a path that is relative to the application directory.")
@com.ibm.streams.operator.internal.model.MethodParameters({"keyStorePath"})
public void setKeyStorePath(java.lang.String keyStorePath) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter is only supported when connecting to a Hadoop instance deployed on IBM Analytics Engine. \nIt specifies the password for the keystore file. This attribute is specified when the **keyStore** attribute is specified and the keystore file is protected by a password. \nIf the keyStorePassword is invalid the operator terminates.")
@com.ibm.streams.operator.internal.model.MethodParameters({"keyStorePassword"})
public void setKeyStorePassword(java.lang.String keyStorePassword) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter specifies the absolute path to the directory that contains the Hadoop library files. \nIf this parameter is omitted and `$HADOOP_HOME` is not set, the apache hadoop specific libraries within the `impl/lib/ext` folder of the toolkit will be used. \nWhen specified, this parameter takes precedence over the `$HADOOP_HOME` environment variable and the libraries within the folder indicated by `$HADOOP_HOME` will not be used.")
@com.ibm.streams.operator.internal.model.MethodParameters({"libPath"})
public void setLibPath(java.lang.String libPath) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter is relevant when connecting to IBM Analytics Engine on IBM Cloud. \nIt specifies the path to the directory that contains the Java Cryptography Extension policy files (US_export_policy.jar and local_policy.jar). \nThe policy files enable the Java operators to use encryption with key sizes beyond the limits specified by the JDK. \nSee the section on 'Policy file configuration' in the main page of this toolkit's documentation for information on how to configure the policy files. \nIf this parameter is omitted the JVM default policy files will be used. When specified, this parameter takes precedence over the JVM default policy files. \n\n**Note:** This parameter changes a JVM property. If you set this property, be sure it is set to the same value in all HDFS operators that are in the same PE. \nThe location of the policy file directory can be absolute path on the file system or a path that is relative to the application directory.")
@com.ibm.streams.operator.internal.model.MethodParameters({"policyFilePath"})
public void setPolicyFilePath(java.lang.String policyFilePath) {}

@com.ibm.streams.operator.model.Parameter(name="credentials", optional=true, description="This optional parameter specifies the JSON string that contains the hdfs credentials: **user**, **password** and **hdfsUri** or **webhdfs**. \n\nThis parameter can also be specified in an application configuration.\n\nThe JSON string must to have the following format:\n\n    {\n        \"user\"     : \"clsadmin\",\n        \"password\" : \"IAE-password\",\n        \"webhdfs\"  : \"webhdfs://ip-address:8443\"\n    }\n")
@com.ibm.streams.operator.internal.model.MethodParameters({"credentials"})
public void setcredentials(java.lang.String credentials) {}

@com.ibm.streams.operator.model.Parameter(name="appConfigName", optional=true, description="This optional parameter specifies the name of the application configuration that contains HDFS connection related configuration parameters.  The 'credentials', 'hdfsUser' and 'hdfsPassword' and 'hdfsUrl' parameter can be set in an application configuration.  If a value is specified in the application configuration and as operator parameter, the application configuration parameter value takes precedence. ")
@com.ibm.streams.operator.internal.model.MethodParameters({"appConfigName"})
public void setAppConfigName(java.lang.String appConfigName) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the name of the file that the operator opens and reads. \nThis parameter must be specified when the optional input port is not configured. \nIf the optional input port is used and the file name is specified, the operator generates an error.")
@com.ibm.streams.operator.internal.model.MethodParameters({"file"})
public void setFile(java.lang.String file) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This parameter specifies the time to wait in seconds before the operator reads the first file. \nThe default value is `0`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"initDelay"})
public void setInitDelay(double initDelay) {}

@com.ibm.streams.operator.model.Parameter(optional=true, description="This optional parameter specifies the encoding to use when reading files. The default value is `UTF-8`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"encoding"})
public void setEncoding(java.lang.String encoding) {}

@com.ibm.streams.operator.model.Parameter(name="blockSize", optional=true, description="This parameter specifies the maximum number of bytes to be read at one time when reading a file into binary mode (ie, into a blob); thus, it is the maximum size of the blobs on the output stream. The parameter is optional, and defaults to `4096`.")
@com.ibm.streams.operator.internal.model.MethodParameters({"inBlockSize"})
public void setBlockSize(int inBlockSize) {}
}