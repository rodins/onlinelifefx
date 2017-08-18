/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package onlinelifefx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.control.GridView;
import org.controlsfx.control.StatusBar;

/**
 *
 * @author sergey
 */

public class OnlinelifeFX extends Application {
    public static final String DOMAIN = "http://online-life.club";
    public final String PROG_NAME = "Online Life";
    private final ExecutorService exec = Executors.newCachedThreadPool();
    //private final ExecutorService exec2 = Executors.newSingleThreadExecutor(); // For downloading files
    private final TreeView<PlayItem> tvPlaylists = new TreeView<>();
    private TreeItem<PlayItem> rootItem;
    private TreeItem<Link> rootItemCategories;
    
    private Button btnPrev, btnNext, btnUp, btnSavedSerials, btnSavedMovies, btnInfo;
    private Button btnHistory;
    //private ToggleButton btnPlay, btnDownload;
    private Label lbPage;
    private final Label lbActors = new Label(),
                        lbActorsInfo = new Label();
    
    private List<Link> categories;
    private Results results;
    private String baseUrl;
    private DisplayMode displayMode;
    private Stage window;
    
    private final ObservableList<Results> backResults = FXCollections.observableArrayList();
    private final ObservableList<Actors> backActors = FXCollections.observableArrayList();
    
    private final Button btnCancel = new Button();
    private final Button btnCancelStatus = new Button();
    
    private final ProgressBar pbDownload = new ProgressBar(0.0);
    private final Label lbDownload = new Label();
    private final Label lbInfo = new Label();
    private final VBox vbDownload = new VBox();
    
    private final VBox vbCenter = new VBox();
    private final VBox vbActors = new VBox();
    
    private final GridView<Result> resultsView = new GridView<>();
    private final StatusBar statusBar = new StatusBar();
    private final BorderPane border = new BorderPane();
    private final TreeView<Link> tvCategories = new TreeView<>();
    private final ListView<Link> lvActors = new ListView<>();
    
    private boolean isSavePrevResults = false;
    
    private final List<Task> tasks = new CopyOnWriteArrayList<>(); // List of running task to cancel them
    private Result parent = null; // for remembering serial episodes played
    private File appDir, serialsDir, watchedDir, moviesDir;
    private final Set<String> comments = new HashSet<>(); // saved in file episodes titles (comments)
   
    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        
        tvPlaylists.setShowRoot(false);
        rootItem = new TreeItem(new PlayItem());
        rootItem.setExpanded(true);
        tvPlaylists.setRoot(rootItem);
        
        tvCategories.setShowRoot(false);
        rootItemCategories = new TreeItem<>(new Link("", ""));
        rootItemCategories.setExpanded(true);
        tvCategories.setRoot(rootItemCategories);
        
        TextField query = new TextField();
        query.setOnAction((ActionEvent event) -> {
            String text = query.getText().trim();
            if(!text.isEmpty()) {
                isSavePrevResults = true;
                resultsActionSearch(text);
            }
        });
        query.setPromptText("Search onlinelife");
        
        ListView<Results> lvBackResults = new ListView<>(backResults);
        lvBackResults.setOnMouseClicked((MouseEvent event) -> {
            if(event.getClickCount() == 1) {
                // Save currently displayed results
                savePrevResultsSimple();
                results = lvBackResults.getSelectionModel().getSelectedItem();
                updateResults();
            }
        });
        lvBackResults.setCellFactory((ListView<Results> param) -> new BackResultsListCell());
        
        Button btnCategories = new Button();
        btnCategories.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/folder_movies_24.png"))));
        btnCategories.setTooltip(new Tooltip("Categories"));
        btnCategories.setOnAction((ActionEvent event) -> {
            
            // Dynamic programming: download categories only if it's empty
            if(categories == null || categories.isEmpty()) {
                CategoriesTask task = new CategoriesTask(DOMAIN);
                btnCancelStatus.setOnAction((ActionEvent event2) -> {
                    task.cancel();
                });
                task.setOnSucceeded((WorkerStateEvent event1) -> {
                    categories = task.getValue();
                    updateCategories();
                    border.setLeft(tvCategories);
                    tasks.remove(task);
                });
                task.setOnFailed((WorkerStateEvent event1) -> {
                    tasks.remove(task);
                    Button btnRepeat = new Button("Repeat");
                    btnRepeat.setOnAction(
                        btnCategories.getOnAction()
                    );
                    BorderPane.setMargin(btnRepeat, new Insets(0, 100.0, 0, 100.0));
                    BorderPane.setAlignment(btnRepeat, Pos.CENTER);
                    border.setLeft(btnRepeat);
                });
                task.setOnCancelled(
                    task.getOnFailed()
                );
                cancelTasks();
                ProgressIndicator pi = new ProgressIndicator(-1.0);
                //TODO: values 100.0 shouldn't be hardcoded
                // Value was jast guessed and it works
                BorderPane.setMargin(pi, new Insets(0, 100.0, 0, 100.0));
                border.setLeft(pi);
                exec.execute(task);
                tasks.add(task);
            }else {
                // Show/hide categories
                if(border.getLeft() == null || border.getLeft() == lvBackResults) {
                    border.setLeft(tvCategories);
                }else {
                    border.setLeft(null);
                }
            }
        });
        
