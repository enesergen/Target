import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;

@XmlRootElement
public class Target {
    private String targetName;
    private double x;
    private double y;
    private double xVelocity;
    private double yVelocity;


    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public Target(String targetName, double x, double y, double xVelocity, double yVelocity) {
        this.targetName = targetName;
        this.x = x;
        this.y = y;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        decimalFormat.setRoundingMode(RoundingMode.UP);

    }

    @XmlAttribute
    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @XmlAttribute
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @XmlAttribute
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @XmlAttribute
    public double getxVelocity() {
        return xVelocity;
    }

    public void setxVelocity(double xVelocity) {
        this.xVelocity = xVelocity;
    }

    @XmlAttribute
    public double getyVelocity() {
        return yVelocity;
    }

    public void setyVelocity(double yVelocity) {
        this.yVelocity = yVelocity;
    }


    public synchronized void sendTarget(String address, int portNumber) {
        /*
        boolean isConnected = false;
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        while (!isConnected) {
            try {
                socket = new Socket(address, portNumber);
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
                isConnected = true;
                System.out.println("Connection is provided for IP:" + address + " Port:" + portNumber);

            } catch (IOException e) {
                System.out.println("Try to connect sensor");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            while (true) {
                try {
                    if (isConnected) {
                        oos.writeObject(this);
                        oos.flush();
                        oos.reset();
                        String sensorMessage = (String) ois.readObject();
                        System.out.println(sensorMessage);
                        Thread.sleep(1000);

                    } else {
                        socket = new Socket(address, portNumber);
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        ois = new ObjectInputStream(socket.getInputStream());
                        System.out.println("Connected again to server");
                        isConnected = true;

                    }
                } catch (IOException | ClassNotFoundException e) {
                    isConnected = false;
                    System.out.println("connection is ruptured");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

        }
        */
        while (true) {
            System.out.println("Send Func" + "X:" + x + " Y:" + y);

        }

    }

    public synchronized void updateCoordinates() {
        while (true) {
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
            System.out.println("Update Func" + "X:" + x + " Y:" + y);

            notify();
            try {
                wait();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public double formatAndRoundNumber(double number) {
        return Double.parseDouble(decimalFormat.format(number));
    }

    public static Target readObjectFromXML() {
        Target target = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File("C:\\Users\\stj.eergen\\Desktop\\Target\\src\\main\\resources\\target1.xml"));
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
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    public static void main(String[] args) {

        Target target = readObjectFromXML();
        Thread t1 = new Thread(() -> {
            target.updateCoordinates();
        });
        Thread t2 = new Thread(() -> {
            target.sendTarget("ad", 5);
        });
        t1.start();
        t2.start();

    }
}

