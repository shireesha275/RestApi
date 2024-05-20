package cabprestservices.ccrossabprest.ccrossabp.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import amdocs.acmarch.exceptions.ACMException;
import cabprestservices.ccrossabprest.ccrossabp.service.AmddService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * This class represents the controller for handling requests related to Amdd operations.
 * It provides methods for retrieving XML files, setting up database connections, and handling security tokens.
 */
@OpenAPIDefinition(
	    info = @Info(title = "CCROSSABP rest ", version = "v1"),
	    security = @SecurityRequirement(name = "api")
	)
	@SecuritySchemes({
	    @SecurityScheme(
	    		name = "api",
	    	    type = SecuritySchemeType.APIKEY,
	    	    in = SecuritySchemeIn.HEADER,
	    	    paramName = "Authorization"
	    )
	})
@RestController
public class AmddController {

	public static final String ERROR_INVALID_SECURITY_TOKEN = "UNAUTHORIZED. The ASM Security Token is INVALID.";
	private static final Logger logger = LoggerFactory.getLogger(AmddController.class);

	private final AmddService amddService;

    @Autowired
    public AmddController(AmddService amddService) {
        this.amddService = amddService;
    }
	
	@PostMapping(value="/getMyBillXml", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@SecurityRequirement(name = "api")
	public ResponseEntity<?> getMyBillXml(@RequestHeader(value = "Authorization", required = false) String secTicket, @RequestBody String billId) {

		if(secTicket == null) {
			logger.info("secTicket is null");
		    return new ResponseEntity<>("Security ticket is null", HttpStatus.UNAUTHORIZED);
		}
		if(billId == null) {
			logger.info("billId is null");
		    return new ResponseEntity<>("Bill ID is null", HttpStatus.BAD_REQUEST);
		}
		if (!billId.matches("\\d+")) {
			logger.error("Invalid billId format");
			return new ResponseEntity<>("Invalid billId format", HttpStatus.BAD_REQUEST);
		}

		logger.info("secTicket: " + secTicket);
		try {
			if(!amddService.isTicketValid(secTicket)){
				logger.error(ERROR_INVALID_SECURITY_TOKEN);
				return new ResponseEntity<>(ERROR_INVALID_SECURITY_TOKEN, HttpStatus.UNAUTHORIZED);
			}
		} catch (ACMException e) {
			logger.error("ACMException occurred: " + e.getMessage());
			return new ResponseEntity<>("Error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		byte[] fileBytes;
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Content-Type-Options", "nosniff");

		try {
			fileBytes = amddService.getXMLFileBytes(billId, headers);
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while processing the database operation: " + e.getMessage());
			return new ResponseEntity<>("Error occurred while fetching the XML file: " + e.getMessage(),HttpStatus.NOT_FOUND);
		} catch (NumberFormatException e) {	
            return new ResponseEntity<>("Error occurred while processing request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
		 catch (ClassNotFoundException | SQLException e) {
			logger.error("Error occurred while processing the database operation: " + e.getMessage());
			return new ResponseEntity<>("Error occurred while processing the database operation. ", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error("Error occurred while fetching the XML file: " + e.getMessage());
			return new ResponseEntity<>("Error occurred while fetching the XML file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (InterruptedException e) {
			return new ResponseEntity<>("Error occurred while fetching the XML file.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

	}


}