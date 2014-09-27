package org.eclipse.tutorials.mqtt;

import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.TimeStringConverter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main extends Application {
	private static final String BROKER_URI = "tcp://iot.eclipse.org:1883";

	@Override
	public void start(Stage stage) {
		stage.setTitle("Live temperature monitoring");
		// defining the axes
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel("Time");
		xAxis.setForceZeroInRange(false);
		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			TimeStringConverter tsc = new TimeStringConverter("HH:mm:ss");

			@Override
			public String toString(Number t) {
				return tsc.toString(new Date(t.longValue()));
			}

			@Override
			public Number fromString(String string) {
				return 1;
			}
		});

		final NumberAxis yAxis = new NumberAxis();
		yAxis.setForceZeroInRange(false);

		// creating the chart
		final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(
				xAxis, yAxis);

		lineChart.setTitle("Live temperature monitoring");
		// defining a series
		final Series<Number, Number> series = new XYChart.Series<Number, Number>();
		series.setName("Temperature");

		Scene scene = new Scene(lineChart, 800, 600);
		lineChart.getData().add(series);

		try {
			final MqttClient mqttClient = new MqttClient(BROKER_URI,
					MqttClient.generateClientId(), new MemoryPersistence());

			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message)
						throws Exception {

					try {
						final double sensorValue = Double
								.parseDouble(new String(message.getPayload()));

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								series.getData().add(
										new Data<Number, Number>(System
												.currentTimeMillis(),
												sensorValue));
							}
						});

					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// not used
				}

				@Override
				public void connectionLost(Throwable cause) {
					System.out.println("Connection lost: "
							+ cause.getLocalizedMessage());
				}
			});
			mqttClient.connect();
			mqttClient.subscribe(
					"javaonedemo/eclipse-greenhouse/sensors/temperature", 1);

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}