        // History button
        btnHistory = new Button();
        btnHistory.setDisable(true);
        btnHistory.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/history_24.png"))));
        btnHistory.setTooltip(new Tooltip("History"));
        btnHistory.setOnAction((ActionEvent event) -> {
            if(border.getLeft() == null || border.getLeft() == tvCategories) {
                border.setLeft(lvBackResults);
            }else {
                border.setLeft(null);
            }
        });
        
        tvPlaylists.setOnMouseClicked((MouseEvent event) -> {
            if(event.getClickCount() == 1) {
                playlistClicked(tvPlaylists.getSelectionModel().getSelectedItem());
            }
        });
        
        tvPlaylists.setCellFactory((TreeView<PlayItem> param) -> new PlayItemTreeCell(comments));
        
        tvCategories.setOnMouseClicked((MouseEvent event) -> {
            if(event.getClickCount() == 1 &&
                    tvCategories.getSelectionModel().getSelectedItem() != null) {
                Link link = tvCategories
                        .getSelectionModel()
                        .getSelectedItem()
                        .getValue();
                Link parentLink = tvCategories
                        .getSelectionModel()
                        .getSelectedItem()
                        .getParent()
                        .getValue();
                isSavePrevResults = true;
                resultsAction(link, parentLink);
            }
        });
        
        lvActors.setOnMouseClicked((MouseEvent event) -> {
            if(event.getClickCount() == 1) {
                Link link = lvActors.getSelectionModel().getSelectedItem();
                isSavePrevResults = true;
                resultsAction(link);
            }
        });
        
        lvActors.setCellFactory((ListView<Link> param) -> new ActorListCell());
        lvActors.setMaxHeight(360.0);
        
        btnPrev = new Button();
        btnPrev.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/back_24.png"))));
        btnPrev.setTooltip(new Tooltip("Previous"));
        btnPrev.setOnAction((ActionEvent event) -> {
            isSavePrevResults = false;
            resultsAction(results.getTitle(), results.getPrevLink(), baseUrl);
        });
        
        lbPage = new Label();
        
        btnNext = new Button();
        btnNext.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/next_24.png"))));
        btnNext.setTooltip(new Tooltip("Next"));
        btnNext.setOnAction((ActionEvent event) -> {
            isSavePrevResults = false;
            resultsAction(results.getTitle(), results.getNextLink(), baseUrl);
        });
                
        btnUp = new Button();
        btnUp.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/up_24.png"))));
        btnUp.setTooltip(new Tooltip("Up results"));
        btnUp.setOnAction((ActionEvent event) -> {
            //isSavePrevResults = false;
            if(displayMode == DisplayMode.PLAYLIST) {
                updateResults();
            }
        });
        
        //ToggleGroup group = new ToggleGroup();
        
        /*btnPlay = new ToggleButton();
        btnPlay.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("player_play_24.png"))));
        btnPlay.setToggleGroup(group);
        btnPlay.setTooltip(new Tooltip("Play"));
        btnPlay.setSelected(true);
        
        btnDownload = new ToggleButton();
        btnDownload.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("download_24.png"))));
        btnDownload.setToggleGroup(group);
        btnDownload.setTooltip(new Tooltip("Download"));*/
        
        disableAllButtons();
        
        btnSavedSerials = new Button();
        btnSavedSerials.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/player_playlist_24.png"))));
        btnSavedSerials.setTooltip(new Tooltip("Saved serials"));
        btnSavedSerials.setOnAction((ActionEvent event) -> {
            savePrevResultsSimple();
            serialFilesToResults();
            updateResults();
        });
        
