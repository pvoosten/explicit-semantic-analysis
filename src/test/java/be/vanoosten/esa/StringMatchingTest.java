/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.vanoosten.esa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author user
 */
public class StringMatchingTest {
    
    public StringMatchingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private void assertMatchesNamespaceRegex(String title, boolean shouldMatch){
        String regex = "^[a-zA-z]+:.*";
        Pattern pat = Pattern.compile(regex);
        Matcher matcher = pat.matcher(title);
        assertTrue(String.format("\"%s\" doesn't match /%s/", title, regex), matcher.find()==shouldMatch);        
    }
    
    @Test
    public void hello() {
        assertMatchesNamespaceRegex("Category: alpha beta gamma\n", true);
        assertMatchesNamespaceRegex("Sjabloon:123FooBar", true);
        assertMatchesNamespaceRegex("1234Blabla:koolness", false);
    }
}
