package modelimport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

public class GDriveFileUploader {
    private String folderId;

    public GDriveFileUploader(String folderId) {
        this.folderId = folderId;
    }

    public void uploadModel(java.io.File localFile) throws IOException {
        String contentType = "";
                
        try {
            contentType = localFile.toURL().openConnection().getContentType();
        } catch(IOException ex) {
            System.out.println("EXCEPTION :()");
            ex.printStackTrace();
        }

        System.out.println("File content type: " + contentType);
        
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
            .createScoped(Arrays.asList(DriveScopes.DRIVE_FILE));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
            credentials);

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(new NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer)
            .setApplicationName("Alice AI Model Generator")
            .build();

        // File's metadata.
        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));
        FileContent mediaContent = new FileContent(contentType, localFile);
        try {
            File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();
            System.out.println("File ID: " + file.getId());
            //return file;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            throw e;
        }
    }
}