/**
 * Forked from https://github.com/CaffeinaSoftware/pos-erp/blob/master/pos_client/src/mx/caffeina/pos/Http/HttpClient.java
 *
 **/
import java.io.*;
import java.net.*;
import java.util.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.io.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class HttpClient {

    public static String Request(String urlString) throws Exception {
        String response = "";

        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String input;
        while ((input = br.readLine()) != null) {
            response += input;
        }
        br.close();

        return response;
    }

    public static void RequestBinToFile(String urlString, String file) throws Exception {
        URL url = new URL(urlString);
        URLConnection uc = url.openConnection();

        String contentType = uc.getContentType();
        int contentLength = uc.getContentLength();

        if (contentType.startsWith("text/") || contentLength == -1) {
            throw new IOException("This is not a binary file.");
        }

        InputStream raw = uc.getInputStream();
        InputStream in = new BufferedInputStream(raw);
        byte[] data = new byte[contentLength];
        int bytesRead = 0;
        int offset = 0;

        while (offset < contentLength) {
            bytesRead = in.read(data, offset, data.length - offset);
            if (bytesRead == -1)
                break;

            offset += bytesRead;
        }

        in.close();

        if (offset != contentLength) {
            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }

        String filename = file;
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.flush();
        out.close();
    }
}

