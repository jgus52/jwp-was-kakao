package utils;

import annotation.Request;
import annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RequestUtils {
    public static Request getRequest(InputStream inputStream){
        BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream));

        try{
            String requestLine = reader.readLine();
            String method = requestLine.split(" ")[0];
            String path = requestLine.split(" ")[1];

            return new Request(RequestMethod.valueOf(method), path);
        }catch(IOException e){

        }
        return null;
    }
}