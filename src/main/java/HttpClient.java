/**
 * Original from https://github.com/CaffeinaSoftware/pos-erp/blob/master/pos_client/src/mx/caffeina/pos/Http/HttpClient.java
 *
 *
 *
 **/
import java.io.*;
import java.net.*;
import java.util.*;

// A client for our multithreaded EchoServer.
public class HttpClient
{
    /**
    *  The socket for this conection
    * */
    private static Socket s = null;
    private static URL url = null;

    public static StringBuilder Request (String host )
    {
        if (!createSocket(host)){
            // Error while creating host
            return null;
        }
        return doRequest();
    }

    public static void RequestBinToFile(String host, String file) throws Exception
    {
        URL u = new URL(host);
        URLConnection uc = u.openConnection();
        String contentType = uc.getContentType();
        int contentLength = uc.getContentLength();

        if (contentType.startsWith("text/") || contentLength == -1)
        {
            throw new IOException("This is not a binary file.");
        }

        InputStream raw = uc.getInputStream();
        InputStream in = new BufferedInputStream(raw);
        byte[] data = new byte[contentLength];
        int bytesRead = 0;
        int offset = 0;

        while (offset < contentLength)
        {
            bytesRead = in.read(data, offset, data.length - offset);
            if (bytesRead == -1)
                break;

            offset += bytesRead;
        }

        in.close();

        if (offset != contentLength)
        {
            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }

        String filename = file;
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.flush();
        out.close();
    }

    private static boolean createSocket( String host_add )
    {

        try {
            url = new URL(host_add);
        } catch(java.net.MalformedURLException mue) {
            //Logger.error(mue);
            //MalformedURLException
            return false;
        }

        try {
            //create the socket
            if(url.getPort() == -1)
                s = new Socket(url.getHost(), 80);
            else
                s = new Socket(url.getHost(), url.getPort());

        } catch(IllegalArgumentException iae) {
            //Logger.error(iae);
            return false;

        }catch(UnknownHostException uhe){
            // Host unreachable
            //Logger.error(uhe);
            return false;

        }catch(IOException ioe){
            // Cannot connect to port on given host
            //Logger.error(ioe);
            return false;

        }catch(Exception e){
            //Logger.error(e);
            return false;
        }

        return true;
    }

    private static StringBuilder doRequest(  )
    {

        //Logger.log("Doing request...");

        BufferedReader  in          = null;
        PrintWriter     out         = null;
        StringBuilder   response    = new StringBuilder("");

        try{
            // Create the streams to send and receive information
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));


            //send petition
            out.println("GET "+ url.getFile() +" HTTP/1.1");
            out.println("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            out.println("Accept-Charset:ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            /*out.println("Accept-Encoding:gzip,deflate,sdch");*/
            out.println("User-Agent: POS_CLIENT 1.0");
            out.println("Host: "+url.getHost()+"\n\n");

            out.flush();

            // receive the reply.
            String r ;
            boolean headerEnded = false;
            int contentLength = 0;
            String transferEncoding = null;

            while ((r = in.readLine()) != null){
                if(r.startsWith("Content-Length")){
                    contentLength = Integer.parseInt((r.split(":")[1]).trim());
                }

                if(r.startsWith("Content-Type")){
                    //headerEnded = true;
                }

                if(r.startsWith("Set-Cookie")){
                    //save the cookie somewhere
                }

                if(r.startsWith("Transfer-Encoding")){
                    transferEncoding = (r.split(":")[1]).trim();
                }

                if(r.length() == 0){

                    /** ************************************************ **
                            READ THE ACTUAL RESPONSE
                     ** ************************************************ **/

                    // ok, ya termine...
                    if(contentLength == 0){
                        //no me enviaron content-length
                        //leer hasta que el buffer tenga nulo

                        // read chunks
                        if(transferEncoding.equals("chunked"))
                        {
                            int chunk = 0;
                            while( (chunk = Integer.parseInt( in.readLine() , 16) ) != 0 )
                            {
                                    while(--chunk >= -1){
                                        response.append( (char)in.read() );
                                    }

                                    in.readLine();
                            }

                            break;
                        }

                        //read line by line
                        while((r = in.readLine() ) != null)
                        {
                            response.append(r+"\n");
                        }

                    }else{

                        // only read content-length char count
                        //Logger.log("Reading Content-Length, which is ("+contentLength+") bytes");

                        while(--contentLength >= 0)
                        {
                            response.append( (char)in.read() );
                        }
                    }

                    break;
                }
            }

        }catch(IOException ioe){
            //Logger.error("Exception during communication. Server probably closed connection.");
            //Logger.error(ioe);
            System.out.println("Exception during communication. Server probably closed connection.");

        }finally{
            //Logger.log( "Closing http-client buffers...");

            try{
                // Close the streams
                out.close();
                in.close();

                // Close the socket before quitting
                s.close();

            }catch(Exception e){
                e.printStackTrace();
                //Logger.error( e );
            }
        }

        return response;
    }
}

