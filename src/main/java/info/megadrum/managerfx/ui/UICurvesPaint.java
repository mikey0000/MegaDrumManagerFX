package info.megadrum.managerfx.ui;

import java.util.Arrays;

import javax.swing.event.EventListenerList;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UICurvesPaint extends Pane {
    private final GraphicsContext gc;

    private final Color lineColor = Color.BLACK;
    private final Color bgColor = Color.WHITE;
    private final Color gridColor = Color.LIGHTGRAY;
    private final Color tickColor = Color.BLACK;
    private final Color labelsColor = Color.BLACK;
    private final Color hookColor = Color.RED;
    private static final int xShift = 24;
    private static final int yShift = 4;
    private final int[] yValues = {2, 32, 64, 96, 128, 160, 192, 224, 255};
    private final int[] MdYvalues = {2, 32, 64, 96, 128, 160, 192, 224, 255};
    private int posId;
    private boolean posCaptured;
    private int xPos;
    private int yPos;

    protected EventListenerList listenerList = new EventListenerList();

    public void addControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.add(ControlChangeEventListener.class, listener);
    }

    public void removeControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.remove(ControlChangeEventListener.class, listener);
    }

    protected void fireControlChangeEvent(ControlChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ControlChangeEventListener.class) {
                ((ControlChangeEventListener) listeners[i + 1]).controlChangeEventOccurred(evt, 0);
            }
        }
    }

    public UICurvesPaint() {
        Canvas canvas = new Canvas(300, 276);
        setMinHeight(276);
        gc = canvas.getGraphicsContext2D();
        repaint();

        getChildren().add(canvas);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                posCaptured = false;
                xPos = (int) event.getX();
                yPos = (int) event.getY();
                if ((xPos > xShift) && (xPos < (xShift + 256))) {
                    posId = (xPos - xShift + 15) / 32;
                    posCaptured = true;
                    updateYvalues(yPos);
                    fireControlChangeEvent(new ControlChangeEvent(this));
                }
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<>() {

                    @Override
                    public void handle(MouseEvent event) {
                        if (posCaptured) {
                            yPos = (int) event.getY();
                            updateYvalues(yPos);
                            fireControlChangeEvent(new ControlChangeEvent(this));
                        }
                    }
                });

    }


    private void updateYvalues(int y) {
        if ((y > yShift) && (y < (yShift + 256))) {
            int yValue = 256 - (y - yShift);
            if (yValue < 2) {
                yValue = 2;
            }
            if (yValue > 255) {
                yValue = 255;
            }
            yValues[posId] = yValue;
            repaint();
        }
    }

    public void repaint() {
        gc.setFill(gridColor);
        gc.fillRect(0, 0, 300, 300);
        gc.setFill(bgColor);
        gc.fillRect(xShift, yShift, 256, 256);
        Font labelsFont = new Font(8.0);
        gc.setFont(labelsFont);
        gc.setStroke(gridColor);
        for (int i = 0; i < 9; i++) {
            gc.setStroke(gridColor);
            gc.strokeLine((i * 32) + xShift, yShift, (i * 32) + xShift, yShift + 256);
            gc.strokeLine(xShift, (i * 32) + yShift, xShift + 256, (i * 32) + yShift);
            gc.setStroke(tickColor);
            gc.strokeLine((i * 32) + xShift, yShift + 256, (i * 32) + xShift, yShift + 256 + 4);
            gc.strokeLine(xShift - 4, (i * 32) + yShift, xShift, (i * 32) + yShift);
            gc.setFill(labelsColor);
            gc.fillText("P" + (i + 1), (i * 32) + xShift - 5, yShift + 256 + 12);
            if (i > 0) {
                if (i < 8) {
                    gc.fillText(((Integer) (256 - i * 32)).toString(), xShift - 20, (i * 32) + yShift + 4);
                } else {
                    gc.fillText(((Integer) (2)).toString(), xShift - 20, (i * 32) + yShift + 4);
                }
            } else {
                gc.fillText(((Integer) (255)).toString(), xShift - 20, yShift + 4);
            }
        }

        if (yValues != null) {
            for (int i = 0; i < 9; i++) {
                gc.setStroke(lineColor);
                if (i < 8) {
                    gc.strokeLine(xShift + (i * 32), 256 - yValues[i] + yShift, xShift + ((i + 1) * 32), 256 - yValues[i + 1] + yShift);
                }
                gc.setStroke(hookColor);
                gc.strokeOval(xShift + (i * 32) - 3, 256 - yValues[i] + yShift - 3, 6, 6);
            }
        }
    }

    public void setYvalues(int[] values, boolean setFromSysex) {
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = values[i];
            if (setFromSysex) {
                MdYvalues[i] = values[i];
            }
        }
        repaint();
    }

    public void setYvalue(int p, int value) {
        yValues[p] = value;
        repaint();
    }

    public void setMdYvalues(int[] values) {
        System.arraycopy(values, 0, MdYvalues, 0, yValues.length);
        repaint();
    }

    public void getYvalues(int[] values) {
        System.arraycopy(yValues, 0, values, 0, yValues.length);
    }

    public boolean isInSync() {
        return Arrays.equals(yValues, MdYvalues);
    }

}
