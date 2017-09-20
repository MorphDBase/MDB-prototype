package mdb.testng.packages;

import mdb.packages.MDBDataset;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

public class MDBDataset_Test {

    private String outputPath = "out/test_output";

    @BeforeMethod
    public void mDBDataset_Test_setUp() throws Exception {

        /**
         * create a test data directory
         */
        File dir = new File(this.outputPath);
        dir.mkdir();

    }

    @Test
    public void deleteDatasetDirectory_DeleteDirectory_CheckIfDirectoryExist() throws Exception {

        MDBDataset mdbDataset = new MDBDataset();

        // delete the data set directory
        mdbDataset.deleteDatasetDirectory(this.outputPath);

        // check if the directory was deleted
        Assert.assertFalse(new File(this.outputPath).exists() , "Directory exist.");

    }
}