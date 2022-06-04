
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author tompo
 */
public class ContrastStretchingController implements Initializable {

    @FXML
    private Button Apply;
    @FXML
    private GridPane GridPane;
    @FXML
    private LineChart<?, ?> ContrastStretchingGraph;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;
    private XYChart.Series series = new XYChart.Series();
    @FXML
    private Button ClearGraph;
    private int numberOfPointsSelected = 0;
    private double r1;
    private double r2;
    private double s1;
    private double s2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        series.getData().add(new XYChart.Data(0, 0));
        series.getData().add(new XYChart.Data(255, 255));
        ContrastStretchingGraph.getData().add(series);

    }

    @FXML
    private void ApplyClicked(ActionEvent event) {
        System.out.println("points selected saved");

        GlobalVariables.setR1(r1);
        GlobalVariables.setR2(r2);
        GlobalVariables.setS1(s1);
        GlobalVariables.setS2(s2);
        
            Stage stage = (Stage) Apply.getScene().getWindow();
 stage.close();

    }

    @FXML
    private void GraphClicked(MouseEvent event) {

        if (numberOfPointsSelected >= 2) {
            System.out.println("Two points have already been selected, clear graph to select new points");
        } else {
            System.out.println("Point selected");
            int xCord = (int) event.getX();
            int yCord = (int) event.getY();
            final int XOFFSET = 45;
            final int YOFFSET = 15;

            final double XCORDTOGRAPHRATIO = 2.1176;
            final double YCORDTOGRAPHRATIO = 2.1373;

            //test values **Redundant
            // System.out.println("X axis: " + xCord);
            //System.out.println("Y axis: " + yCord);
            System.out.println("X value is: " + (int) ((xCord - XOFFSET) / XCORDTOGRAPHRATIO));
            System.out.println("Y value is: " + (int) (255 - (yCord - YOFFSET) / YCORDTOGRAPHRATIO));

            int selectedXValue = (int) ((xCord - XOFFSET) / XCORDTOGRAPHRATIO);
            int selectedYValue = (int) (255 - ((yCord - YOFFSET) / YCORDTOGRAPHRATIO));
            series.getData().add(new XYChart.Data(selectedXValue, selectedYValue));
            ContrastStretchingGraph.getData().add(series);

            if (numberOfPointsSelected == 0) {
                r1 = (double) selectedXValue;
                s1 = (double) selectedYValue;
            } else if (numberOfPointsSelected == 1) {
                r2 = (double) selectedXValue;
                s2 = (double) selectedYValue;

            }
            numberOfPointsSelected++;
        }

    }

    @FXML
    private void ClearGraphClicked(ActionEvent event) {
        numberOfPointsSelected = 0;
        System.out.println("Point selection cleared");
        ContrastStretchingGraph.getData().clear();
        series.getData().clear();
        series.getData().add(new XYChart.Data(0, 0));
        series.getData().add(new XYChart.Data(255, 255));

    }

}
