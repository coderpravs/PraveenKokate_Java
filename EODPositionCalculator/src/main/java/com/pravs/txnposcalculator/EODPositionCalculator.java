package com.pravs.txnposcalculator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravs.txnposcalculator.dto.EODPosition;
import com.pravs.txnposcalculator.dto.Transaction;

public class EODPositionCalculator {
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	static Logger log = Logger.getLogger(EODPositionCalculator.class.getName());
	
	public static void main(String[] args) {
		EODPositionCalculator obj= new EODPositionCalculator();
		obj.processEODPosition();
	}
	
	@Test
	public void junitTest(){
		processEODPosition();
		
		File file = new File(getApplicationPath()+ "\\src\\main\\resources\\Expected_EndOfDay_Positions.csv");
		assertTrue(file.exists());
	}
	
	
	public void processEODPosition() {
		Properties prop = getProperties();
		String inputFile = getApplicationPath()+prop.getProperty("inputFile");
		String outputFilePath = getApplicationPath()+prop.getProperty("outputFilePath");
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		Map<String, EODPosition> eodMap = new TreeMap<String, EODPosition>();

		try {
			
			br = new BufferedReader(new FileReader(inputFile));
			log.info("Reading Start of day position file success !! ");
			EODPosition eodPositionObj = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (i > 0) {
					String[] eodPosition = line.split(cvsSplitBy);
					eodPositionObj = new EODPosition();
					eodPositionObj.setInstrument(eodPosition[0]);

					if (eodPosition[1] != null) {
						eodPositionObj.setAccount(Integer.valueOf(eodPosition[1]));
					}
					eodPositionObj.setAccountType(eodPosition[2]);
					if (eodPosition[3] != null) {
						eodPositionObj.setQuantity(Integer.valueOf(eodPosition[3]));
						eodPositionObj.setOriginialQuantity(Integer.valueOf(eodPosition[3]));
					}
					// HashMap will have key as "Instrument#AccountType"
					eodMap.put(eodPosition[0] + "#" + eodPosition[2],eodPositionObj);
				}
				i = i + 1;
			}

			List<Transaction> transactionList = readInputTransactions(getApplicationPath()+ prop.getProperty("inputTxnFile"));
			if(transactionList==null){
				log.error("Oops ! input file not found. Please check following : \n 1. Json file path should be added 'config.properties'. \n 2. Json file should available on provided path as specified in property file & has access rights to read. \n 3. Json file should not be in use by any other process.");
				return;
			}
			int quantity = 0;
			int delta = 0;
			for (EODPosition eodPosition : eodMap.values()) {
				for (Transaction txnObj : transactionList) {
					quantity = 0;
					delta = 0;

					if (eodPosition.getInstrument().equalsIgnoreCase(txnObj.getInstrument())) {
						if (txnObj.getTransactionType().equalsIgnoreCase("B")) {
							if (eodPosition.getAccountType().equalsIgnoreCase("E")) {
								quantity = eodPosition.getQuantity()+ txnObj.getTransactionQuantity();
							} else if (eodPosition.getAccountType().equalsIgnoreCase("I")) {
								quantity = eodPosition.getQuantity()- txnObj.getTransactionQuantity();
							}
						} else {
							if (eodPosition.getAccountType().equalsIgnoreCase("E")) {
								quantity = eodPosition.getQuantity() - txnObj.getTransactionQuantity();
							} else if (eodPosition.getAccountType().equalsIgnoreCase("I")) {
								quantity = eodPosition.getQuantity() + txnObj.getTransactionQuantity();
							}
						}
						eodPosition.setQuantity(quantity);
						// Update map as per updated quantity
						eodMap.put(eodPosition.getInstrument() + "#"+ eodPosition.getAccountType(), eodPosition);
					}
				}
			}
			log.info("Quantity update successful !! ");

			// Calculate Delta
			for (EODPosition eodPosition : eodMap.values()) {
				delta = eodPosition.getQuantity() - eodPosition.getOriginialQuantity();
				eodPosition.setDelta(delta);
				eodMap.put(eodPosition.getInstrument() + "#"+ eodPosition.getAccountType(), eodPosition);
			}
			log.info("Delta update successful !! ");

			if(writeToCSVFile(eodMap, outputFilePath)){
				log.info("Expected End of day position file created successfully !! ");
			}

		} catch (FileNotFoundException e) {
			log.error("Oops ! input file not found. Please check following : \n 1. Input file path should be added 'config.properties' is correct or not. \n 2. File should available on provided path in property file & has access rights to read. \n 3. File should not be in use by any other process.");			
		} catch (IOException e) {
			log.error("Oops ! System is not able to read file.");		
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Oops ! Error while closing file from system.");
				}
			}
		}
	}

	public List<Transaction> readInputTransactions(String txnJsonFile) {
		List<Transaction> transactionList = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(txnJsonFile));
	        JSONArray txnJsonArr =  (JSONArray) obj;
	        
	        ObjectMapper mapper = new ObjectMapper();
			transactionList = Arrays.asList(mapper.readValue(txnJsonArr.toString(),Transaction[].class));
		} catch (JsonParseException e) {
			log.error("Oops ! Exception while parsing transaction json provided. Please validate structure of json is correct or not.");
		} catch (JsonMappingException e) {
			log.error("Oops ! Exception while mapping provided transaction json with system object. Please validate structure of json is correct or not.");
		} catch (IOException e) {
			log.error("Oops ! Json file not found. Please check following : \n 1. Json file path should be added 'config.properties' is correct or not. \n 2. Json file should available on provided path in property file & has access rights to read. \n 3. File should not be in use by any other process.");
		} catch (ParseException e) {
			log.error("Oops ! Error while parsing the json, Please validate format of json.");
		}

		return transactionList;

	}

	public boolean writeToCSVFile(Map<String, EODPosition> eodMap,
			String outputFilePath) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(outputFilePath+ "Expected_EndOfDay_Positions.csv");

			// Write the CSV file header
			fileWriter.append("Instrument,Account,AccountType,Quantity,Delta");

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Write a new student object list to the CSV file
			for (EODPosition eodPositionObj : eodMap.values()) {
				fileWriter.append(eodPositionObj.getInstrument());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(eodPositionObj.getAccount()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(eodPositionObj.getAccountType());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(eodPositionObj.getQuantity()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(eodPositionObj.getDelta()));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
		} catch (Exception e) {
			log.error("Oops ! output file path not found/incorrect. Please check following : \n 1. Output file path should be added 'config.properties'. \n 2. Folder path should available on provided path in property file & has access rights to write files.");
			return false;
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.flush();
					fileWriter.close();
				}
			} catch (IOException e) {
				log.error("Oops ! Error while flushing/closing fileWriter.");
			}
		}
		return true;
	}

	public Properties getProperties() {
		Properties prop = new Properties();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream stream = loader.getResourceAsStream("config.properties");
			prop.load(stream);

		} catch (IOException io) {
			log.error("Oops ! Error while reading property file.");
		}
		return prop;
	}
	
	public String getApplicationPath() {
		Path currentRelativePath = Paths.get("");
		return currentRelativePath.toAbsolutePath().toString();
	}

}
