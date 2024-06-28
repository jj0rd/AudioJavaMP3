package com.example.audio;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class HelloController implements Initializable {

    @FXML
    private Pane pane;
    @FXML
    private Label songLabel;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton, pickSongButton, addToPlayListButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private ListView<String> playlistView;
    @FXML
    private ToggleButton loopButton;

    private Media media;
    private MediaPlayer mediaPlayer;

    private File directory;
    private File[] files;

    private ArrayList<File> songs;

    private int songNumber;
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};

    private Timer timer;
    private TimerTask task;

    private boolean running;
    private File selectedFile;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        songs = new ArrayList<>();

        directory = new File("music");

        files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
            }
        }

        if (!songs.isEmpty()) {
            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);
        }

        // Zaktualizuj playlistView
        updatePlaylistView();

        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }

        speedBox.setOnAction(this::changeSpeed);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
                }
            }
        });

        songProgressBar.setStyle("-fx-accent: #00FF00;");
    }

    private void updatePlaylistView() {
        playlistView.getItems().clear();
        for (File song : songs) {
            playlistView.getItems().add(song.getName());
        }
    }

    public void playMedia() {
        if (mediaPlayer == null) {
            return;
        }

        if (songLabel.getText().equals("MP3 PLAYER"))
            songLabel.setText(songs.get(songNumber).getName());
        beginTimer();
        changeSpeed(null);
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        mediaPlayer.play();
    }

    public void pickSong() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick a song");
        fileChooser.setInitialDirectory(new File("C:\\"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP3 audio", "*.mp3"));

        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            media = new Media(selectedFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);

            if(songs.isEmpty())
            {
                addAddToPlayList();
            }
            playMedia();
            songLabel.setText(selectedFile.getName());
        }
    }

    public void addAddToPlayList() {
        boolean isIn = false;
        if (selectedFile != null) {
            for (File file : songs) {
                if (selectedFile.equals(file)) {
                    isIn = true;
                    break;
                }
            }
            if (!isIn) {
                songs.add(selectedFile);
                selectedFile = null;
                if (mediaPlayer == null && !songs.isEmpty()) {
                    songNumber = songs.size() - 1;
                    media = new Media(songs.get(songNumber).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);
                }
                updatePlaylistView();
            }
        }
    }

    public void removeFromPlaylist() {
        if (!songs.isEmpty() && songNumber >= 0 && songNumber < songs.size() && !songLabel.getText().equals("MP3 PLAYER")) {
            pauseMedia();
            songs.remove(songNumber);
            if (songs.isEmpty()) {
                mediaPlayer.stop();
                mediaPlayer = null;
                songLabel.setText("MP3 PLAYER");
            } else {
                if (songNumber >= songs.size()) {
                    songNumber = songs.size() - 1;
                }
                media = new Media(songs.get(songNumber).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);
                songLabel.setText(songs.get(songNumber).getName());
                playMedia();
            }
            updatePlaylistView();
        }
    }

    public void pauseMedia() {
        cancelTimer();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resetMedia() {
        songProgressBar.setProgress(0);
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(0));
        }
    }

    public void previousMedia() {
        if (mediaPlayer == null) {
            return;
        }

        if (songNumber > 0) {
            songNumber--;
        } else {
            songNumber = songs.size() - 1;
        }
        mediaPlayer.stop();
        if (running) {
            cancelTimer();
        }
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);
        songLabel.setText(songs.get(songNumber).getName());
        playMedia();
    }

    public void nextMedia() {
        if (mediaPlayer == null) {
            return;
        }

        if (songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        mediaPlayer.stop();
        if (running) {
            cancelTimer();
        }
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);
        songLabel.setText(songs.get(songNumber).getName());
        playMedia();
    }

    public void changeSpeed(ActionEvent event) {
        if (mediaPlayer == null) {
            return;
        }

        if (speedBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        }
    }

    public void beginTimer() {
        if (mediaPlayer == null) {
            return;
        }

        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current / end);
                if (current / end == 1) {
                    cancelTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void cancelTimer() {
        running = false;
        if (timer != null) {
            timer.cancel();
        }
    }

    private void handleEndOfMedia() {
        if (loopButton.isSelected()) {
            resetMedia();
            playMedia();
        } else {
            nextMedia();
        }
    }
}
