package parcial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



public class AppPersonas {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField correo;
    private JTextField nombre;
    private JTextField id;

    public static void main(String[] args) {
        AppPersonas app = new AppPersonas();
    }

    public AppPersonas() {
        inicializarPantalla();
    }

    private void inicializarPantalla() {
        frame = new JFrame("AppPersonas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel formulario = new JPanel(new GridLayout(4, 2));
        formulario.add(new JLabel("Identificación:"));
        id = new JTextField();
        formulario.add(id);
        formulario.add(new JLabel("Nombre:"));
        nombre = new JTextField();
        formulario.add(nombre);
        formulario.add(new JLabel("Correo:"));
        correo = new JTextField();
        formulario.add(correo);
        JButton agregarPersona = new JButton("Agregar");
        formulario.add(agregarPersona);

        JPanel botonesPanel = new JPanel(new GridLayout(1, 3));

        JPanel botonesArchivoPlano = new JPanel(new GridLayout(2, 1));
        botonesArchivoPlano.add(createButton("Guardar Archivo Plano", this::guardarArchivoPlano));
        botonesArchivoPlano.add(createButton("Leer Archivo Plano", this::cargarArchivoPlano));

        JPanel botonesXML = new JPanel(new GridLayout(2, 1));
        botonesXML.add(createButton("Guardar Archivo XML", this::guardarXML));
        botonesXML.add(createButton("Leer Archvo XML", this::cargarXML));

        JPanel botonesJSON = new JPanel(new GridLayout(2, 1));
        botonesJSON.add(createButton("Guardar archivo JSON", this::guardarJSON));
        botonesJSON.add(createButton("Leer Archivo JSON", this::cargarJSON));

        botonesPanel.add(botonesArchivoPlano);
        botonesPanel.add(botonesXML);
        botonesPanel.add(botonesJSON);

        String[] columnNames = {"Identificación", "Nombre", "Correo"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        agregarPersona.addActionListener(e -> agregarPersona());

        frame.add(formulario, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(botonesPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }
    
    private Element createElement(Document doc, String name, String value) {
        Element element = doc.createElement(name);
        element.appendChild(doc.createTextNode(value));
        return element;
    }

    private void agregarPersona() {
        try {
            String idPersona = id.getText();
            String nombrePersona = nombre.getText();
            String correoPersona = correo.getText();

            if (!Pattern.matches(".+@.+\\..+", correoPersona)) {
                throw new correoException("Correo no válido: " + correoPersona);
            }

            tableModel.addRow(new String[]{idPersona, nombrePersona, correoPersona});
            id.setText("");
            nombre.setText("");
            correo.setText("");
        } catch (correoException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarArchivoPlano() {
        try (PrintWriter writer = new PrintWriter("personas.txt")) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(String.join(",", getRowData(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarArchivoPlano() {
        try (Scanner scanner = new Scanner(new File("personas.txt"))) {
            tableModel.setRowCount(0);
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                tableModel.addRow(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("Personas");
            doc.appendChild(root);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Element person = doc.createElement("Persona");
                root.appendChild(person);

                String[] data = getRowData(i);
                person.appendChild(createElement(doc, "Identificacion", data[0]));
                person.appendChild(createElement(doc, "Nombre", data[1]));
                person.appendChild(createElement(doc, "Correo", data[2]));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("personas.xml"));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File("personas.xml"));

            tableModel.setRowCount(0);
            NodeList nodeList = doc.getElementsByTagName("Persona");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getElementsByTagName("Identificacion").item(0).getTextContent();
                    String name = element.getElementsByTagName("Nombre").item(0).getTextContent();
                    String email = element.getElementsByTagName("Correo").item(0).getTextContent();
                    tableModel.addRow(new String[]{id, name, email});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarJSON() {
        try (Writer writer = new FileWriter("personas.json")) {
            java.util.List<Map<String, String>> data = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String[] row = getRowData(i);
                Map<String, String> map = new HashMap<>();
                map.put("Identificacion", row[0]);
                map.put("Nombre", row[1]);
                map.put("Correo", row[2]);
                data.add(map);
            }
            new Gson().toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarJSON() {
        try (Reader reader = new FileReader("personas.json")) {
            java.util.List<Map<String, String>> data = new Gson().fromJson(reader, new TypeToken<java.util.List<Map<String, String>>>(){}.getType());
            tableModel.setRowCount(0);
            for (Map<String, String> map : data) {
                tableModel.addRow(new String[]{map.get("Identificacion"), map.get("Nombre"), map.get("Correo")});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] getRowData(int row) {
        int colCount = tableModel.getColumnCount();
        String[] cuenta = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            cuenta[i] = (String) tableModel.getValueAt(row, i);
        }
        return cuenta;
    }
}

class correoException extends Exception {
    public correoException(String message) {
        super(message);
    }
}