        btnSavedMovies = new Button();
        btnSavedMovies.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/totem-icon_24.png"))));
        btnSavedMovies.setTooltip(new Tooltip("Saved movies"));
        btnSavedMovies.setOnAction((ActionEvent event) -> {
            savePrevResultsSimple();
            moviesFilesToResults();
            updateResults();
        });
        
        btnInfo = new Button();
        btnInfo.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/info_24.png"))));
        btnInfo.setTooltip(new Tooltip("Show/hide info"));
        btnInfo.setDisable(true);
        btnInfo.setOnAction((ActionEvent event) -> {
            if(border.getRight() == null) {
                border.setRight(vbActors);
            }else {
                border.setRight(null);
            }
        });
        
        Button btnExit = new Button();
        btnExit.setGraphic(new ImageView(
                new Image(getClass().getResourceAsStream("images/logout_24.png"))));
        btnExit.setTooltip(new Tooltip("Exit"));
        btnExit.setOnAction((ActionEvent event) -> {
            Platform.exit();
        });
        
        VBox.setVgrow(tvPlaylists, Priority.ALWAYS);
        VBox.setVgrow(resultsView, Priority.ALWAYS);
        resultsView.setCellFactory(new ResultCellFactory(this));
        resultsView.setCellWidth(180);
        resultsView.setCellHeight(300);
        
        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(
                btnCategories,
                new Separator(),
                btnHistory,
                new Separator(),
                btnUp, 
                new Separator(),
                btnPrev, lbPage, btnNext,
                new Separator(),
                query,
                new Separator(),
                btnSavedSerials,
                btnSavedMovies,
                new Separator(),
                btnInfo,
                new Separator(),
                btnExit);
        
