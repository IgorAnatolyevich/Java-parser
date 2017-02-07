import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleDriver;
import oracle.sql.CLOB;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: litovkin-i
 * Date: 13.12.16
 * Time: 18:04
 */

public class Main{

    public static void main(String[] args) throws SQLException, ParserConfigurationException, SAXException {
        try {
            File file = new File("C:\\path_to_xml_file");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

			// interesting tags
                String data_1 = "DATA_1";
                String data_2 = "DATA_2";
                String data_3 = "DATA_3";
                
                boolean isData_1 = false;
                boolean isData_2 = false;
                boolean isData_3 = false;
                
                HashMap<String, String> strings = new HashMap<String, String>();

                public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {	
                    if (qName.equalsIgnoreCase(data_1)) isData_1 = true;
                    if (qName.equalsIgnoreCase(data_2)) isData_2 = true;
                    if (qName.equalsIgnoreCase(data_3)) isData_3 = true;
                  }

                public void characters(char ch[], int start, int length) throws SAXException {
                    if (isData_1) {
                        strings.put(data_1, new String(ch, start, length));
                        isData_1 = false;
                    }
                    if (isData_2) {
                        strings.put(data_2, new String(ch, start, length));
                        isData_2 = false;
                    }
			    if (isData_3) {
                        strings.put(data_3, new String(ch, start, length));
                        isData_3 = false;
                    }

                   }

                public void endDocument(){
                    try {
                        DriverManager.registerDriver(new OracleDriver());
                        String url = "jdbc:oracle:thin:@driver";
                        String login = "login";
                        String password = "pass";

                        Connection conn = DriverManager.getConnection(url, login, password);
                        OracleCallableStatement cs = (OracleCallableStatement) conn.prepareCall("{call WRITE_EVENT_TEST (?, ?, ?)}");

                        cs.setString(1, strings.get(data_1));
                        cs.setString(2,  strings.get(data_2));
                        CLOB clob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);
                        clob.open(CLOB.MODE_READWRITE);
                        clob.setString(1, strings.get(data_3));
                        cs.setClob(3, clob);

                        cs.execute(); // was configure db with 'alter table TABLE_NAME enable row movement'

                        clob.free();
                        cs.close();
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            saxParser.parse(file, handler);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
