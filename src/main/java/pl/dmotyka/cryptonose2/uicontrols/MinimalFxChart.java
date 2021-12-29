/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2.uicontrols;

import java.util.Arrays;
import java.util.logging.Logger;

import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MinimalFxChart extends Region {

    public static final Logger logger = Logger.getLogger(MinimalFxChart.class.getName());

    private final Canvas canvas;
    private double[] values;
    private Effect chartEffect;
    private Paint chartPaint;
    private double marginsHorizontalPercent = 0;
    private double marginsVerticalPercent = 0;

    public MinimalFxChart(double[] values, Paint chartPaint, Effect chartEffect) {
        if (values == null || values.length < 2)
            throw new IllegalArgumentException("please provide at least 2 values");
        this.values = values;
        this.chartEffect = chartEffect;
        this.chartPaint = chartPaint;
        widthProperty().addListener(o -> paint());
        heightProperty().addListener(o -> paint());
        canvas = new Canvas();
        getChildren().add(canvas);
        // workaround for empty canvas when drawing during unminimizing window - repaint after unminimized
        sceneProperty().addListener((sceneObservable, oldScene, newScene) -> {
            if (newScene != null) {
                Window window = newScene.getWindow();
                if (window instanceof Stage) {
                    repaintOnUnminimize((Stage)window);
                } else {
                    newScene.windowProperty().addListener((windowObservable, oldWindow, newWindow) -> {
                        if (newWindow instanceof Stage) {
                            repaintOnUnminimize((Stage) newWindow);
                        }
                    });
                }
            }
        });
    }

    public synchronized void repaint(double[] values) {
        if (values == null || values.length < 2)
            throw new IllegalArgumentException("please provide at least 2 values");
        this.values = values;
        paint();
    }

    public synchronized void setMarginsHorizontalPercent(double marginsHorizontalPercent) {
        if (marginsHorizontalPercent >=50) {
            throw new IllegalArgumentException("value should be less than 50(%)");
        }
        this.marginsHorizontalPercent = marginsHorizontalPercent;
    }

    public synchronized void setMarginsVerticalPercent(double marginsVerticalPercent) {
        if (marginsVerticalPercent >=50) {
            throw new IllegalArgumentException("value should be less than 50(%)");
        }
        this.marginsVerticalPercent = marginsVerticalPercent;
    }

    public synchronized double getMarginsHorizontalPercent() {
        return marginsHorizontalPercent;
    }

    public synchronized double getMarginsVerticalPercent() {
        return marginsVerticalPercent;
    }

    public synchronized void setChartEffect(Effect chartEffect) {
        this.chartEffect = chartEffect;
    }

    public synchronized void setChartPaint(Paint chartPaint) {
        this.chartPaint = chartPaint;
    }

    private synchronized void paint() {
        double width = getWidth();
        double height = getHeight();
        canvas.setWidth(width);
        canvas.setHeight(height);
        double[][] rescaledValues = rescaleValues(values, width, height);
        double[] arguments = rescaledValues[0];
        double[] values = rescaledValues[1];
        var gc = canvas.getGraphicsContext2D();
        gc.setEffect(null);
        gc.clearRect(0,0,width,height);
        gc.setEffect(chartEffect);
        gc.setStroke(chartPaint);
        gc.beginPath();
        gc.moveTo(arguments[0],values[0]);
        for(int i=1; i<arguments.length;i++)
            gc.lineTo(arguments[i], values[i]);
        gc.stroke();
        gc.closePath();
    }

    // adapt to a screen coordinates, given width, height and margins
    private double[][] rescaleValues(double[] values, double width, double height) {
        double drawingWidth = width - 2*marginsHorizontalPercent*width/100;
        double drawingHeight = height - 2*marginsVerticalPercent*height/100;
        int marginHorizontalPx = (int)(marginsHorizontalPercent*width/100);
        int marginVerticalPx = (int)(marginsVerticalPercent*height/100);
        double[] scaledArguments = new double[values.length];
        double[] scaledValues = Arrays.copyOf(values, values.length);
        double minVal = Double.POSITIVE_INFINITY;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i=0; i<scaledValues.length; i++) {
            scaledValues[i] = -scaledValues[i];
            if (scaledValues[i] < minVal) {
                minVal = scaledValues[i];
            }
            if (scaledValues[i] > maxVal) {
                maxVal = scaledValues[i];
            }
        }
        double valuesRange = Math.abs(minVal-maxVal);
        for (int i=0; i<scaledValues.length; i++) {
            scaledValues[i] -= minVal;
            scaledValues[i] = scaledValues[i]/valuesRange;
            scaledValues[i] = scaledValues[i] * drawingHeight + marginVerticalPx;
        }
        for (int i=0; i<values.length; i++) {
            scaledArguments[i] = ((double)i)/(values.length-1);
            scaledArguments[i] = scaledArguments[i] * drawingWidth + marginHorizontalPx;
        }
        return new double[][] {scaledArguments, scaledValues};
    }

    private void repaintOnUnminimize(Stage stage) {
        stage.iconifiedProperty().addListener((iconifiedObservable, oldVal, newVal) -> {
            if (!newVal) {
                paint();
            }
        });
    }
}