        btnCancel.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("images/close_16.png"))));
        btnCancelStatus.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("images/close_16.png"))));
        btnCancelStatus.setVisible(false);
        statusBar.getRightItems().add(btnCancelStatus);
        
        HBox hbDownload = new HBox();
        hbDownload.setAlignment(Pos.CENTER);
        hbDownload.setSpacing(5);
        hbDownload.setPadding(new Insets(2, 2, 2, 2));
        hbDownload.getChildren().addAll(lbDownload, pbDownload, btnCancel);
        vbDownload.getChildren().addAll(hbDownload, lbInfo);
        vbDownload.setPadding(new Insets(2, 2, 2, 2));
        vbDownload.setAlignment(Pos.CENTER);
        vbCenter.getChildren().add(tvPlaylists);
        
        lbActors.setMaxWidth(250);
        //lbActors.setMinHeight(40);
        lbActors.setPadding(new Insets(2, 2, 2, 2));
        lbActors.setWrapText(true);
        //VBox.setVgrow(lvActors, Priority.ALWAYS);
        lbActorsInfo.setMaxWidth(250);
        lbActorsInfo.setPadding(new Insets(2, 2, 2, 2));
        
        ListView<Actors> lvBackActors = new ListView<>(backActors);
        lvBackActors.setOnMouseClicked((MouseEvent event) -> {
            if(event.getClickCount() == 1) {
                Actors actors = lvBackActors.getSelectionModel().getSelectedItem();
                // Save currently displayed actors
                saveActors();
                updateActors(actors);
            }
        });
        lvBackActors.setCellFactory((ListView<Actors> param) -> new BackActorsListCell());
        
        vbActors.getChildren().addAll(lbActors, lbActorsInfo, lvActors, lvBackActors);
        
        border.setTop(toolbar);
        border.setCenter(vbCenter);
        border.setBottom(statusBar);
        
        Scene scene = new Scene(border, 750, 500);
        // Application icon doesn't work on Ubuntu, but sometimes works, don't know why
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("images/lamp-aladin_32.png")));
        
        appDir = new File(System.getProperty("user.home"), ".onlinelifefx");
        if(!appDir.exists()) { // Create app dir if doesn't exist
            if(!appDir.mkdir()) {
                Alert alert = new Alert(AlertType.WARNING, "Cannot create app directory!");
                alert.showAndWait();
            }
        }
        
        serialsDir = new File(appDir, "serials");
        if(!serialsDir.exists()) {
            if(!serialsDir.mkdir()) {
                Alert alert = new Alert(AlertType.WARNING, "Cannot create serials directory!");
                alert.showAndWait();
            }
        }
        
        watchedDir = new File(serialsDir, "watched");
        if(!watchedDir.exists()) {
            if(!watchedDir.mkdir()) {
                Alert alert = new Alert(AlertType.WARNING, "Cannot create 'watched' directory!");
                alert.showAndWait();
            }
        }
        
        moviesDir = new File(appDir, "movies");
        if(!moviesDir.exists()) {
            if(!moviesDir.mkdir()) {
                Alert alert = new Alert(AlertType.WARNING, "Cannot create 'watched' directory!");
                alert.showAndWait();
            }
        }
        
        updateSaveButtons();
        
        primaryStage.setTitle(PROG_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void disableAllButtons() {
        btnUp.setDisable(true);
        disableAllButUpButtons();
    }
    
    private void disableAllButUpButtons() {
        /*btnPlay.setDisable(true);
        btnDownload.setDisable(true);*/
        disableAllButUpPlayDownloadButtons();
    }
    
    private void disableAllButUpPlayDownloadButtons() {
        btnPrev.setDisable(true);
        btnNext.setDisable(true);
    }
    
    private void playlistButtons() {
        btnUp.setDisable(false);
        /*btnPlay.setDisable(false);
        btnDownload.setDisable(false);*/
        disableAllButUpPlayDownloadButtons();
    }
    
    private void cancelTasks() {
        tasks.forEach((task) -> {
            task.cancel();
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private void playlistClicked(TreeItem<PlayItem> newValue) {
        if(newValue != null && !newValue.getValue().getFile().isEmpty()) {
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Play",
                    "Play", /*"Play all",*/ "Download");
            Optional<String> response = choiceDialog.showAndWait();
            if(response.isPresent()) {
                switch(response.get()) {
                    case "Play":
                        playSerial(newValue.getValue());
                        break;
                    // Difficult to implement, cancel so far
                    /*case "Play all":
                        //TODO...
                        playAllAction(newValue);
                        break;*/
                    case "Download":
                        downloadPlayItem(newValue.getValue());
                        break;
                }
            }
            
        }
    }
    
    //TODO: fix playAll
    /*private void playAllAction(TreeItem<PlayItem> treeItem) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ObservableList<TreeItem<PlayItem>> children = 
                     treeItem.getParent().getChildren();
                boolean play = false;
                for(TreeItem<PlayItem> item : children) {
                    // Play items in season starting from clicked item
                    if(item.getValue().getComment().equals(treeItem.getValue().getComment())) {
                        play = true;
                    }
                    if(play) {
                        updateMessage("Playing: " + item.getValue().getComment());
                        System.out.println("Play: " + item.getValue().getComment());
                        //Thread.sleep(5000);
                        Process process = playSerial(treeItem.getValue());
                        process.waitFor();
                    }
                    if(isCancelled()) {
                        break;
                    }
                }
                return null;
            }
            
        };
        btnCancelStatus.setOnAction((ActionEvent event2) -> {
            task.cancel();
        });
        task.setOnSucceeded((WorkerStateEvent event1) -> {
            statusBar.textProperty().unbind();
            deactivateProgressBar("Done.");
            tasks.remove(task);
        });
        task.setOnCancelled((WorkerStateEvent event1) -> {
            statusBar.textProperty().unbind();
            deactivateProgressBar("Play all cancelled.");
            tasks.remove(task);
        });
        cancelTasks();
        activateProgressBar("Playing all...");
        exec.execute(task);
        statusBar.textProperty().bind(task.messageProperty());
        tasks.add(task);
    }*/
    
    public void resultClicked(Result result) {     
        playItemAction(result);
    }
    
    private void updateCategories() {
        rootItemCategories.getChildren().clear();
        categories.stream().map((c) -> {
            TreeItem cItem = new TreeItem(c, new ImageView(
                    new Image(getClass()
                            .getResourceAsStream("images/folder_movies_16.png"))
            ));
            c.Links.stream().map((l) -> new TreeItem(l, new ImageView(
                    new Image(getClass()
                            .getResourceAsStream("images/link_16.png"))
            ))).forEachOrdered((lItem) -> {
                cItem.getChildren().add(lItem);
            });
            return cItem;
        }).forEachOrdered((cItem) -> {
            rootItemCategories.getChildren().add(cItem);
        });
    }
    
    private void resultsActionSearch(String query) {
        try {
            baseUrl = DOMAIN + "?do=search&subaction=search&mode=simple&story="
                    + URLEncoder.encode(query, "windows-1251");
            resultsAction("Search: " + query, baseUrl, baseUrl);
        } catch (UnsupportedEncodingException e) {
            errorDialog("Encoding error!");
        }
    }
    
    private void resultsAction(Link link) {
        resultsAction(link.Title, link.Href, "");
    }
    
    private void resultsAction(Link link, Link parentLink) {
        if(!parentLink.Title.isEmpty()) {
            resultsAction(parentLink.Title + " - " + link.Title, link.Href, "");
        }else {
            resultsAction(link.Title, link.Href, "");
        }
    }
    
    private void savePrevResults() {
        if(isSavePrevResults && results != null && results.isSaveable()) {
            if(backResults.contains(results)) {
                // Update with new results
                int index = backResults.indexOf(results);
                backResults.remove(index);
                backResults.add(index, results);
            }else { // add new results
                backResults.add(results);
            }
        }
        
        // Enable history button
        if(btnHistory.isDisable() && !backResults.isEmpty()) {
            btnHistory.setDisable(false);
        }
    }
    
    private void savePrevResultsSimple() {
        if(results != null && results.isSaveable()) {
            if(!backResults.contains(results)) {
                backResults.add(results);
            }
        }
        
        // Enable history button
        if(btnHistory.isDisable() && !backResults.isEmpty()) {
            btnHistory.setDisable(false);
        }
    }
    
    private void resultsAction(String title, String link, String bUrl) {
        ResultsTask task = new ResultsTask(link, bUrl, categories == null);
        btnCancelStatus.setOnAction((ActionEvent event2) -> {
            task.cancel();
        });
        task.setOnSucceeded((WorkerStateEvent event1) -> {
            // Save previous results
            savePrevResults();
            results = task.getValue();
            results.setTitle(title);
            updateResults();
            if(results.getCategories() != null) {
                categories = results.getCategories();
                updateCategories();
            }
            border.setCenter(vbCenter);
            tasks.remove(task);
        });
        task.setOnFailed((WorkerStateEvent event1) -> {
            tasks.remove(task);
            Button btnRepeat = new Button("Repeat");
            btnRepeat.setOnAction((ActionEvent event2) -> {
                // recursive call of resultsAction method
                resultsAction(title, link, bUrl);
            });
            border.setCenter(btnRepeat);
        });
        task.setOnCancelled(task.getOnFailed());
        cancelTasks();
        ProgressIndicator pi = new ProgressIndicator(-1.0);
        pi.setMaxSize(50.0, 50.0);
        border.setCenter(pi);
        exec.execute(task);
        tasks.add(task);
    }
    
    public void updateResults() {
        if(results == null) 
            return;
        displayMode = DisplayMode.RESULTS;
        
        btnUp.setDisable(true);
        /*btnPlay.setDisable(false);
        btnDownload.setDisable(false);*/
        
        window.setTitle(PROG_NAME + " - " + results.getPageTitle());
        
        resultsView.getItems().clear();
        resultsView.getItems().addAll(results.getItems());
        
        // Replace treeView with GridView
        if(vbCenter.getChildren().contains(tvPlaylists)) {
            vbCenter.getChildren().remove(tvPlaylists);
            vbCenter.getChildren().add(0, resultsView);
        }

        if(results.getPrevLink() != null && !results.getPrevLink().isEmpty()) {
            btnPrev.setDisable(false);
        }else {
            btnPrev.setDisable(true);
        }
        
        if(!results.getCurrentPage().isEmpty()) {
            lbPage.setText(results.getCurrentPage());
        }else {
            lbPage.setText("");
        }

        if(results.getNextLink() != null && !results.getNextLink().isEmpty()) {
            btnNext.setDisable(false);
        }else {
            btnNext.setDisable(true);
        }
    }
    
    private void updateSaveButtons() {
        if(serialsDir.listFiles().length == 1) {
            btnSavedSerials.setDisable(true);
        }else {
            btnSavedSerials.setDisable(false);
        }
        
        if(moviesDir.listFiles().length == 0) {
            btnSavedMovies.setDisable(true);
        }else {
            btnSavedMovies.setDisable(false);
        }
    }
    
    private void saveMovie(Result result) {
        resultToFile(result, moviesDir);
        updateSaveButtons();
    }
    
    private void deleteMovie(Result result) {
        File delFile = new File(moviesDir, result.Id);
        if(delFile.exists()) {
            delFile.delete();
            updateSaveButtons();
            // Update list
            moviesFilesToResults();
            updateResults();
        }
    }
    
    private void saveSerial(Result result) {
        resultToFile(result, serialsDir);
        updateSaveButtons();
    }
    
    private void deleteSerial(Result result) {
        // Delete watched parts list
        File delFile = new File(watchedDir, result.Id);
        if(delFile.exists()) {
            delFile.delete();
        }
        // Delete serial file itself
        delFile = new File(serialsDir, result.Id);
        if(delFile.exists()) {
            delFile.delete();
            updateSaveButtons();
            // Update list
            serialFilesToResults();
            updateResults();
        }
    }
    
    private void playItemAction(Result result) {
        PlayItemTask task = new PlayItemTask(result);
        btnCancelStatus.setOnAction((ActionEvent event) -> {
            task.cancel();
        });
        task.setOnSucceeded((WorkerStateEvent event2) -> {
            deactivateProgressBar("Done.");
            tasks.remove(task);
            PlayItem playItem = task.getValue();
            String bookmark;
            if(results.isSaveable()) { // For not saved serials
                bookmark = "Bookmark";
            }else { // For saved serials
                bookmark = "Delete";
            }
            
            ChoiceDialog<String> choiceDialog;
            
            if(playItem.isJsPlayItem()) { // Movie or trailer found
                choiceDialog = new ChoiceDialog<>("Play", 
                bookmark, "Play", "Download", "Actors");
            }else { // Serial found
                choiceDialog = new ChoiceDialog<>("Open", 
                bookmark, "Open", "Actors");
            }
            
            Optional<String> responce = choiceDialog.showAndWait();
            //TODO: this is bad for localization, this should be made better
            //TODO: Change it to Command design pattern
            if(responce.isPresent()) {
                switch(responce.get()) {
                    case "Bookmark":
                        if(playItem.isJsPlayItem()) { // Movie
                            saveMovie(result);
                        }else { // Serial
                            saveSerial(result);
                        }
                        break;
                    case "Delete":
                        if(playItem.isJsPlayItem()) { // Movie
                            deleteMovie(result);
                        }else { // Serial
                            deleteSerial(result);
                        }
                        break;
                    case "Play":
                        playMovie(playItem);
                        break;
                    case "Download":
                        downloadPlayItem(playItem);
                        break;
                    case "Open":
                        playlistsAction(result, playItem.getJs());
                        break;
                    case "Actors":
                        actorsAction(result);
                        break;
                }
            }
        });
        task.setOnFailed((WorkerStateEvent event1) -> {
            deactivateProgressBar("Error");
            tasks.remove(task);
            errorDialog(task.getException().toString());
        });
        task.setOnCancelled((WorkerStateEvent event1) -> {
            deactivateProgressBar("PlayItem cancelled.");
            tasks.remove(task);
        });
        cancelTasks();
        activateProgressBar("Getting playitem...");
        exec.execute(task);
        tasks.add(task);
    }
    
    private void playlistsAction(Result result, String js) {
        PlaylistTask task = new PlaylistTask(js);
        btnCancelStatus.setOnAction((ActionEvent event2) -> {
            task.cancel();
        });
        task.setOnSucceeded((WorkerStateEvent event1) -> {
            Playlists playlists = task.getValue();
            if(playlists != null) {
                playlists.setTitle(result.Title);
                /*if(playlists.getPlayItem() != null) {
                    processPlayItem(playlists.getPlayItem());
                }else {
                    parent = result;
                    updatePlaylists(playlists);
                }*/
                parent = result;
                updatePlaylists(playlists);
                border.setCenter(vbCenter);
                tasks.remove(task);
            }else {
                tasks.remove(task);
                errorDialog("This link is not supported!");
            }
        });
        task.setOnFailed((WorkerStateEvent event1) -> {
            Button btnRepeat = new Button("Repeat");
            btnRepeat.setOnAction((ActionEvent event2) -> {
                // recursive call of playlistsAction method
                playlistsAction(result, js);
            });
            border.setCenter(btnRepeat);
            tasks.remove(task);
        });
        task.setOnCancelled(
            task.getOnFailed()
        );
        cancelTasks();
        ProgressIndicator pi = new ProgressIndicator(-1.0);
        pi.setMaxSize(50.0, 50.0);
        border.setCenter(pi);
        exec.execute(task);
        tasks.add(task);
    }
    
    private void updatePlaylists(Playlists playlists) {
        displayMode = DisplayMode.PLAYLIST;
        lbPage.setText("");
        
        playlistButtons();
        
        window.setTitle(PROG_NAME + " - " + playlists.getTitle());
        
        // Read comments (watched episodes) from file
        File saveFile = new File(watchedDir, parent.Id);                
        if(saveFile.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(saveFile))) {
                String s;
                while((s = in.readLine()) != null) {
                    comments.add(s);
                }
            } catch (IOException ex) {
                Logger.getLogger(OnlinelifeFX.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(playlists.playlist.size() == 1) {
            rootItem.getChildren().clear();
            playlists.playlist.get(0).playlist.forEach((playItem) -> {
                rootItem.getChildren().add(new TreeItem(playItem, new ImageView(
                        new Image(getClass().getResourceAsStream("images/totem-icon_16.png")))));
            });
        }else { // Display multiple playlists
            rootItem.getChildren().clear();
            playlists.playlist.stream().map((playlist) -> {
                PlayItem fakePlayItem = new PlayItem();
                fakePlayItem.setComment(playlist.comment);
                TreeItem<PlayItem> treeItem = new TreeItem<>(fakePlayItem, new ImageView(
                        new Image(getClass().getResourceAsStream("images/player_playlist_16.png"))));
                playlist.playlist.forEach((playItem) -> {
                    treeItem.getChildren().add(new TreeItem<>(playItem, new ImageView(
                            new Image(getClass().getResourceAsStream("images/totem-icon_16.png")))));
                });
                return treeItem;
            }).forEachOrdered((treeItem) -> {
                rootItem.getChildren().add(treeItem);
            });
        }
        
        replaceResultsToTreeView();
    }
    
    public void actorsAction(Result result) {
        btnInfo.setDisable(false);
        // Do not reload currently displayed actors
        if(lbActors.getText().contains(result.Title)) {
            return;
        }
        // Do not reload saved actors. Linear search
        for(Actors actors : backActors) {
            if(actors.getTitle().contains(result.Title)) {
                // Save currently displayed actors
                saveActors();
                updateActors(actors);
                return;
            }
        }
        
        ActorsTask task = new ActorsTask(result.Href);
        btnCancelStatus.setOnAction((ActionEvent event2) -> {
            task.cancel();
        });
        task.setOnSucceeded((WorkerStateEvent event1) -> {
            Actors actors = task.getValue();
            actors.setTitle(result.Title);
            saveActors();
            updateActors(actors);
            border.setRight(vbActors);
            tasks.remove(task);
        });
        task.setOnFailed((WorkerStateEvent event1) -> {
            Button btnRepeat = new Button("Repeat");
            btnRepeat.setOnAction((ActionEvent event2) ->{
                actorsAction(result); //recursive call
            });
            BorderPane.setMargin(btnRepeat, new Insets(0, 100.0, 0, 100.0));
            BorderPane.setAlignment(btnRepeat, Pos.CENTER);
            border.setRight(btnRepeat);
            tasks.remove(task);
        });
        task.setOnCancelled(
            task.getOnFailed()
        );
        cancelTasks();
        ProgressIndicator pi = new ProgressIndicator(-1.0);
        pi.setMaxSize(50.0, 50.0);
        //TODO: values 100.0 shouldn't be hardcoded
        // Value was jast guessed and it works
        BorderPane.setAlignment(pi, Pos.CENTER);
        BorderPane.setMargin(pi, new Insets(0, 100.0, 0, 100.0));
        border.setRight(pi);
        exec.execute(task);
        tasks.add(task);
    }
    
    private void updateActors(Actors actors) {
        if(actors != null) {
            lbActors.setText(actors.getTitle());
            lbActorsInfo.setText(actors.getInfo());
            lvActors.setItems(actors.getItems());
        }
    }
    
    private void saveActors() {
        if(!lvActors.getItems().isEmpty()) {
            // Save previous actors
            Actors prevActors = new Actors(lbActors.getText(),
                    lbActorsInfo.getText(), lvActors.getItems());
            if(!backActors.contains(prevActors)) {
                backActors.add(prevActors);
            }
        }
    }
    
    private void resultToFile(Result result, File saveDir) {
        File saveFile = new File(saveDir, result.Id);
        // Do not save if file exists
        if(saveFile.exists()) {
            return;
        }
        try(
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(result);
        }catch(Exception e) {
            errorDialog(e.toString());
        }
        // TODO: rewrite all other where possible to try-finally like here...
    }
    
    private void filesToResults(File saveDir) {
        results = new Results();
        results.setSaveable(false); // Do not save this list with others
        for(File saveFile : saveDir.listFiles()) {
            // Deserialize file
            if(saveFile.isFile()) {
                try (
                    FileInputStream fis = new FileInputStream(saveFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                ) {
                    Result result = (Result)ois.readObject();
                    results.add(result);
                }catch(Exception e) {
                    errorDialog("Serials list: " + e.toString());
                }
            }
        }
    }
    
    private void serialFilesToResults() {
        filesToResults(serialsDir);
        results.setTitle("Saved serials");
    }
    
    private void moviesFilesToResults() {
        filesToResults(moviesDir);
        results.setTitle("Saved movies");
    }
    
    private void playMovie(PlayItem playItem) {
        PlayerTask task = new PlayerTask(playItem.getFile());
        task.setOnFailed((WorkerStateEvent event1) -> {
            errorDialog("Player error!");
        });
        exec.execute(task);
    }
    
    private void playSerial(PlayItem playItem) {
        if(appDir.exists() && appDir.canWrite()) {// is this check needed ?

            saveSerial(parent); // To have a list of watched serials
            // Mark watched serial part and save the mark
            File saveFile = new File(watchedDir, parent.Id);
            // Add new comment to save file  
            if(!comments.contains(playItem.getComment())) {
                try (PrintWriter out = new PrintWriter(
                    new BufferedWriter(new FileWriter(saveFile, saveFile.exists())))) {
                    out.println(playItem.getComment());  
                    comments.add(playItem.getComment()); // add to comments for refresh to work
                } catch (IOException ex) {
                    errorDialog(ex.toString());
                }
                tvPlaylists.refresh();
            }
        }
        playMovie(playItem);
    }
    
    private void downloadPlayItem(PlayItem playItem) {
        //TODO: select download dir and make it a property
        File videoDir = new File(System.getProperty("user.home"), "Видео");
        if(!videoDir.exists()) {
            if(!videoDir.mkdir()) {
                Alert alert = new Alert(AlertType.WARNING, "Cannot create Video directory!");
                alert.showAndWait();
                return;
            }
        }
        File saveFile = new File(videoDir, playItem.getComment() + ".mp4");

        //TODO: warning about existing file?

        if(vbCenter.getChildren().contains(vbDownload)) {
            statusBar.setText("");
            Alert alert = new Alert(AlertType.WARNING, "Downloader is busy!");
            alert.showAndWait();
            return;
        }

        vbCenter.getChildren().add(1, vbDownload);
        DownloadTask task = new DownloadTask(playItem.getDownload(), saveFile);
        pbDownload.progressProperty().bind(task.progressProperty());
        lbInfo.textProperty().bind(task.messageProperty());
        btnCancel.setOnAction((ActionEvent event) -> {
            if(task.isRunning()) {
                task.cancel();
            }else {
                vbCenter.getChildren().remove(vbDownload);
            }
        });
        task.setOnRunning((WorkerStateEvent event) -> {
            lbDownload.setText(playItem.getComment()); 
        });
        task.setOnCancelled((WorkerStateEvent event) -> {
            vbCenter.getChildren().remove(vbDownload);
            pbDownload.progressProperty().unbind();
            pbDownload.setProgress(0);
        });
        task.setOnSucceeded((WorkerStateEvent event1) -> {
            lbDownload.setText("Done.");
            pbDownload.progressProperty().unbind();
            pbDownload.setProgress(0);
        });
        task.setOnFailed((WorkerStateEvent event1) -> {
            lbDownload.setText(task.getException().toString());
            pbDownload.progressProperty().unbind();
            pbDownload.setProgress(0);
        });
        exec.execute(task);
    }
    
    public void errorDialog(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.showAndWait();
    }
    
    private void replaceResultsToTreeView() {
        // Replace resultsView with treeView 
        if(vbCenter.getChildren().contains(resultsView)) {
            vbCenter.getChildren().remove(resultsView);
            vbCenter.getChildren().add(0, tvPlaylists);
        }
    }
    
    private void activateProgressBar(String text) {
        statusBar.setText(text);
        statusBar.setProgress(-1);
        btnCancelStatus.setVisible(true);
    }
    
    private void deactivateProgressBar(String text) {
        statusBar.setText(text);
        statusBar.setProgress(0);
        btnCancelStatus.setVisible(false);
    }

}
