import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;


// Datenbankzugriff mit Anzeige der Daten in einer GUI (SWING)

public class SQLConn3 {

    static final String SQL_URL = "jdbc:mysql://127.0.0.1:3306/plugin2";
    static final String USER = "daten";
    static final String PASS = "daten";

    static Connection connection = null;
    static Statement statement = null;

    // Funktion zur Überprüfung, ob der Name existiert
    public static boolean isNameExisting(Statement statement, String name) throws SQLException {
        String query = "SELECT Name FROM users2 WHERE Name = '" + name + "';";
        ResultSet resultSet = statement.executeQuery(query);
        boolean exists = resultSet.next(); // Wenn es einen Treffer gibt, existiert der Name
        resultSet.close();
        return exists;
    }

    // Funktion zum Laden der Daten aus der Datenbank
    public static void loadData(DefaultTableModel tableModel) {
        try {
            ResultSet resultset = statement.executeQuery("SELECT * FROM users2");
            tableModel.setRowCount(0); // Tabelle zurücksetzen

            while (resultset.next()) {
                int id = resultset.getInt("ID");
                String username = resultset.getString("Name");
                String password = resultset.getString("Password");
                tableModel.addRow(new Object[]{id, username, password});
            }
            resultset.close(); // Ergebnis-Set schließen
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Funktion zum Löschen eines Benutzers
    public static void deleteUser(Statement statement, String name) throws SQLException {
        String deleteSql = "DELETE FROM users2 WHERE Name = '" + name + "';";
        statement.executeUpdate(deleteSql);
    }

    public static void main(String[] args) {
        // GUI erstellen
        JFrame frame = new JFrame("SQL User Registration");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Eingabefelder
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        JLabel nameLabel = new JLabel("New Name:");
        JTextField nameField = new JTextField();
        JLabel passwordLabel = new JLabel("New Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton submitButton = new JButton("Submit");
        JButton deleteButton = new JButton("Delete"); // Delete Button
        JButton endButton = new JButton("End"); // Ende Button

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(submitButton);
        inputPanel.add(deleteButton); // Delete Button hinzufügen
        inputPanel.add(endButton); // Ende Button hinzufügen

        // JTable zur Anzeige der Daten
        String[] columnNames = {"ID", "Username", "Password"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER); // JTable hinzufügen

        // SQL-Verbindung vorbereiten
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connecting to Database MySQL...");
            connection = DriverManager.getConnection(SQL_URL, USER, PASS);
            System.out.println("Connection successful....");
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Daten laden
        loadData(tableModel);

        // Button Aktion für Submit hinzufügen
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String password = new String(passwordField.getPassword());

                try {
                    if (!isNameExisting(statement, name)) {
                        // Name existiert nicht, also den neuen Benutzer einfügen
                        String insertSql = "INSERT INTO users2(Name, Password) VALUES ('" + name + "', '" + password + "');";
                        statement.executeUpdate(insertSql);
                        JOptionPane.showMessageDialog(frame, "New Name inserted: " + name);
                        loadData(tableModel); // Daten nach dem Einfügen neu laden
                    } else {
                        // Name bereits vorhanden
                        JOptionPane.showMessageDialog(frame, "Name already exists: " + name + " \nTry again please");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Button Aktion für Delete hinzufügen
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow(); // Ausgewählte Zeile abrufen
                if (selectedRow != -1) {
                    String name = table.getValueAt(selectedRow, 1).toString(); // Den Namen aus der Tabelle abrufen
                    try {
                        deleteUser(statement, name); // Benutzer löschen
                        JOptionPane.showMessageDialog(frame, "User deleted: " + name);
                        loadData(tableModel); // Tabelle aktualisieren
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a user to delete.");
                }
            }
        });

        // Button Aktion für Ende hinzufügen
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Aktuelle Datenbankeinträge in der Konsole ausgeben
                loadData(tableModel); // Aktuelle Daten in der Tabelle anzeigen

                // Programm beenden
                try {
                    if (connection != null) {
                        connection.close();
                        System.out.println("\nConnection closed");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                System.exit(0); // Programm beenden
            }
        });

        // GUI sichtbar machen
        frame.setVisible(true);

        // Bei Programmende Verbindung schließen (nur für die Sicherheit)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("\nConnection closed");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
