package utils;

import com.sun.deploy.config.ClientConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class Utility {

    public static boolean save(String filename,Object object){
        try
        {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(object);
            out.close();
            file.close();
            log.info("Object is saved");
        }
        catch(IOException ex)
        {
            log.error("IOException is caught");
            return false;
        }
        return true;
    }

    public static Object load(String filename){
        Object object=null;
        try
        {
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);
            object = in.readObject();
            in.close();
            file.close();
            log.info("Object is loaded");
        }
        catch(IOException ex)
        {
            log.error("Unable to load: IOException is caught");
        }
        catch(ClassNotFoundException ex)
        {
            log.error("Unable to load: ClassNotFoundException is caught");
        }
        return object;
    }
}