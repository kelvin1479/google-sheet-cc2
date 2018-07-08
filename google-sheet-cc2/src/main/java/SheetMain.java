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
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    
    public static List<Sheet> getSheetList(Sheets service, String spreadsheetId) throws IOException{
	    	Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
	    	List<Sheet> sheets = sp.getSheets();
	    	return sheets;
    }
    

    public static void main(String... args) throws IOException, GeneralSecurityException {
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Input spreadsheetID: ");
// 		use 19VFf3p2iVPj950875E9xLB_qJfqkEda6fESBgjslu6I for testing now
        String spreadsheetId = scanner.nextLine();
//        System.out.print("Input sheet name: ");
//        String sheetName = scanner.nextLine();
//        System.out.print("Input range (e.g. A1:B2) : ");
//        String sheetRange = scanner.nextLine();
//        final String range = sheetName + "!" + sheetRange;
        scanner.close();
        
        Sheets service = createSheetsService();
        List<Sheet> sheetList = getSheetList(service, spreadsheetId);
        for(Sheet ss : sheetList) {
        		System.out.println(ss.getProperties().getTitle());
        }
//        Get request = service.spreadsheets().values()
//                .get(spreadsheetId, range);
//        ValueRange response = request.execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//        } else {
//            System.out.println("N\t mean\t min\t max");
//            for (List<Object> row : values) {
//                // Print columns A and E, which correspond to indices 0 and 4.
//                System.out.printf("%s\t %s\t %s\t %s\n", row.get(0), row.get(1), row.get(2), row.get(3));
//            }
//        }
    }
}