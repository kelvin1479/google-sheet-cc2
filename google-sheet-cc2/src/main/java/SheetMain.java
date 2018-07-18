package main.java;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import spreadsheet.struct.StructDefine;
import spreadsheet.struct.StructDefine.Cell;

import com.google.api.services.script.Script;
import com.google.api.services.script.model.Content;
import com.google.api.services.script.model.CreateProjectRequest;
import com.google.api.services.script.model.File;
import com.google.api.services.script.model.Project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class SheetMain {
	private static final String APPLICATION_NAME = "Google Sheet CC2";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.

	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved credentials/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private static final String CLIENT_SECRET_DIR = "client_secret.json";

	/**
	 * Creates an authorized Credential object.
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If there is no client_secret.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = SheetMain.class.getResourceAsStream(CLIENT_SECRET_DIR);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
				.setAccessType("offline")
				.build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public static Sheets createSheetsService() throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
		NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}


	public static void main(String... args) throws IOException, GeneralSecurityException {

		Scanner scanner = new Scanner(System.in);
		//        System.out.print("Input spreadsheetID: ");
		// for testing, login to the google acc and choose any spreadsheet, find the ID in the URL 
		String spreadsheetId = "16B5hitBc1CHs0F_Lxo-WS0dc_JB3EYuQsmvZy42jcxA";

		Sheets service = createSheetsService();
		List<Sheet> sheetList = service.spreadsheets().get(spreadsheetId).execute().getSheets();
		System.out.println("List of sheets: ");
		int i=0;
		for(Sheet ss : sheetList) {
			System.out.print(i++ + ".\t");
			System.out.println(ss.getProperties().getTitle());
		}
		System.out.print("Input sheet number: ");
		int sheetIndex = scanner.nextInt();
		System.out.print("Input range (e.g. A1:B2), Enter n to fetch whole sheet: ");
		String sheetRange = scanner.next();
		scanner.close();
		Sheet chosen = sheetList.get(sheetIndex);
		String sheetTitle = chosen.getProperties().getTitle();
		String range = (sheetRange.equals("n")) ? sheetTitle : sheetTitle+"!"+sheetRange;

		Get request = service.spreadsheets().values()
				.get(spreadsheetId, range);
		request.setValueRenderOption("FORMULA");
//		GridRange gRange = new GridRange();
//		gRange.setSheetId(sheetList.get(sheetIndex).getProperties().getSheetId())
//			  .setStartRowIndex(0).setStartColumnIndex(0);
//		System.out.println(gRange);
		ValueRange response = request.execute();
		List<List<Object>> values = response.getValues();
		if (values == null || values.isEmpty()) {
			System.out.println("No data found.");
		} else {
//			int rowCount = 0;
//			int colCount = 0;
//			for (List<Object> row : values ) {
//			    if(row != null) {
//			    		if(row.size()>colCount) colCount = row.size();
//			    }
//			    rowCount++;
//			}
//			Cell[][] cellArray = new Cell[rowCount][colCount];
//			int rCount = 0;
//			for (List<Object> row : values ) {
//			    if(row != null) {
//			    		for(int cCount = 0; cCount<row.size(); cCount++) 
//			    			cellArray[rCount][cCount] = (row.get(cCount) == null) ? new Cell() : new Cell(row.get(cCount));
//			    }
//			    rCount++;
//			}
			int rowNum = 0;
			for (List<Object> row : values) {
				System.out.print(rowNum++ + "\t");
				if(row != null) {
					for(int j=0; j<row.size(); j++)
						System.out.printf("%-30s", new StructDefine.Cell(row.get(j)).getValue());
				}
				System.out.println();

			}
		}
	}
}