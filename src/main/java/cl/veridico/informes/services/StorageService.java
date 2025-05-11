package cl.veridico.informes.services;


import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;


@Service
public class StorageService {

    public void uploadPdfBytes(byte[] pdfBytes, String bucketName, String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
            .setContentType("application/pdf")
            .build();
        Storage storage = StorageOptions.getDefaultInstance().getService();
        storage.create(blobInfo, pdfBytes);
        System.out.println("Archivo subido a " + bucketName + " con nombre " + fileName);
    }
}