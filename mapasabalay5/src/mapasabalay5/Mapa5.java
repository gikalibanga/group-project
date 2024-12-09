
package mapasabalay5;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Mapa5 {
    private static final String API_KEY = "72497962eb6848d18c671c75eff2dd22"; // Replace with your OpenCage API Key

    public static void main(String[] args) {
        SwingUtilities.invokeLater( Mapa5::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Create the main frame
        JFrame frame = new JFrame("Route Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Panel for input fields
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel fromCityLabel = new JLabel("From City:");
        JTextField fromCityField = new JTextField();
        JLabel toCityLabel = new JLabel("To City:");
        JTextField toCityField = new JTextField();

        inputPanel.add(fromCityLabel);
        inputPanel.add(fromCityField);
        inputPanel.add(toCityLabel);
        inputPanel.add(toCityField);

        // Button to submit and redirect
        JButton submitButton = new JButton("View Route on OpenStreetMap");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String fromCity = fromCityField.getText().trim();
                    String toCity = toCityField.getText().trim();

                    if (fromCity.isEmpty() || toCity.isEmpty()) {
                        throw new IllegalArgumentException("City names cannot be empty.");
                    }

                    // Get coordinates for the cities
                    double[] fromCoords = getCoordinates(fromCity);
                    double[] toCoords = getCoordinates(toCity);

                    // Open OpenStreetMap with directions
                    String url = String.format(
                        "https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=%f,%f;%f,%f",
                        fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]
                    );
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Failed to fetch coordinates or open the URL.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // Add components to the frame
        frame.add(new JLabel("Enter city names for the route:", SwingConstants.CENTER), BorderLayout.NORTH);
        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(submitButton, BorderLayout.SOUTH);

        // Display the frame
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }

    private static double[] getCoordinates(String cityName) throws Exception {
        // Call OpenCage API to get coordinates
        String apiUrl = String.format(
            "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s", 
            cityName.replace(" ", "%20"), 
            API_KEY
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(apiUrl))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        if (json.get("results").getAsJsonArray().size() == 0) {
            throw new IllegalArgumentException("City not found: " + cityName);
        }

        JsonObject location = json.get("results").getAsJsonArray()
            .get(0).getAsJsonObject()
            .get("geometry").getAsJsonObject();

        double lat = location.get("lat").getAsDouble();
        double lon = location.get("lng").getAsDouble();

        return new double[]{lat, lon};
    }
}
