package wasdev.sample.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;




/**
 * Servlet implementation class SimpleServlet
 */
@WebServlet("/SimpleServlet")
public class SimpleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        
        final String accessKey = "44406ac4fa12457e8054fdb82d5b7a62";
        final String secretKey = "f39bde24e060fdc9ca7f34ac5d52af126c475f97b0e2445a";

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey); // declare a new set of basic credentials that includes the Access Key ID and the Secret Access Key
        AmazonS3 cos = new AmazonS3Client(credentials); // create a constructor for the client by using the declared credentials.
        cos.setEndpoint("https://s3.us-east.cloud-object-storage.appdomain.cloud");
              
        
        try {
        	List<Bucket> Buckets = cos.listBuckets(); // get a list of buckets

        	for (Bucket b : Buckets) { // for each bucket...
        	  System.out.println("Found"+b.getName()); // display 'Found: ' and then the name of the bucket
        	}
        	
        	ObjectListing objectListing = cos.listObjects(new ListObjectsRequest().withBucketName("cloud-object-storage-l7-cos-standard-61l"));
                  	
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            	if(objectSummary.getKey().substring(objectSummary.getKey().lastIndexOf('.') + 1).equals("mp4")) {
            		String Name =objectSummary.getKey().substring(0,objectSummary.getKey().lastIndexOf('.'));
                System.out.println(" - " + objectSummary.getKey() + "  " +
                                   "(size = " + objectSummary.getSize() + ")");
          	
        	 	S3Object returned = cos.getObject( // request the object by identifying
        			"cloud-object-storage-l7-cos-standard-61l", // the name of the bucket
        			objectSummary.getKey()//"Shop1.mp4" // the name of the serialized object
        			);
        	S3ObjectInputStream s3Input = returned.getObjectContent(); // set the object stream


        		
            Scanner s=new Scanner(System.in);  

         	Java2DFrameConverter converter = new Java2DFrameConverter();  
             FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(s3Input);  
             frameGrabber.start();
          
             double frameRate=frameGrabber.getFrameRate();  
             int imgNum = 0;  
             //System.out.println("Video has "+frameGrabber.getLengthInFrames()+" frames and has frame rate of "+frameRate);  
            
               for(int ii=1;ii<=frameGrabber.getLengthInFrames();ii++){  
               imgNum++;       
               frameGrabber.setFrameNumber(ii);  
               Frame frame = frameGrabber.grab();  
               BufferedImage bi = converter.convert(frame);  
               String path = "../../../../../LiveMarketWIC//src//main//webapp//images//MaketImages//"+Name+imgNum+".jpg";
               ImageIO.write(bi,"jpg", new File(path));                
               
               cos.putObject(
               "cloud-object-storage-l7-cos-standard-0x1", // the name of the bucket to which the object is being written
               Name+imgNum+".jpg", // the name of the object being written
               new File(path) // the metadata for the object being written
               ); 
               
               ii+=100;  
               }  
               frameGrabber.stop(); 
            	}
            }   
 
        }
        catch(Exception e) {
        	log("denied");
        }


    }

}
