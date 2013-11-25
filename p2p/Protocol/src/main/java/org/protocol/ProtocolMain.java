package org.protocol;

import javax.vecmath.Vector3f;

//Temporary test class
//Run this if you want to check if everything gets (de)serialized properly
// without starting server/client pair or hacking around in either modules
public class ProtocolMain {
    public static void main(String args[]) {

		Vector3f test = new Vector3f(5,5,5);
		Agv test1 = new Agv(test);
		System.out.println(test1.GetLocation());
    	
        ProtocolParser parser = new ProtocolParser();
        Protocol p = new Protocol();
        Protocol p2 = null;
        p.containers.add(new Container());
        String xmlOutput = "";

        try {
            xmlOutput = parser.serialize(p);
            System.out.println(xmlOutput);

            p2 = parser.deserialize(xmlOutput);
            System.out.println(p2.getContainers().get(0).name);

        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println(xmlOutput);
    }
}
