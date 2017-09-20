/*
 * Created by Roman Baum on 17.04.15.
 * Last modified by Roman Baum on 17.04.15.
 */
package mdb.packages;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * The class "MDBDataset" provides one default constructor and one method. With this class you can create a directory
 * for a MDB dataset.
 */
public class MDBDataset {

    // default constructor
    public MDBDataset() {
    }


    public void deleteDatasetDirectory(String directoryPath) {

        /**
         * Remove a directory from the dataset. The directory you want to remove is the word(-complex) at the end of
         * the complete directory path. The name of the directory to removed stand behind the last slash sign.
         * You can use an absolute or a relative path to delete an arbitrary directory. This method will delete
         * all data inside the diretory.
         */

        File fileToDelete = new File(directoryPath);

        try {
            FileUtils.deleteDirectory(fileToDelete);
            System.out.println("The diretory for the dataset was removed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
