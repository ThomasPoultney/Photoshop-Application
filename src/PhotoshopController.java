/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import static javafx.collections.FXCollections.fill;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author tompo
 */
public class PhotoshopController implements Initializable {

    private Label label;
    @FXML
    private Label titleLabel;
    @FXML
    private Button invertButton;
    @FXML
    private Slider GammaCorrectionSlider;
    @FXML
    private Button contrastStretchinggButton;
    @FXML
    private Button CrossCorrelationButton;
    @FXML
    private ImageView imageView;
    private static double a = 1.0;

    private double r1 = 0.0;
    private double r2 = 0.0;
    private double s1 = 0.0;
    private double s2 = 0.0;
    int[] CF;

    @FXML
    private Button applyButton;
    @FXML
    private Button greyScale;
    @FXML
    private LineChart<?, ?> histogramGraph;
    private XYChart.Series redSeries = new XYChart.Series();
    private XYChart.Series greenSeries = new XYChart.Series();
    private XYChart.Series blueSeries = new XYChart.Series();
    private XYChart.Series brightnessSeries = new XYChart.Series();
    private XYChart.Series CFSeries = new XYChart.Series();
    private boolean showRedHistogram = true;
    private boolean showGreenHistogram = true;
    private boolean showBlueHistogram = true;
    private boolean showBrightnessHistogram = true;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private RadioButton redRB;
    @FXML
    private RadioButton greenRB;
    @FXML
    private RadioButton blueRB;
    @FXML
    private Button ShowhistogramButton;
    @FXML
    private Button equaliseHistogramButton;
    @FXML
    private RadioButton brightnessRB;
    @FXML
    private Button equaliseHistogramButton1;
    @FXML
    private RadioButton CFRB;
    private boolean isGrey;
    private int minValue;
    private int maxValue;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        redSeries.setName("Red");
        greenSeries.setName("Green");
        blueSeries.setName("Blue");
        brightnessSeries.setName("Brightness");
        CFSeries.setName("CF");

    }

    @FXML
    private void invertButtonAction(ActionEvent event) {
        System.out.println("Invert");
        //At this point, "image" will be the original image
        //imageView is the graphical representation of an image
        //imageView.getImage() is the currently displayed image

        //Let's invert the currently displayed image by calling the invert function later in the code
        Image inverted_image = ImageInverter(imageView.getImage());
        //Update the GUI so the new image is displayed
        imageView.setImage(inverted_image);
    }

    @FXML
    private void ContrastButtonClicked(ActionEvent event) throws IOException {
        //loads new form above current form to select points via a graph
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ContrastStretching.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    void SliderAction(MouseEvent event) {
        System.out.println("Slider Value is: " + GammaCorrectionSlider.getValue());
        System.out.println("Gamma Correcting");
        //At this point, "image" will be the original image
        //imageView is the graphical representation of an image
        //imageView.getImage() is the currently displayed image

        //Let's invert the currently displayed image by calling the invert function later in the code
        Image gammaCorrected_image = ImageGammaCorrector(imageView.getImage());
        //Update the GUI so the new image is displayed
        imageView.setImage(gammaCorrected_image);

    }

    public Image ImageInverter(Image image) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        //Create a new image of that width and height
        WritableImage inverted_image = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter inverted_image_writer = inverted_image.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();

        //Iterate over all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                //Do something (in this case invert) - the getColor function returns colours as 0..1 doubles (we could multiply by 255 if we want 0-255 colours)
                color = Color.color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue());
                //Note: for gamma correction you may not need the divide by 255 since getColor already returns 0-1, nor may you need multiply by 255 since the Color.color function consumes 0-1 doubles.

                //Apply the new colour
                inverted_image_writer.setColor(x, y, color);
            }
        }
        return inverted_image;
    }

    public Image ImageGammaCorrector(Image image) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        //Create a new image of that width and height
        WritableImage gammaCorrectedImage = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter gammaCorrected_image_writer = gammaCorrectedImage.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();
        double[] gammaValues = generateLookUpTableForGamma();
        //Iterate over all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                //applies gamma equation to each color channel

                //color = Color.color(gammaEquation(color.getRed()), gammaEquation(color.getGreen()), gammaEquation(color.getBlue()));
                color = Color.color(gammaValues[(int) (color.getRed() * 255)], gammaValues[(int) (color.getGreen() * 255)], gammaValues[(int) (color.getBlue() * 255)]);

                //Apply the new colour
                gammaCorrected_image_writer.setColor(x, y, color);
            }
        }
        return gammaCorrectedImage;
    }

    public double gammaEquation(double colorValue) {
        double V;
        double I = colorValue;
        double gamma = GammaCorrectionSlider.getValue();
        V = Math.pow(a * I, gamma);
        // v = math.pow(I/a,1/gamma)
        return V;
    }

    public double[] generateLookUpTableForGamma() {
        double[] gammaValues = new double[256];
        for (int i = 0; i <= 255; i++) {
            gammaValues[i] = gammaEquation(((double) i / 255));
            System.out.println(i + "     " + gammaValues[i]);
        }
        return gammaValues;
    }

    public Image imageContrastStretcher(Image image) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        //Create a new image of that width and height
        WritableImage contrastStretchedImage = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter contrastStretched_image_writer = contrastStretchedImage.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();
        double contrastValues[] = generateLookUpTableForContrastStretching();

        //Iterate over all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = image_reader.getColor(x, y);
                color = Color.color(
                        contrastValues[(int) (color.getRed() * 255.0)],
                        contrastValues[(int) (color.getGreen() * 255.0)],
                        contrastValues[(int) (color.getBlue() * 255.0)]);

                contrastStretched_image_writer.setColor(x, y, color);
            }
        }
        return contrastStretchedImage;
    }

    public double[] generateLookUpTableForContrastStretching() {
        double[] contrastValues = new double[256];
        for (int i = 0; i <= 255; i++) {
            contrastValues[i] = contrastStretchingAlgorithm(i / 255.0);
        }
        return contrastValues;
    }

    public double contrastStretchingAlgorithm(double colorValue) {
        double newColorValue = 0; //out
        colorValue = (colorValue * 255.0);

        if (colorValue < r1) {
            newColorValue = (s1 / r1) * colorValue;
        } else if (colorValue >= r1 && colorValue <= r2) {
            newColorValue = (s2 - s1) / (r2 - r1) * (colorValue - r1) + s1;
        } else if (colorValue >= r2) {
            newColorValue = (255 - s2) / (255 - r2) * (colorValue - r2) + s2;
        }
        return newColorValue / 255.0;

    }

    @FXML
    private void applyButtonClicked(ActionEvent event) {
        System.out.println("Contrast Stretching");
        r1 = GlobalVariables.getR1();
        r2 = GlobalVariables.getR2();
        s1 = GlobalVariables.getS1();
        s2 = GlobalVariables.getS2();
        System.out.println(GlobalVariables.getR1());
        System.out.println("R1 is " + r1);
        System.out.println("s1 is " + s1);
        System.out.println("R2 is " + r2);
        System.out.println("s2 is " + s2);

        Image contrastStretched_image = imageContrastStretcher(imageView.getImage());
        //Update the GUI so the new image is displayed
        imageView.setImage(contrastStretched_image);
    }

    @FXML
    private void greyScaleButtonClicked(ActionEvent event) {
        System.out.println("Grey Scale applied");
        isGrey = true;
        Image greyScaleImage = greyScaleAlgoritm(imageView.getImage());
        imageView.setImage(greyScaleImage);
    }

    private Image greyScaleAlgoritm(Image image) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        //Create a new image of that width and height
        WritableImage greyScaleImage = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter greyScale_image_writer = greyScaleImage.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();

        //Iterate over all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = image_reader.getColor(x, y);
                double greyValue = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                color = Color.color(greyValue, greyValue, greyValue);

                greyScale_image_writer.setColor(x, y, color);
            }
        }
        return greyScaleImage;
    }

    @FXML
    private void redRBClicked(ActionEvent event) {
        resetHistograms();
    }

    @FXML
    private void greenRBClicked(ActionEvent event) {
        resetHistograms();
    }

    @FXML
    private void blueRBClicked(ActionEvent event) {
        resetHistograms();
    }

    @FXML
    private void brightnessRBClicked(ActionEvent event) {
        resetHistograms();
    }

    @FXML
    private void CFRBClicked(ActionEvent event) {
        resetHistograms();
    }

    public void resetHistograms() {
        histogramGraph.getData().removeAll(blueSeries, redSeries, greenSeries, brightnessSeries, CFSeries);

        if (brightnessRB.isSelected() == true) {
            histogramGraph.getData().add(brightnessSeries);
        }

        if (redRB.isSelected() == true) {
            histogramGraph.getData().add(redSeries);
        }

        if (greenRB.isSelected() == true) {
            histogramGraph.getData().add(greenSeries);
        }
        if (blueRB.isSelected() == true) {
            histogramGraph.getData().add(blueSeries);
        }

        if (CFRB.isSelected() == true) {
            histogramGraph.getData().add(CFSeries);
        }
    }

    @FXML
    private void HistogramButtonClicked(ActionEvent event) {

        System.out.println("histograms created!");
        generateHistograms();

    }

    private void generateHistograms() {
        Image image = imageView.getImage();
        int[][] histogram;
        histogram = new int[256][4];

        CF = new int[256];
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage histogramImage = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter histogramImage_writer = histogramImage.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                int redValue = (int) (color.getRed() * 255);
                int blueValue = (int) (color.getBlue() * 255);
                int greenValue = (int) (color.getGreen() * 255);
                int brightness = ((redValue + greenValue + blueValue) / 3);

                histogram[brightness][3]++;
                histogram[redValue][2]++;
                histogram[greenValue][1]++;
                histogram[blueValue][0]++;

            }
        }

        CF[0] = histogram[0][3];
        for (int i = 1; i < 256; i++) {
            CF[i] = CF[i - 1] + histogram[i][3];
        }
        CFSeries.getData().clear();
        brightnessSeries.getData().clear();
        redSeries.getData().clear();
        blueSeries.getData().clear();
        greenSeries.getData().clear();

        for (int i = 0; i < 256; i++) {
            //Test Code            
            //System.out.println("CF value " + i + " is: " + CF[i]);
            //System.out.println("BR value " + i + " is: " + histogram[i][3]);
            CFSeries.getData().add(new XYChart.Data(i, CF[i]));
            brightnessSeries.getData().add(new XYChart.Data(i, histogram[i][3]));
            redSeries.getData().add(new XYChart.Data(i, histogram[i][2]));
            greenSeries.getData().add(new XYChart.Data(i, histogram[i][1]));
            blueSeries.getData().add(new XYChart.Data(i, histogram[i][0]));

        }

        resetHistograms();
        //Test Code
        //for (int i = 0; i < 256; i++) {
        //    System.out.println(i + " r=" + histogram[i][2]
        //            + " g=" + histogram[i][1]
        //            + " b=" + histogram[i][0]);
        //}
    }

    @FXML
    private void equaliseHistogramButtonClicked(ActionEvent event) {
        if (isGrey == true) {
            System.out.println("histogram Equalisation Applied");
            //makes sure histograms are up-to-date(especially CF values)
            generateHistograms();
            resetHistograms();
            int equalisedValues[];
            equalisedValues = new int[256];
            double size = imageView.getImage().getHeight() * imageView.getImage().getWidth();
            for (int i = 0; i < 256; i++) {
                equalisedValues[i] = (int) (255.0 * (CF[i] / size));

            }

            //Loop through the image as usual. Get the value from the appropriate colour
            //channel as usual and find its mapping to new intensity:
            Image image = imageView.getImage();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            WritableImage histogramImage = new WritableImage(width, height);
            //Get an interface to write to that image memory
            PixelWriter histogramImage_writer = histogramImage.getPixelWriter();
            //Get an interface to read from the original image passed as the parameter to the function
            PixelReader image_reader = image.getPixelReader();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = image_reader.getColor(x, y);
                    int oldIntensity = (int) (((color.getRed() + color.getGreen() + color.getBlue()) / 3) * 255);
                    double newIntensity = equalisedValues[oldIntensity] / 255.0;
                    color = Color.color(newIntensity, newIntensity, newIntensity);
                    histogramImage_writer.setColor(x, y, color);

                }

            }
            imageView.setImage(histogramImage);

        } else {
            System.out.println("Please turn image greyscale first");
        }

    }

    @FXML
    private void CrossCorrelationButtonClicked(ActionEvent event) {
        System.out.println("Cross Correlation applied");
        int laplacianMatrix[][];
        laplacianMatrix = new int[5][5];

        //row 1
        laplacianMatrix[0][0] = -4;
        laplacianMatrix[0][1] = -1;
        laplacianMatrix[0][2] = 0;
        laplacianMatrix[0][3] = -1;
        laplacianMatrix[0][4] = -4;

        //row 2
        laplacianMatrix[1][0] = -1;
        laplacianMatrix[1][1] = 2;
        laplacianMatrix[1][2] = 3;
        laplacianMatrix[1][3] = 2;
        laplacianMatrix[1][4] = -1;

        //row 3
        laplacianMatrix[2][0] = 0;
        laplacianMatrix[2][1] = 3;
        laplacianMatrix[2][2] = 4;
        laplacianMatrix[2][3] = 3;
        laplacianMatrix[2][4] = 0;

        //row 4
        laplacianMatrix[3][0] = -1;
        laplacianMatrix[3][1] = 2;
        laplacianMatrix[3][2] = 3;
        laplacianMatrix[3][3] = 2;
        laplacianMatrix[3][4] = -1;

        //row 5
        laplacianMatrix[4][0] = -4;
        laplacianMatrix[4][1] = -1;
        laplacianMatrix[4][2] = 0;
        laplacianMatrix[4][3] = -1;
        laplacianMatrix[4][4] = -4;

        Image image = imageView.getImage();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage CCImage = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter CCImage_writer = CCImage.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();
        int sumProductForImage[][][];
        sumProductForImage = new int[width][height][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //kernel cannot be centred so color of edge is set to black
                Color color = image_reader.getColor(x, y);
                if (kernelCanBeCentred(x, y) == false) {

                    color = Color.color(0.0, 0.0, 0.0);
                    CCImage_writer.setColor(x, y, color);
                } else {

                    int redMatrix[][] = generateRedMatrix(x, y);
                    int greenMatrix[][] = generateGreenMatrix(x, y);
                    int blueMatrix[][] = generateBlueMatrix(x, y);

                    sumProductForImage[x][y][0] = calculateSumProduct(redMatrix, laplacianMatrix);
                    sumProductForImage[x][y][1] = calculateSumProduct(greenMatrix, laplacianMatrix);
                    sumProductForImage[x][y][2] = calculateSumProduct(blueMatrix, laplacianMatrix);
                    System.out.println(minValue + " " + maxValue);

                    int normalisedRedValue = normalise(sumProductForImage[x][y][0]);
                    int normalisedGreenValue = normalise(sumProductForImage[x][y][0]);
                    int normalisedBlueValue = normalise(sumProductForImage[x][y][0]);
                   color = Color.color((double)normalisedRedValue/255.0, (double)normalisedGreenValue/255.0, (double)normalisedBlueValue/255.0);
                   
                    CCImage_writer.setColor(x, y, color);

                }

            }
        }

        imageView.setImage(CCImage);
    }

    private boolean kernelCanBeCentred(int x, int y) {
        int width = (int) imageView.getImage().getWidth();
        int height = (int) imageView.getImage().getHeight();
        if ((x >= (width - 2) || x <= 2) || (y >= (height - 2) || y <= 2)) {
            return false;
        } else {
            return true;
        }

    }

    private int[][] generateRedMatrix(int x, int y) {

        int redMatrix[][];
        redMatrix = new int[5][5];
        Image image = imageView.getImage();
        PixelReader image_reader = image.getPixelReader();

        Color color = image_reader.getColor(x, y);
        //row 1
        redMatrix[0][0] = (int) (image_reader.getColor(x - 2, y - 2).getRed() * 255.0);
        redMatrix[0][1] = (int) (image_reader.getColor(x - 1, y - 2).getRed() * 255.0);
        redMatrix[0][2] = (int) (image_reader.getColor(x, y - 2).getRed() * 255.0);
        redMatrix[0][3] = (int) (image_reader.getColor(x + 1, y - 2).getRed() * 255.0);
        redMatrix[0][4] = (int) (image_reader.getColor(x + 2, y - 2).getRed() * 255.0);

        //row 2
        redMatrix[1][0] = (int) (image_reader.getColor(x - 2, y - 1).getRed() * 255.0);
        redMatrix[1][1] = (int) (image_reader.getColor(x - 1, y - 1).getRed() * 255.0);
        redMatrix[1][2] = (int) (image_reader.getColor(x, y - 1).getRed() * 255.0);;
        redMatrix[1][3] = (int) (image_reader.getColor(x + 1, y - 1).getRed() * 255.0);;
        redMatrix[1][4] = (int) (image_reader.getColor(x + 2, y - 1).getRed() * 255.0);;

        //row 3
        redMatrix[2][0] = (int) (image_reader.getColor(x - 2, y).getRed() * 255.0);
        redMatrix[2][1] = (int) (image_reader.getColor(x - 1, y).getRed() * 255.0);
        redMatrix[2][2] = (int) (image_reader.getColor(x, y).getRed() * 255.0);
        redMatrix[2][3] = (int) (image_reader.getColor(x + 1, y).getRed() * 255.0);
        redMatrix[2][4] = (int) (image_reader.getColor(x + 2, y).getRed() * 255.0);

        //row 4
        redMatrix[3][0] = (int) (image_reader.getColor(x - 2, y + 1).getRed() * 255.0);
        redMatrix[3][1] = (int) (image_reader.getColor(x - 1, y + 1).getRed() * 255.0);
        redMatrix[3][2] = (int) (image_reader.getColor(x, y + 1).getRed() * 255.0);
        redMatrix[3][3] = (int) (image_reader.getColor(x + 1, y + 1).getRed() * 255.0);
        redMatrix[3][4] = (int) (image_reader.getColor(x + 2, y + 1).getRed() * 255.0);

        //row 5
        redMatrix[4][0] = (int) (image_reader.getColor(x - 2, y + 2).getRed() * 255.0);
        redMatrix[4][1] = (int) (image_reader.getColor(x - 1, y + 2).getRed() * 255.0);
        redMatrix[4][2] = (int) (image_reader.getColor(x, y + 2).getRed() * 255.0);
        redMatrix[4][3] = (int) (image_reader.getColor(x + 1, y + 2).getRed() * 255.0);
        redMatrix[4][4] = (int) (image_reader.getColor(x + 2, y + 2).getRed() * 255.0);
        return redMatrix;

    }

    private int[][] generateGreenMatrix(int x, int y) {

        int GreenMatrix[][];
        GreenMatrix = new int[5][5];
        Image image = imageView.getImage();
        PixelReader image_reader = image.getPixelReader();

        Color color = image_reader.getColor(x, y);
        //row 1
        GreenMatrix[0][0] = (int) (image_reader.getColor(x - 2, y - 2).getRed() * 255.0);
        GreenMatrix[0][1] = (int) (image_reader.getColor(x - 1, y - 2).getRed() * 255.0);
        GreenMatrix[0][2] = (int) (image_reader.getColor(x, y - 2).getRed() * 255.0);
        GreenMatrix[0][3] = (int) (image_reader.getColor(x + 1, y - 2).getRed() * 255.0);
        GreenMatrix[0][4] = (int) (image_reader.getColor(x + 2, y - 2).getRed() * 255.0);

        //row 2
        GreenMatrix[1][0] = (int) (image_reader.getColor(x - 2, y - 1).getRed() * 255.0);
        GreenMatrix[1][1] = (int) (image_reader.getColor(x - 1, y - 1).getRed() * 255.0);
        GreenMatrix[1][2] = (int) (image_reader.getColor(x, y - 1).getRed() * 255.0);;
        GreenMatrix[1][3] = (int) (image_reader.getColor(x + 1, y - 1).getRed() * 255.0);;
        GreenMatrix[1][4] = (int) (image_reader.getColor(x + 2, y - 1).getRed() * 255.0);;

        //row 3
        GreenMatrix[2][0] = (int) (image_reader.getColor(x - 2, y).getRed() * 255.0);
        GreenMatrix[2][1] = (int) (image_reader.getColor(x - 1, y).getRed() * 255.0);
        GreenMatrix[2][2] = (int) (image_reader.getColor(x, y).getRed() * 255.0);
        GreenMatrix[2][3] = (int) (image_reader.getColor(x + 1, y).getRed() * 255.0);
        GreenMatrix[2][4] = (int) (image_reader.getColor(x + 2, y).getRed() * 255.0);

        //row 4
        GreenMatrix[3][0] = (int) (image_reader.getColor(x - 2, y + 1).getRed() * 255.0);
        GreenMatrix[3][1] = (int) (image_reader.getColor(x - 1, y + 1).getRed() * 255.0);
        GreenMatrix[3][2] = (int) (image_reader.getColor(x, y + 1).getRed() * 255.0);
        GreenMatrix[3][3] = (int) (image_reader.getColor(x + 1, y + 1).getRed() * 255.0);
        GreenMatrix[3][4] = (int) (image_reader.getColor(x + 2, y + 1).getRed() * 255.0);

        //row 5
        GreenMatrix[4][0] = (int) (image_reader.getColor(x - 2, y + 2).getRed() * 255.0);
        GreenMatrix[4][1] = (int) (image_reader.getColor(x - 1, y + 2).getRed() * 255.0);
        GreenMatrix[4][2] = (int) (image_reader.getColor(x, y + 2).getRed() * 255.0);
        GreenMatrix[4][3] = (int) (image_reader.getColor(x + 1, y + 2).getRed() * 255.0);
        GreenMatrix[4][4] = (int) (image_reader.getColor(x + 2, y + 2).getRed() * 255.0);
        return GreenMatrix;

    }

    private int[][] generateBlueMatrix(int x, int y) {

        int blueMatrix[][];
        blueMatrix = new int[5][5];
        Image image = imageView.getImage();
        PixelReader image_reader = image.getPixelReader();

        Color color = image_reader.getColor(x, y);
        //row 1
        blueMatrix[0][0] = (int) (image_reader.getColor(x - 2, y - 2).getRed() * 255.0);
        blueMatrix[0][1] = (int) (image_reader.getColor(x - 1, y - 2).getRed() * 255.0);
        blueMatrix[0][2] = (int) (image_reader.getColor(x, y - 2).getRed() * 255.0);
        blueMatrix[0][3] = (int) (image_reader.getColor(x + 1, y - 2).getRed() * 255.0);
        blueMatrix[0][4] = (int) (image_reader.getColor(x + 2, y - 2).getRed() * 255.0);

        //row 2
        blueMatrix[1][0] = (int) (image_reader.getColor(x - 2, y - 1).getRed() * 255.0);
        blueMatrix[1][1] = (int) (image_reader.getColor(x - 1, y - 1).getRed() * 255.0);
        blueMatrix[1][2] = (int) (image_reader.getColor(x, y - 1).getRed() * 255.0);;
        blueMatrix[1][3] = (int) (image_reader.getColor(x + 1, y - 1).getRed() * 255.0);;
        blueMatrix[1][4] = (int) (image_reader.getColor(x + 2, y - 1).getRed() * 255.0);;

        //row 3
        blueMatrix[2][0] = (int) (image_reader.getColor(x - 2, y).getRed() * 255.0);
        blueMatrix[2][1] = (int) (image_reader.getColor(x - 1, y).getRed() * 255.0);
        blueMatrix[2][2] = (int) (image_reader.getColor(x, y).getRed() * 255.0);
        blueMatrix[2][3] = (int) (image_reader.getColor(x + 1, y).getRed() * 255.0);
        blueMatrix[2][4] = (int) (image_reader.getColor(x + 2, y).getRed() * 255.0);

        //row 4
        blueMatrix[3][0] = (int) (image_reader.getColor(x - 2, y + 1).getRed() * 255.0);
        blueMatrix[3][1] = (int) (image_reader.getColor(x - 1, y + 1).getRed() * 255.0);
        blueMatrix[3][2] = (int) (image_reader.getColor(x, y + 1).getRed() * 255.0);
        blueMatrix[3][3] = (int) (image_reader.getColor(x + 1, y + 1).getRed() * 255.0);
        blueMatrix[3][4] = (int) (image_reader.getColor(x + 2, y + 1).getRed() * 255.0);

        //row 5
        blueMatrix[4][0] = (int) (image_reader.getColor(x - 2, y + 2).getRed() * 255.0);
        blueMatrix[4][1] = (int) (image_reader.getColor(x - 1, y + 2).getRed() * 255.0);
        blueMatrix[4][2] = (int) (image_reader.getColor(x, y + 2).getRed() * 255.0);
        blueMatrix[4][3] = (int) (image_reader.getColor(x + 1, y + 2).getRed() * 255.0);
        blueMatrix[4][4] = (int) (image_reader.getColor(x + 2, y + 2).getRed() * 255.0);
        return blueMatrix;

    }

    private int calculateSumProduct(int[][] matrix, int[][] laplacianMatrix) {
        int sumProduct = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                sumProduct += (matrix[i][j] * laplacianMatrix[i][j]);

            }
        }
        if (sumProduct < minValue) {
            minValue = sumProduct;
        }

        if (sumProduct > maxValue) {
            maxValue = sumProduct;
        }
        return sumProduct;
    }

    private int normalise(int sumProduct) {
        int min = minValue;
        int max = maxValue;
        int normalisedSumProduct =((sumProduct - min) * 255)/(max - min);
        return normalisedSumProduct;

    }

}
