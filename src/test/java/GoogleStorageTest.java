import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by nataniel.neto on 22/03/2016.
 */
public class GoogleStorageTest{

    private static Storage storage;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static HttpTransport httpTransport;

    @Test
    public void AuthenticationTest() throws Exception {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // EditalStorage-1d3c27c26349.json é a Service Account Key gerada no Credentials do API Manager do Google Cloud Platform
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("src/test/resources/EditalStorage-1d3c27c26349.json"))
                .createScoped(Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL));

        storage = new Storage.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("EditalStorage").build();

        // Get metadata about the specified bucket.
        Storage.Buckets.Get getBucket = storage.buckets().get("editalstorage.appspot.com");
        getBucket.setProjection("full");
        Bucket bucket = getBucket.execute();
        System.out.println("name: " + "editalstorage.appspot.com");
        System.out.println("location: " + bucket.getLocation());
        System.out.println("timeCreated: " + bucket.getTimeCreated());
        System.out.println("owner: " + bucket.getOwner());

        // List the contents of the bucket.
        Storage.Objects.List listObjects = storage.objects().list("editalstorage.appspot.com");
        com.google.api.services.storage.model.Objects objects;
        do {
            objects = listObjects.execute();
            List<StorageObject> items = objects.getItems();
            if (null == items) {
                System.out.println("There were no objects in the given bucket; try adding some and re-running.");
                break;
            }
            for (StorageObject object : items) {
                System.out.println(object.getName() + " (" + object.getSize() + " bytes)");
            }
            listObjects.setPageToken(objects.getNextPageToken());
        } while (null != objects.getNextPageToken());
    }
}
