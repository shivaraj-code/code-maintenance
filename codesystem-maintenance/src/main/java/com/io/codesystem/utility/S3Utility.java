package com.io.codesystem.utility;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
	@Component
	public class S3Utility {

	    private AmazonS3 s3Client = null;

	    public S3Utility() {
	        try {
	            s3Client = AmazonS3ClientBuilder.standard().build();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    public List<S3ObjectSummary> getS3Files(String sourceBucketName, String sourceBucketPrefix) {
	        ListObjectsRequest lor = new ListObjectsRequest()
	                .withBucketName(sourceBucketName)
	                .withPrefix(sourceBucketPrefix);
	        System.out.println(lor.getBucketName());
	        ObjectListing objectListing = s3Client.listObjects(lor);
	        return objectListing.getObjectSummaries();
		    }
	    
	   public InputStream getS3File(String sourceBucketName, String filename) {
	        S3Object object = s3Client.getObject(new GetObjectRequest(sourceBucketName, filename));
	        byte[] byteArray=null;
	        try {
	        	byteArray= object.getObjectContent().readAllBytes();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return new ByteArrayInputStream(byteArray);
	    }
	   
	   public void delete(String sourceBucketName, String keyName) {
	        try {
	            s3Client.deleteObject(new DeleteObjectRequest(sourceBucketName, keyName));
	        } catch (Exception ase) {
	            ase.printStackTrace();
	        }
	    }
	   public boolean isExist(String sourceBucketName, String filename) {
	        return s3Client.doesObjectExist(sourceBucketName, filename);
	    }
	   
	   public void moveFile(String sourceBucketName, String key, String targetBucket, String targetPath, boolean sourceDelete) {
	        Path path = Paths.get(key);
	        Path filePath = path.getFileName();
	        String fileName = filePath.toString();
	        System.out.println("FileName: " + fileName);
	        s3Client.copyObject(new CopyObjectRequest(sourceBucketName, key, targetBucket, targetPath + "/" + fileName));
	        s3Client.deleteObject(sourceBucketName, key);
	    }
	    public void saveFile(String destinationBucketName, String filename, InputStream stream) throws IOException {
	        ObjectMetadata objMetadata = new ObjectMetadata();
	        System.out.println("IN SAVING THE FILE:"+destinationBucketName+":"+filename);
	        objMetadata.setContentLength(stream.available());
	        s3Client.putObject(destinationBucketName, filename, stream, objMetadata);
	        //s3Client.setObjectAcl(destinationBucketName, filename, CannedAccessControlList.PublicRead);
	    }
	    
	   
	   
}
