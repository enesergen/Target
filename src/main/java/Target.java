
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
import java.io.*;
import java.math.RoundingMode;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.concurrent.Semaphore;


@XmlRootElement
public class Target implements Serializable {
    private String targetName;
    private double x;
    private double y;
    private double xVelocity;
    private double yVelocity;
    private Semaphore semaphore1;
    private Semaphore semaphore2;

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public Target(String targetName, double x, double y, double xVelocity, double yVelocity) {
        this.targetName = targetName;
        this.x = x;
        this.y = y;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        decimalFormat.setRoundingMode(RoundingMode.UP);
        semaphore1 = new Semaphore(1);
        semaphore2 = new Semaphore(0);

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


    public void sendTarget(String address, int portNumber) {
        boolean isConnected = false;
        Socket socket;
        ObjectInputStream ois=null;
        ObjectOutputStream oos=null;

        while(!isConnected){
            try {
                socket=new Socket(address,portNumber);
                ObjectInputStream ois1=new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos1=new ObjectOutputStream(socket.getOutputStream());
                isConnected=true;
                System.out.println("Connection provided:"+socket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.out.println("Try to connect sensor...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }

    }


    public void updateCoordinates() {
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
            System.out.println("Update Func " + "X:" + x + " Y:" + y);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //semaphore1.release();
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
        //Thread t1 = new Thread(target::updateCoordinates);
        //Thread t2 = new Thread(() -> {
          //  target.sendTarget("ad", 5);
        //});
        //t1.start();
        //t2.start();
        System.out.println(target.getTargetName());
        target.sendTarget("localhost", 8080);

    }
}

