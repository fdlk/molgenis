package org.molgenis.amazon.bucket.client;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.file.FileStoreImpl;

import java.io.File;
import java.io.IOException;

public interface AmazonBucketClient
{
	AmazonS3 getClient(String accessKey, String secretKey, String region);

	File downloadFile(AmazonS3 s3Client, FileStoreImpl fileStore, String jobIdentifier, String bucketName,
			String keyName,
			boolean isExpression) throws IOException, AmazonClientException;
}
