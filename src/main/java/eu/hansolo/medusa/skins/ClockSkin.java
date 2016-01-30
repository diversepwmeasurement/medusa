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
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;


/**
 * Created by hansolo on 28.01.16.
 */
public class ClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH          = 250;
    private static final double            PREFERRED_HEIGHT         = 250;
    private static final double            MINIMUM_WIDTH            = 50;
    private static final double            MINIMUM_HEIGHT           = 50;
    private static final double            MAXIMUM_WIDTH            = 1024;
    private static final double            MAXIMUM_HEIGHT           = 1024;
    private static final DateTimeFormatter DATE_FORMATER            = DateTimeFormatter.ofPattern("EE d");
    private static final DateTimeFormatter TIME_FORMATTER           = DateTimeFormatter.ofPattern("HH:mm");
    private              double            size;
    private              Canvas            ticks;
    private              GraphicsContext   ctx;
    private              Rectangle         hour;
    private              Rectangle         minute;
    private              Rectangle         second;
    private              Circle            knob;
    private              Text              title;
    private              Text              dateText;
    private              Text              text;
    private              Pane              pane;
    private              Rotate            hourRotate;
    private              Rotate            minuteRotate;
    private              Rotate            secondRotate;
    private              Group             shadowGroup;
    private              DropShadow        dropShadow;


    // ******************** Constructors **************************************
    public ClockSkin(Clock clock) {
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

        hour  = new Rectangle(3, 60);
        hour.setArcHeight(3);
        hour.setArcWidth(3);
        hour.setStroke(getSkinnable().getHourNeedleColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Rectangle(3, 96);
        minute.setArcHeight(3);
        minute.setArcWidth(3);
        minute.setStroke(getSkinnable().getMinuteNeedleColor());
        minute.getTransforms().setAll(minuteRotate);

        second = new Rectangle(1, 96);
        second.setArcHeight(1);
        second.setArcWidth(1);
        second.setStroke(getSkinnable().getSecondNeedleColor());
        second.getTransforms().setAll(secondRotate);
        second.setVisible(getSkinnable().isSecondsVisible());
        second.setManaged(getSkinnable().isSecondsVisible());

        knob = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, 4.5);
        knob.setStroke(Color.web("#282a3280"));

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(hour, minute, second, knob);
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(getSkinnable().isTitleVisible());
        title.setManaged(getSkinnable().isTitleVisible());

        dateText = new Text("");
        dateText.setVisible(getSkinnable().isDateVisible());
        dateText.setManaged(getSkinnable().isDateVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.getChildren().addAll(ticks, title, dateText, text, shadowGroup);

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
            dateText.setVisible(getSkinnable().isDateVisible());
            dateText.setManaged(getSkinnable().isDateVisible());
            second.setVisible(getSkinnable().isSecondsVisible());
            second.setManaged(getSkinnable().isSecondsVisible());
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
        ctx.setLineCap(StrokeLineCap.ROUND);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.405 * sinValue, center.getY() + size * 0.405 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.435 * sinValue, center.getY() + size * 0.435 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.465 * sinValue, center.getY() + size * 0.465 * cosValue);

            if (counter % 5 == 0) {
                // Draw hour tickmark
                ctx.setLineWidth(size * 0.01);
                ctx.setStroke(hourTickMarkColor);
                ctx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (counter % 1 == 0) {
                // Draw minute tickmark
                ctx.setLineWidth(size * 0.005);
                ctx.setStroke(minuteTickMarkColor);
                ctx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Resizing ******************************************
    public void updateTime(final LocalDateTime TIME) {
        if (getSkinnable().isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (getSkinnable().isDiscreteSeconds()) {
            secondRotate.setAngle(TIME.getSecond() * 6);
        } else {
            secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
        }

        hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));

        text.setText(TIME_FORMATTER.format(TIME));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateText.setText(DATE_FORMATER.format(TIME).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            ticks.setWidth(size);
            ticks.setHeight(size);
            drawTicks();

            hour.setFill(getSkinnable().getHourNeedleColor());
            hour.setWidth(size * 0.015);
            hour.setHeight(size * 0.29);
            hour.setArcWidth(size * 0.015);
            hour.setArcHeight(size * 0.015);
            hour.relocate((size - hour.getWidth()) * 0.5, size * 0.21);

            minute.setFill(getSkinnable().getMinuteNeedleColor());
            minute.setWidth(size * 0.015);
            minute.setHeight(size * 0.47);
            minute.setArcWidth(size * 0.015);
            minute.setArcHeight(size * 0.015);
            minute.relocate((size - minute.getWidth()) * 0.5, size * 0.03);

            second.setFill(getSkinnable().getSecondNeedleColor());
            second.setWidth(size * 0.005);
            second.setHeight(size * 0.47);
            second.setArcWidth(size * 0.015);
            second.setArcHeight(size * 0.015);
            second.relocate((size - second.getWidth()) * 0.5, size * 0.03);

            knob.setFill(getSkinnable().getKnobColor());
            knob.setRadius(size * 0.0225);
            knob.setCenterX(center);
            knob.setCenterY(center);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateText.setFill(getSkinnable().getDateColor());
            dateText.setFont(Fonts.latoLight(size * 0.05));
            dateText.relocate((center - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(getSkinnable().getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            minuteRotate.setPivotX(minute.getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getHeight());
            hourRotate.setPivotX(hour.getWidth() * 0.5);
            hourRotate.setPivotY(hour.getHeight());
            secondRotate.setPivotX(second.getWidth() * 0.5);
            secondRotate.setPivotY(second.getHeight());
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        LocalDateTime time = getSkinnable().getTime();

        updateTime(time);

        title.setText(getSkinnable().getTitle());
        Helper.adjustTextSize(title, 0.6 * size, size * 0.12);
        title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

        text.setText(TIME_FORMATTER.format(time));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateText.setText(DATE_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);
    }
}