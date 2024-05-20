package cabprestservices.ccrossabprest.ccrossabp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cabprestservices.ccrossabprest.ccrossabp.model.XmlFileData;

@Component
public class DBUtils {

    private Connection connection;
	private static final Logger logger = LoggerFactory.getLogger(DBUtils.class);
    private final String dbOraUser;
    private final String dbOraHost;
    private final String dbOraPass;
    private final String dbOraPort;
    private final String dbOraSID;

    @Autowired
    public DBUtils() {
        this.dbOraUser = System.getenv("APP_DB_USER");
        this.dbOraHost = System.getenv("APP_DB_NODE");
        this.dbOraPass = System.getenv("APP_DB_PASS");
        this.dbOraPort = System.getenv("APP_ORACLE_PORT");
        this.dbOraSID = System.getenv("ORACLE_SID");
    }
    
    public String setupDatabaseConnection() throws SQLException, ClassNotFoundException {
		String dbOraUrl=null;
		if (this.connection == null || this.connection.isClosed()) {

            String dbUrl = "(description=(address=(protocol=tcp)(host=" + dbOraHost + ")(port=" + dbOraPort + "))(connect_data=(sid=" + dbOraSID + ")))";
			logger.info("dbUrl : " + dbUrl);

			dbOraUrl = "jdbc:oracle:thin:" + dbOraUser + "/" + dbOraPass + "@" + dbUrl;
			logger.info("dbOraUrl : " + dbOraUrl);

			Class.forName("oracle.jdbc.driver.OracleDriver");
			this.connection = DriverManager.getConnection(dbOraUrl);
		}

		return dbOraUrl;
	}


    public XmlFileData executeQuery(String billId) throws SQLException {
		XmlFileData xmlFileData = new XmlFileData();

		try (PreparedStatement pstmt = this.connection.prepareStatement("SELECT XML_FILE_NAME, XML_FILE_PATH FROM ADD9_XML_INFO WHERE DOC_SEQ_NO=?")) {

			logger.info("billId : " + billId);
			pstmt.setLong(1, Long.parseLong(billId));

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					xmlFileData.setXmlFileName(rs.getString("XML_FILE_NAME"));
					xmlFileData.setXmlFilePath(rs.getString("XML_FILE_PATH"));

					logger.info("xmlFileName :" + xmlFileData.getXmlFileName());
					logger.info("xmlFilePath :" + xmlFileData.getXmlFilePath());
				}
			}
		}

		return xmlFileData;
	}

    public void closeConnection() throws SQLException {
		if (this.connection != null) {
			this.connection.close();
		}
	}


	public String getAmddMaxXmlSize(String attrName) throws SQLException {
		String attrValue  = null;
		try (PreparedStatement pstmt = this.connection.prepareStatement("SELECT ATTRIBUTE_VALUE_GLOBE FROM ADD9_BILL_POLICY WHERE ATTRIBUTE_NAME=?")) {
			pstmt.setString(1, attrName);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					attrValue = rs.getString("ATTRIBUTE_VALUE_GLOBE");
				}
			}
		}
		logger.info(attrName+ " : " + attrValue);
		return attrValue;
	}
}

