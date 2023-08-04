
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.RoundingMode;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Target {
    private String targetName;
    private double x;
    private double y;
    private double xVelocity;
    private double yVelocity;
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static ArrayList<Integer> portList;
    private static String address;

    public Target(String targetName, double x, double y, double xVelocity, double yVelocity) {
        this.targetName = targetName;
        this.x = x;
        this.y = y;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        decimalFormat.setRoundingMode(RoundingMode.UP);
    }


    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getxVelocity() {
        return xVelocity;
    }

    public void setxVelocity(double xVelocity) {
        this.xVelocity = xVelocity;
    }

    public double getyVelocity() {
        return yVelocity;
    }

    public void setyVelocity(double yVelocity) {
        this.yVelocity = yVelocity;
    }


    public void sendTarget(String address, int portNumber) {
        boolean isConnected = false;
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        while (!isConnected) {
            try {
                socket = new Socket(address, portNumber);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                isConnected = true;
                System.out.println("Connection is provided for port:" + portNumber);
            } catch (IOException e) {
                System.out.println("Try to connect sensor");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        //
        while (true) {
            try {
                if (isConnected) {
                    oos.writeObject(targetToString());
                    oos.flush();
                    String msg = (String) ois.readObject();
                    System.out.println(msg + "for Address:" + address + " Port:" + portNumber);
                    Thread.sleep(400);
                } else {
                    socket = new Socket(address, portNumber);
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());
                    isConnected = true;
                    Thread.sleep(400);
                }
            } catch (IOException e) {
                isConnected = false;
                System.out.println("Connection failed. It will try to provide connection");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (InterruptedException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public String targetToString() {
        return targetName + "," + x + "," + y + "," + xVelocity + "," + yVelocity;
    }

    public void updateCoordinates() {
        while (true) {
            synchronized (this) {
                if (x > 500 | x < -500) {
                    xVelocity *= -1;
                }
                if (y > 500 | y < -500) {
                    yVelocity *= -1;
                }
                this.x += xVelocity;
                this.y -= yVelocity;
                this.x = formatAndRoundNumber(x);
                this.y = formatAndRoundNumber(y);
                System.out.println("Update Func " + "X:" + x + " Y:" + y);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public double formatAndRoundNumber(double number) {
        return Double.parseDouble(decimalFormat.format(number));
    }

    public static Target readObjectFromXML(String xmlPath) {
        Target target = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlPath));
            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("Target");

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    target = new Target(element.getElementsByTagName("targetName").item(0).getTextContent(),
                            Double.parseDouble(element.getElementsByTagName("x").item(0).getTextContent()),
                            Double.parseDouble(element.getElementsByTagName("y").item(0).getTextContent()),
                            Double.parseDouble(element.getElementsByTagName("xVelocity").item(0).getTextContent()),
                            Double.parseDouble(element.getElementsByTagName("yVelocity").item(0).getTextContent()));
                    setPortList(element.getElementsByTagName("ports").item(0).getTextContent());
                    address = element.getElementsByTagName("address").item(0).getTextContent();
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
        return target;
    }
    public static ArrayList<Integer> setPortList(String ports) {
        String[] splitPorts = ports.split(",");
        portList = new ArrayList<>();
        for (String port : splitPorts) {
            portList.add(Integer.valueOf(port));
        }
        return portList;
    }

    public static void main(String[] args) {
        
        Target target = readObjectFromXML("C:\\Users\\stj.eergen\\Desktop\\Target\\src\\main\\resources\\target1.xml");
        Thread t1 = new Thread(target::updateCoordinates);
        t1.start();
        for (Integer port : portList) {
            new Thread(() -> {
                target.sendTarget(address, port);
            }).start();
        }
    }
}

