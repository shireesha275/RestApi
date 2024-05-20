package cabprestservices.ccrossabprest.ccrossabp.service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import amdocs.acmarch.exceptions.ACMException;
import amdocs.acmarch.security.LoginHelper;
import cabprestservices.ccrossabprest.ccrossabp.model.XmlFileData;
import cabprestservices.ccrossabprest.ccrossabp.utils.DBUtils;




@Service
public class AmddService {

    private final DBUtils dbutils;
    Double maxFileSize; //NOSONAR
    private static final Logger logger = LoggerFactory.getLogger(AmddService.class);

    @Autowired
    public AmddService(DBUtils dbutils) {
        this.dbutils = dbutils;
    }

    public Boolean isTicketValid(String secTicket) throws ACMException {
        return LoginHelper.isValidTicket(secTicket);
    }

    public byte[] getXMLFileBytes(String billId, HttpHeaders headers) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException, InterruptedException{
        String xmlFileName = null;
        String xmlFilePath = null;
        XmlFileData xmlFileData = getXMLFileDetails( billId);

        xmlFileName = xmlFileData.getXmlFileName();
        xmlFilePath = xmlFileData.getXmlFilePath();

        if (xmlFileName == null || xmlFilePath == null) {
            logger.error("No XML representation available for the queried bill ID.");
            throw new FileNotFoundException("No XML representation available for the queried bill ID.");
        }

        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment", xmlFileName);
        byte[] fileBytes;
        fileBytes = fetchXmlFile(xmlFileName, xmlFilePath);

        return fileBytes;
    }




    public XmlFileData getXMLFileDetails(String billId) throws NumberFormatException, SQLException, ClassNotFoundException {
      
        XmlFileData xmlFileData;

        try {
            dbutils.setupDatabaseConnection();
            xmlFileData = dbutils.executeQuery( billId);
            logger.info("fetching GETMYBILLXML_MAX_XML_SIZE from ADD9_BILL_POLICY");
            String genCode = dbutils.getAmddMaxXmlSize ("GETMYBILLXML_MAX_XML_SIZE");
            if (genCode != null && !genCode.trim().isEmpty()) {
                maxFileSize = Double.parseDouble(genCode);
            } else {
                logger.info("GETMYBILLXML_MAX_XML_SIZE value was not found in ADD9_BILL_POLICY, setting 10MB as default value");
                maxFileSize = 10.0;
            }
            
        }
         catch (ClassNotFoundException e) {
            logger.error("Error occurred while getting database connection: " + e.getMessage());
            throw e;
        } catch (NumberFormatException e) {
            logger.error("Error occurred while parsing the maxFileSize: " + e.getMessage());
            throw new NumberFormatException("Error occurred while processing request.");
        }
        catch (SQLException e) {
            logger.error("Error occurred while processing the database operation: " + e.getMessage());
            throw e;
        }finally {
			try {
				dbutils.closeConnection();
			} catch (SQLException e) {
				logger.error("Error occurred while closing the database connection: " + e.getMessage());
                throw e;
			}
		}

        return xmlFileData;
    }



    public byte[] fetchXmlFile(String xmlFileName, String xmlFilePath) throws FileNotFoundException, IOException, InterruptedException {
        if (xmlFileName.endsWith(".gz")) {
            xmlFileName = xmlFileName.substring(0, xmlFileName.length() - 3);
        }

        File file = new File(xmlFilePath + File.separator + xmlFileName);
        if (!file.exists() || !file.isFile()) {
            xmlFileName = xmlFileName.concat(".gz");
            file = new File(xmlFilePath + File.separator + xmlFileName);
            if (!file.exists() || !file.isFile()) {
                logger.info(xmlFilePath + File.separator + xmlFileName + " not found");
                throw new IOException("XML file not found.");
            }
        }

        long fileSizeInBytes = file.length();
        double fileSizeInMB = (double) fileSizeInBytes / (1024.0 * 1024.0);
        if (fileSizeInMB > maxFileSize) {
            logger.error("File size is above "+maxFileSize+"MB and cannot be handled. File size: " + fileSizeInMB + " MB"); //NOSONAR
            throw new IOException("File size is above "+maxFileSize+"MB and cannot be handled.");
        }

        logger.info("File size: " + fileSizeInMB + " MB");

        byte[] fileBytes;
        if (xmlFileName.endsWith(".gz")) {
            logger.info("Unzipping the file: " + xmlFileName);
            ProcessBuilder pb = new ProcessBuilder("gzip", "-d", file.getAbsolutePath());
            Process p = pb.start();
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                logger.error("Error while unzip of gz file: "+xmlFileName + e.getMessage());
                throw new InterruptedException("Error occurred while fetching the XML file.");
            }

            logger.info("Checking if unzipped file exists: " +xmlFilePath + File.separator + xmlFileName.substring(0, xmlFileName.length() - 3));

            file = new File(xmlFilePath + File.separator + xmlFileName.substring(0, xmlFileName.length() - 3));
            if (!file.exists()) {
                logger.error("Failed to decompress gzip file " + xmlFileName);
                throw new IOException("Error occurred while fetching the XML file.");
            }

            logger.info("Checking the size of decompressed file." );
            
            double xmlFileSizeInMB = (double)Files.size(file.toPath()) / (1024.0 * 1024.0);
            logger.info("File size: " + xmlFileSizeInMB + " MB");
            if (xmlFileSizeInMB > maxFileSize) {
                logger.error("File size is above "+maxFileSize+"MB after decompression. File size: " + fileSizeInMB + " MB");
                throw new IOException("File size is above "+maxFileSize+"MB and cannot be handled.");
            }

        }
        
        logger.info("Reading file bytes." );
        
        try {
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            logger.error("Error occurred while reading file bytes: " + e.getMessage());
            throw new IOException("Error occurred while reading file bytes.");
        }

        return fileBytes;
    }
}
