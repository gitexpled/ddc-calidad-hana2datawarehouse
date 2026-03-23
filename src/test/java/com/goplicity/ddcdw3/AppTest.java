package com.goplicity.ddcdw3;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Scanner;

import org.json.JSONObject;

import com.goplicity.ddcdw3.dw.Hana2SqlSv;
import com.goplicity.ddcdw3.dw.SqlSvConn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     * @throws SQLException 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public static Test suite() throws SQLException, UnsupportedEncodingException, IOException
    {
    	
    	/*
    	Hana2SqlSv hna = new Hana2SqlSv();
    	JSONObject res = hna.start();
    	*/
        /*
    	SqlSvConn s = new SqlSvConn();
		s.executeQuery("select * from calidad_granel_carozo where lote='LIM0047-01'");
		s.close();
        */
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
