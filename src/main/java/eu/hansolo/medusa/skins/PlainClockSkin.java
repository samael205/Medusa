/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;


/**
 * Created by hansolo on 29.01.16.
 */
public class PlainClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH      = 250;
    private static final double            PREFERRED_HEIGHT     = 250;
    private static final double            MINIMUM_WIDTH        = 50;
    private static final double            MINIMUM_HEIGHT       = 50;
    private static final double            MAXIMUM_WIDTH        = 1024;
    private static final double            MAXIMUM_HEIGHT       = 1024;
    private static final DateTimeFormatter DATE_NUMBER_FORMATER = DateTimeFormatter.ofPattern("d");
    private static final DateTimeFormatter TIME_FORMATTER       = DateTimeFormatter.ofPattern("HH:mm");
    private double          size;
    private Canvas          ticks;
    private GraphicsContext ctx;
    private Path            hour;
    private Path            minute;
    private Path            second;
    private Text            title;
    private Text            dateNumber;
    private Text            text;
    private Pane            pane;
    private Rotate          hourRotate;
    private Rotate          minuteRotate;
    private Rotate          secondRotate;


    // ******************** Constructors **************************************
    public PlainClockSkin(Clock clock) {
        super(clock);

        minuteRotate = new Rotate();
        hourRotate   = new Rotate();
        secondRotate = new Rotate();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() < 0 && getSkinnable().getPrefHeight() < 0) {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        ticks = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx   = ticks.getGraphicsContext2D();

        hour  = new Path();
        hour.setFillRule(FillRule.EVEN_ODD);
        hour.setStroke(null);
        hour.getTransforms().setAll(hourRotate);

        minute = new Path();
        minute.setFillRule(FillRule.EVEN_ODD);
        minute.setStroke(null);
        minute.getTransforms().setAll(minuteRotate);

        second = new Path();
        second.setFillRule(FillRule.EVEN_ODD);
        second.setStroke(null);
        second.getTransforms().setAll(secondRotate);

        title = new Text("");
        title.setVisible(getSkinnable().isTitleVisible());
        title.setManaged(getSkinnable().isTitleVisible());

        dateNumber = new Text("");
        dateNumber.setVisible(getSkinnable().isDateVisible());
        dateNumber.setManaged(getSkinnable().isDateVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.getChildren().addAll(ticks, title, dateNumber, text, hour, minute, second);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_tYPE) {
        if ("RESIZE".equals(EVENT_tYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_tYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_tYPE)) {
            title.setVisible(getSkinnable().isTitleVisible());
            title.setManaged(getSkinnable().isTitleVisible());
            text.setVisible(getSkinnable().isTextVisible());
            text.setManaged(getSkinnable().isTextVisible());
            dateNumber.setVisible(getSkinnable().isDateVisible());
            dateNumber.setManaged(getSkinnable().isDateVisible());
        }
    }


    // ******************** Canvas ********************************************
    private void drawTicks() {
        ctx.clearRect(0, 0, size, size);
        double  sinValue;
        double  cosValue;
        double  startAngle          = 180;
        double  angleStep           = 360 / 60;
        Point2D center              = new Point2D(size * 0.5, size * 0.5);
        Color   hourTickMarkColor   = getSkinnable().getHourTickMarkColor();
        Color   minuteTickMarkColor = getSkinnable().getMinuteTickMarkColor();
        Font    font                = Fonts.robotoLight(size * 0.084);
        ctx.setLineCap(StrokeLineCap.BUTT);
        ctx.setFont(font);
        ctx.setLineWidth(size * 0.00539811);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.40350877 * sinValue, center.getY() + size * 0.40350877 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.451417 * sinValue, center.getY() + size * 0.451417 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.47435897 * sinValue, center.getY() + size * 0.47435897 * cosValue);

            if (counter % 5 == 0) {
                ctx.setStroke(hourTickMarkColor);
                ctx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (counter % 1 == 0) {
                ctx.setStroke(minuteTickMarkColor);
                ctx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createHourPointer() {
        double width  = size * 0.04723347;
        double height = size * 0.35897436;
        hour.getElements().clear();
        hour.getElements().add(new MoveTo(0.18571428571428572 * width, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(0.18571428571428572 * width, 0.9116541353383458 * height,
                                                0.32857142857142857 * width, 0.8928571428571429 * height,
                                                0.5 * width, 0.8928571428571429 * height));
        hour.getElements().add(new CubicCurveTo(0.6714285714285714 * width, 0.8928571428571429 * height,
                                                0.8142857142857143 * width, 0.9116541353383458 * height,
                                                0.8142857142857143 * width, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(0.8142857142857143 * width, 0.956766917293233 * height,
                                                0.6714285714285714 * width, 0.9755639097744361 * height,
                                                0.5 * width, 0.9755639097744361 * height));
        hour.getElements().add(new CubicCurveTo(0.32857142857142857 * width, 0.9755639097744361 * height,
                                                0.18571428571428572 * width, 0.956766917293233 * height,
                                                0.18571428571428572 * width, 0.9342105263157895 * height));
        hour.getElements().add(new ClosePath());
        hour.getElements().add(new MoveTo(0.0, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(0.0, 0.9699248120300752 * height,
                                                0.22857142857142856 * width, height,
                                                0.5 * width, height));
        hour.getElements().add(new CubicCurveTo(0.7714285714285715 * width, height,
                                                width, 0.9699248120300752 * height,
                                                width, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(width, 0.9116541353383458 * height,
                                                0.9142857142857143 * width, 0.8928571428571429 * height,
                                                0.7857142857142857 * width, 0.8796992481203008 * height));
        hour.getElements().add(new CubicCurveTo(0.7857142857142857 * width, 0.8796992481203008 * height,
                                                0.7857142857142857 * width, 0.07706766917293233 * height,
                                                0.7857142857142857 * width, 0.07706766917293233 * height));
        hour.getElements().add(new LineTo(0.5 * width, 0.0));
        hour.getElements().add(new LineTo(0.21428571428571427 * width, 0.07706766917293233 * height));
        hour.getElements().add(new CubicCurveTo(0.21428571428571427 * width, 0.07706766917293233 * height,
                                                0.21428571428571427 * width, 0.8796992481203008 * height,
                                                0.21428571428571427 * width, 0.8796992481203008 * height));
        hour.getElements().add(new CubicCurveTo(0.08571428571428572 * width, 0.8928571428571429 * height,
                                                0.0, 0.9116541353383458 * height,
                                                0.0, 0.9342105263157895 * height));
        hour.getElements().add(new ClosePath());
    }

    private void createMinutePointer() {
        double width  = size * 0.04723347;
        double height = size * 0.47503374;
        minute.getElements().clear();
        minute.getElements().add(new MoveTo(0.18571428571428572 * width, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.18571428571428572 * width, 0.9332386363636364 * height,
                                                  0.32857142857142857 * width, 0.9190340909090909 * height,
                                                  0.5 * width, 0.9190340909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.6714285714285714 * width, 0.9190340909090909 * height,
                                                  0.8142857142857143 * width, 0.9332386363636364 * height,
                                                  0.8142857142857143 * width, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.8142857142857143 * width, 0.9673295454545454 * height,
                                                  0.6714285714285714 * width, 0.9815340909090909 * height,
                                                  0.5 * width, 0.9815340909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.32857142857142857 * width, 0.9815340909090909 * height,
                                                  0.18571428571428572 * width, 0.9673295454545454 * height,
                                                  0.18571428571428572 * width, 0.9502840909090909 * height));
        minute.getElements().add(new ClosePath());
        minute.getElements().add(new MoveTo(0.0, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.0, 0.9772727272727273 * height,
                                                  0.22857142857142856 * width, height,
                                                  0.5 * width, height));
        minute.getElements().add(new CubicCurveTo(0.7714285714285715 * width, height,
                                                  width, 0.9772727272727273 * height,
                                                  width, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(width, 0.9318181818181818 * height,
                                                  0.9 * width, 0.9147727272727273 * height,
                                                  0.7428571428571429 * width, 0.90625 * height));
        minute.getElements().add(new CubicCurveTo(0.7428571428571429 * width, 0.90625 * height,
                                                  0.7428571428571429 * width, 0.05113636363636364 * height,
                                                  0.7428571428571429 * width, 0.05113636363636364 * height));
        minute.getElements().add(new LineTo(0.5 * width, 0.0));
        minute.getElements().add(new LineTo(0.2571428571428571 * width, 0.05113636363636364 * height));
        minute.getElements().add(new CubicCurveTo(0.2571428571428571 * width, 0.05113636363636364 * height,
                                                  0.2571428571428571 * width, 0.90625 * height,
                                                  0.2571428571428571 * width, 0.90625 * height));
        minute.getElements().add(new CubicCurveTo(0.1 * width, 0.9147727272727273 * height,
                                                  0.0, 0.9318181818181818 * height,
                                                  0.0, 0.9502840909090909 * height));
        minute.getElements().add(new ClosePath());
    }

    private void createSecondPointer() {
        double width  = size * 0.04723347;
        double height = size * 0.50404858;
        second.getElements().clear();
        second.getElements().add(new MoveTo(0.18571428571428572 * width, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(0.18571428571428572 * width, 0.9370816599732262 * height,
                                                  0.32857142857142857 * width, 0.9236947791164659 * height,
                                                  0.5 * width, 0.9236947791164659 * height));
        second.getElements().add(new CubicCurveTo(0.6714285714285714 * width, 0.9236947791164659 * height,
                                                  0.8142857142857143 * width, 0.9370816599732262 * height,
                                                  0.8142857142857143 * width, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(0.8142857142857143 * width, 0.9692101740294511 * height,
                                                  0.6714285714285714 * width, 0.9825970548862115 * height,
                                                  0.5 * width, 0.9825970548862115 * height));
        second.getElements().add(new CubicCurveTo(0.32857142857142857 * width, 0.9825970548862115 * height,
                                                  0.18571428571428572 * width, 0.9692101740294511 * height,
                                                  0.18571428571428572 * width, 0.9531459170013387 * height));
        second.getElements().add(new ClosePath());
        second.getElements().add(new MoveTo(0.0, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(0.0, 0.9785809906291834 * height,
                                                  0.22857142857142856 * width, height,
                                                  0.5 * width, height));
        second.getElements().add(new CubicCurveTo(0.7714285714285715 * width, height,
                                                  width, 0.9785809906291834 * height,
                                                  width, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(width, 0.9330655957161981 * height,
                                                  0.8714285714285714 * width, 0.9170013386880856 * height,
                                                  0.6857142857142857 * width, 0.9089692101740294 * height));
        second.getElements().add(new CubicCurveTo(0.6857142857142857 * width, 0.9089692101740294 * height,
                                                  0.6857142857142857 * width, 0.04819277108433735 * height,
                                                  0.6857142857142857 * width, 0.04819277108433735 * height));
        second.getElements().add(new LineTo(0.5 * width, 0.0));
        second.getElements().add(new LineTo(0.3142857142857143 * width, 0.04819277108433735 * height));
        second.getElements().add(new CubicCurveTo(0.3142857142857143 * width, 0.04819277108433735 * height,
                                                  0.3142857142857143 * width, 0.9089692101740294 * height,
                                                  0.3142857142857143 * width, 0.9089692101740294 * height));
        second.getElements().add(new CubicCurveTo(0.12857142857142856 * width, 0.9170013386880856 * height,
                                                  0.0, 0.9330655957161981 * height,
                                                  0.0, 0.9531459170013387 * height));
        second.getElements().add(new ClosePath());
    }


    // ******************** Resizing ******************************************
    public void updateTime(final LocalDateTime TIME) {
        if (getSkinnable().isDiscreteSteps()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
            secondRotate.setAngle(TIME.getSecond() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
            secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
        }
        hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));

        text.setText(TIME_FORMATTER.format(TIME));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateNumber.setText(DATE_NUMBER_FORMATER.format(TIME).toUpperCase());
        Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
        dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.6), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        LocalDateTime time = getSkinnable().getTime();

        updateTime(time);

        title.setText(getSkinnable().getTitle());
        Helper.adjustTextSize(title, 0.6 * size, size * 0.12);
        title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

        text.setText(TIME_FORMATTER.format(time));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateNumber.setText(DATE_NUMBER_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
        dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.6), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            ticks.setWidth(size);
            ticks.setHeight(size);
            drawTicks();

            createHourPointer();
            hour.setFill(getSkinnable().getHourNeedleColor());
            hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, size * 0.16464238);

            createMinutePointer();
            minute.setFill(getSkinnable().getMinuteNeedleColor());
            minute.relocate((size - minute.getLayoutBounds().getWidth()) * 0.5, size * 0.048583);

            createSecondPointer();
            second.setFill(getSkinnable().getSecondNeedleColor());
            second.relocate((size - second.getLayoutBounds().getWidth()) * 0.5, size * 0.01956815);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateNumber.setFill(getSkinnable().getDateColor());
            dateNumber.setFont(Fonts.latoLight(size * 0.06666667));
            dateNumber.relocate((center - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.6), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(getSkinnable().getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            hourRotate.setPivotX(hour.getLayoutBounds().getWidth() * 0.5);
            hourRotate.setPivotY(hour.getLayoutBounds().getHeight() * 0.93421053);
            minuteRotate.setPivotX(minute.getLayoutBounds().getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getLayoutBounds().getHeight() * 0.95028409);
            secondRotate.setPivotX(second.getLayoutBounds().getWidth() * 0.5);
            secondRotate.setPivotY(second.getLayoutBounds().getHeight() * 0.95314592);
        }
    }
}
