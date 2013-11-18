package org.server;

/**
 * Created with IntelliJ IDEA.
 * User: Remco
 * Date: 18/11/13
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.util.*;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import javax.vecmath.Point3d;
/**
 * Hello world!
 *
 */
public class xmlParser
{

    public List<Container> parse(String path) throws FileNotFoundException
    {
        List<Container> containerList = new ArrayList<Container>();

        try {
            //Configure the xpath stuff
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(path);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            //Grabs all containers, <record></record>
            XPathExpression recordExpr = xpath.compile("/recordset/record");
            //:TODO: stop presuming dates are in format d/m/j and locations in x/y/z
            //Grab arrival date
            XPathExpression arrivalExpr = xpath.compile("aankomst/datum/*");
            //Grab leave date
            XPathExpression leaveExpr = xpath.compile("vertrek/datum/*");
            //Grab location
            XPathExpression locationExpr = xpath.compile("aankomst/positie/*");
            //Grab name
            XPathExpression nameExpr = xpath.compile("eigenaar/naam");

            //evaluate the expression, this will grab all <record>... </record> nodes
            //instead of having a complex maze of switch/loop/if statements we can just
            //grab everything with a loop and a few more xpath expressions
            NodeList containerNodes = (NodeList)recordExpr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < containerNodes.getLength(); i++) {
                Node n = containerNodes.item(i);
                //////Aankomst code
                //Grab arrival date, (0 1 2) => (d m j)
                NodeList arrivalNodes = (NodeList)arrivalExpr.evaluate(n, XPathConstants.NODESET);
                int d1 = Integer.valueOf(arrivalNodes.item(0).getTextContent());
                int m1 = Integer.valueOf(arrivalNodes.item(1).getTextContent());
                int j1 = Integer.valueOf(arrivalNodes.item(2).getTextContent());
                Point3d arrivalDate = new Point3d(d1, m1, j1);

                //Grab arrival time, (0 1) => (hour:minute)
                //NodeList arrivalTimeNodes = (NodeList)arrivalExpr.evaluate(n, XPathConstants.NODESET);
                //String  = arrivalNodes.item(0).getTextContent();
                //String minute1 = arrivalNodes.item(1).getTextContent();

                //TODO: check if the xml file contains float values, if so, change int -> float
                //Grab location (0 1 2) => (x y z)
                NodeList locationNodes = (NodeList)locationExpr.evaluate(n, XPathConstants.NODESET);
                int x = Integer.valueOf(locationNodes.item(0).getTextContent());
                int y = Integer.valueOf(locationNodes.item(1).getTextContent());
                int z =Integer.valueOf(locationNodes.item(2).getTextContent());
                Point3d location = new Point3d(x, y, z);

                //Grab leave Date (0 1 2) => (d m j)
                NodeList leaveNodes = (NodeList)leaveExpr.evaluate(n, XPathConstants.NODESET);
                int d2 = Integer.valueOf(leaveNodes.item(0).getTextContent());
                int m2 = Integer.valueOf(leaveNodes.item(1).getTextContent());
                int j2 =Integer.valueOf(leaveNodes.item(2).getTextContent());
                Point3d leaveDate = new Point3d(d2, m2, j2);

                //Grab name
                String name = (String)nameExpr.evaluate(n, XPathConstants.STRING);

                //moar here

                containerList.add(new Container(location, arrivalDate, leaveDate, name));
            }
        }
        catch (Exception e) {
            System.out.println("Exception in Server.main: " + e.getMessage());
        }

        return containerList;
    }
}

