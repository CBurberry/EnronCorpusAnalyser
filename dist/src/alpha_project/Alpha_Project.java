/*
UI BETA VERSION 0.9.4

 */
package alpha_project;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Christian
*/
public class Alpha_Project extends Application {
    private Stage stage;
    private final String css1 = getClass().getResource("Alpha_Stylesheet1.css").toExternalForm();
    private final String css2 = getClass().getResource("Alpha_Stylesheet2.css").toExternalForm();
    private Controller controller = new Controller();
    private updateBoolean isUpdated = new updateBoolean();
    //^ required, otherwise css can't be loaded more than once
    private TabPane tabpane;
    SingleSelectionModel<Tab> selectionModel;
    
    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        
        Backend.runMain(new String[3]);
        
        stage = primaryStage;
        stage.setOnCloseRequest((event) -> Platform.exit());
        Scene scene = welcomeScene();
        /*primaryStage.setTitle("Enron Corpus Analytics");
        primaryStage.setScene(scene);
        primaryStage.show();*/
        new LoadingStage(stage, testScene());
    }
    
    public Scene testScene() {
        TabPane tb = new TabPane();
        Tab tab1 = new Tab("Welcome Screen");
        tab1.setContent(welcomeScene().getRoot());
        Tab tab2 = new Tab("View Employees");
        tab2.setContent(employeeScene().getRoot());
        Tab tab3 = new Tab("View Groups");
        tab3.setContent(groupScene().getRoot());
        Tab tab4 = new Tab("View Rankings");
        tab4.setContent(rankScene().getRoot());
        Tab tab5 = new Tab("About & Help");
        tab5.setContent(aboutScene().getRoot());
        
        tb.getTabs().addAll(tab1, tab2, tab3, tab4, tab5);
        tb.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        this.tabpane = tb;
        this.selectionModel = tb.getSelectionModel();
        
        Scene scene = new Scene(tb, 1360, 700);
        scene.getStylesheets().add(css2);
        stage.setTitle("Enron Corpus Analytics");
        /*stage.setScene(scene);
        stage.show();*/
        return scene;
    }
    
    private void graphStage(Efactfile emp, Group grp, Stage stage) {                //THIS METHOD SHOULD NOT BE USED
        String windowID = "FFWindow" + Integer.toString(controller.size());
        Date min = controller.getDateFrom(0);
        Date max = controller.getDateTo(0);
        controller.addPair(windowID, min, max); 
        int winID = controller.size()-1;
        String name;
        if (emp == null) {
            controller.assignGroup(grp, winID);
            name = grp.name;
           
        } else {
            controller.assignEmployee(emp, winID);
            name = emp.employee;
        }
        
        ScrollPane unused = new ScrollPane();   //Just to pass an empty parameter where null is not permitted. It should not be displayed.
        
        Scene scene = new Scene(graphScene(emp, grp, winID, stage, unused), 680, 690);
        scene.getStylesheets().add(css2);   
        stage.setResizable(false);
        
        stage.setTitle("Line Chart Viewer: " + name);
        stage.setScene(scene);
        stage.show();
    }
    
    private BorderPane graphScene(Efactfile emp, Group grp, int winID, Stage stage, ScrollPane parent) {
        //callee of graph scene can only be from a factfile
        BorderPane root = new BorderPane();
        VBox content;
        if (grp==null){
         content = employeeLineChart(emp, winID, stage, parent);
        } else {
         content = groupLineChart(grp, winID, stage, parent);
        }
        root.setCenter(content);
        if (winID != 0) {
            if (emp == null) {
                root.setTop(generateMiniBar(grp, stage));
            }
            else {
                root.setTop(generateMiniBar(emp, stage));
            }
        }
        
        return root;
    }
    
    private VBox employeeLineChart(Efactfile emp, int winID, Stage stage, ScrollPane parent) {
        VBox container = new VBox();
        
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
        
        lineChart.setTitle("Message rate of " + emp.employee);
        XYChart.Series series = new XYChart.Series();
        series.setName("Sent Emails");
        
        graphData[] graphdata = emp.getGraphData();
        
        for ( graphData point : graphdata){
             series.getData().add(new XYChart.Data(point.day, point.count));
        }
        //no
        
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);
        
        Button viewFF = new Button("View FactFile");
        viewFF.setOnAction((event) -> {
           if (winID != 0) {
               openFFWindow(emp, winID);
               stage.hide();
           }
           else {
               parent.setContent(getEFFWindowContent(emp, stage, winID, parent));
           }
        });
        
        Text desc = new Text("Currently Displaying for: " + controller.getDateFrom(winID).toString() + "\n\t\t\t\t   to: " + controller.getDateTo(winID).toString());
        desc.setFont(new Font(18));
        
        viewFF.getStyleClass().add("main-button");
        VBox.setMargin(lineChart, new Insets(30, 0, 0, 0));
        VBox.setMargin(viewFF, new Insets(80, 0, 0, 550));
        VBox.setMargin(desc, new Insets(50, 0, 0, 50));
        //StackPane also has a static void setMargin method
        container.getChildren().addAll(lineChart, desc, viewFF);
        return container;
    }
        
    private VBox groupLineChart(Group grp, int winID, Stage stage, ScrollPane parent) {
        VBox container = new VBox();
        
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
        
        lineChart.setTitle("Message rate of Group " + grp.name);
        XYChart.Series series = new XYChart.Series();
        series.setName("Sent Emails");
        
        graphData[] graphdata = grp.getGraphData();
        
        for ( graphData point : graphdata){
             series.getData().add(new XYChart.Data(point.day, point.count));
        }
        //no
        
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);
        
        Button viewFF = new Button("View FactFile");
        viewFF.setOnAction((event) -> {
           if (winID != 0) {
               openFFWindow(grp, winID);
               stage.hide();
           }
           else {
               parent.setContent(getGFFWindowContent(grp, stage, winID, parent));
           }
        });
        Text desc = new Text("Currently Displaying for: " + controller.getDateFrom(winID).toString() + "\n\t\t\t\t   to: " + controller.getDateTo(winID).toString());
        desc.setFont(new Font(18));
        
        viewFF.getStyleClass().add("main-button");
        VBox.setMargin(lineChart, new Insets(30, 0, 0, 0));
        VBox.setMargin(viewFF, new Insets(80, 0, 0, 550));
        VBox.setMargin(desc, new Insets(50, 0, 0, 50));
        //StackPane also has a static void setMargin method
        container.getChildren().addAll(lineChart, desc, viewFF);
        return container;
    }
    
        private boolean periodScene(int winID, Button button) {
        button.setDisable(true);
        if (winID != 0) {
            button.getParent().getParent().getParent().getParent().getParent().getScene().getWindow().setOnCloseRequest((ae) -> {
                ae.consume();
            });
        }
        Stage miniStage = new Stage();
        miniStage.setResizable(false);
            miniStage.setOnCloseRequest((event2) -> {
            isUpdated.setUpdated(false);
            isUpdated.setCancelled(true);
            button.setDisable(false);
            miniStage.hide();
        });
        VBox root = new VBox();
        Scene scene = new Scene(root, 500, 150);
        scene.getStylesheets().add(css2);
        GridPane gpDates = new GridPane();
        GridPane gpButtons = new GridPane();
        gpDates.setVgap(10);
        gpDates.setHgap(10);
        Label label = new Label("Please select the timeframe for data filtering:");
        label.setFont(new Font(16));
        
        Line orange = new Line(0, 40, 510, 40);
        orange.setStrokeWidth(5);
        orange.setStroke(Color.ORANGE);
        
        Line rTop = new Line(10, 60, 490, 60);
        Line rBottom = new Line(0, 170, 490, 170);
        Line rLeft = new Line(10, 40, 10, 170);
        Line rRight = new Line(490, 40, 490, 170);
        
        ColumnConstraints cc1 = new ColumnConstraints(250);
        ColumnConstraints cc2 = new ColumnConstraints(250);
        
        gpDates.getColumnConstraints().addAll(cc1, cc2);
        
        
        Date date1 = controller.getDateFrom(winID);
        Date date2 = controller.getDateTo(winID);
        
        DatePicker fromSelect = new DatePicker(Instant.ofEpochMilli(date1.getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
        gpDates.add(fromSelect, 0, 0);
        
        DatePicker toSelect = new DatePicker(Instant.ofEpochMilli(date2.getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
        gpDates.add(toSelect, 1, 0);
        
        gpDates.setHalignment(fromSelect, HPos.CENTER);
        gpDates.setHalignment(toSelect, HPos.CENTER);
        
        
        
        fromSelect.setConverter(new StringConverter<LocalDate>()
        {
            private DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });
        
        toSelect.setConverter(new StringConverter<LocalDate>()
        {
            private DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });
        
        final String[] dateSelect = new String[2];
        Button submit = new Button("Submit");
        submit.getStyleClass().add("main-button");
        submit.setMinSize(Button.USE_PREF_SIZE , Button.USE_PREF_SIZE);
        submit.setOnAction((event) -> {          
            LocalDate fromdate = fromSelect.getValue();
            LocalDate todate = toSelect.getValue();
            controller.setDateFrom(new Date(fromdate.getYear()-1900, fromdate.getMonthValue()-1, fromdate.getDayOfMonth()), winID);
            controller.setDateTo(new Date(todate.getYear()-1900, todate.getMonthValue()-1, todate.getDayOfMonth()), winID);
            
            button.setDisable(false);
            isUpdated.setUpdated(true);
            isUpdated.setCancelled(false);
            miniStage.hide();
            //Perhaps a return statement to caller window?
        });
        
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("main-button");
        cancel.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        cancel.setOnAction((event) -> {
            button.setDisable(false);
            isUpdated.setUpdated(false);
            isUpdated.setCancelled(true);
            miniStage.hide();
            //add a return here if method returns something to caller.
        });
        gpButtons.add(submit, 0, 0);
        gpButtons.add(cancel, 1, 0);
        gpButtons.setHgap(30);
        
        final Callback<DatePicker, DateCell> dayCellFactory = 
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                            public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                            
                            if (item.isBefore(LocalDate.of(1997, 01, 01)) || item.isAfter(LocalDate.of(2002, 06, 22))){
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                            }   
                            }
                };
            }
        };
        fromSelect.setDayCellFactory(dayCellFactory);
        toSelect.setDayCellFactory(dayCellFactory);
        
        /*gpDates.setGridLinesVisible(true);
        gpButtons.setGridLinesVisible(true);*/
        root.getChildren().addAll(label, orange, gpDates, gpButtons);
        
        VBox.setMargin(label, new Insets(10, 0, 0, 10));
        VBox.setMargin(gpDates, new Insets(25, 0, 0, 0));
        VBox.setMargin(gpButtons, new Insets(25, 0, 0, 350));
        
        miniStage.setTitle("Period Selector Window");
        miniStage.setScene(scene);
        miniStage.showAndWait();
        
        boolean successStatus = isUpdated.getUpdated();
        System.out.println(Boolean.toString(successStatus));
        
        if (winID != 0) {
            button.getParent().getParent().getParent().getParent().getParent().getScene().getWindow().setOnCloseRequest((ae) -> {
                button.getParent().getParent().getParent().getParent().getParent().getScene().getWindow().hide();
            });
        }
        
        return successStatus;
    }
    
    private final MenuBar generateMiniBar(Efactfile emp, Stage window) {
        MenuBar miniBar = new MenuBar();
        Menu menu = new Menu("File");
        MenuItem dupe = new MenuItem("Open Duplicate Window");
        dupe.setOnAction(ae -> {
            openFFWindow(emp);
        });
        MenuItem close = new MenuItem("Close");
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                window.hide();
            }
        }); 
        
        menu.getItems().addAll(dupe, close);
        miniBar.getMenus().add(menu);
        
        return miniBar;
    }
    
    private final MenuBar generateMiniBar(Group grp, Stage window) {
        MenuBar miniBar = new MenuBar();
        Menu menu = new Menu("File");
        MenuItem dupe = new MenuItem("Open Duplicate Window");
        dupe.setOnAction(ae -> {
            openFFWindow(grp);
        });
        MenuItem close = new MenuItem("Close");
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                window.hide();
            }
        }); 
        
        menu.getItems().addAll(dupe, close);
        miniBar.getMenus().add(menu);
        
        return miniBar;
    }
    
    private final MenuBar generateMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Menu");
        MenuItem welcomeItem = new MenuItem("Welcome Screen");
        welcomeItem.setOnAction(ae -> {
            stage.setScene(welcomeScene());   
        });
        MenuItem employeeItem = new MenuItem("View Employees");
        employeeItem.setOnAction(ae -> {
            stage.setScene(employeeScene());
        });
        MenuItem groupItem = new MenuItem("View Groups");
        groupItem.setOnAction(ae -> {
            stage.setScene(groupScene());
        });
        MenuItem rankItem = new MenuItem("View Rankings");
        rankItem.setOnAction(ae -> {
            stage.setScene(rankScene());
        });
        MenuItem aboutItem = new MenuItem("About & Help");
        aboutItem.setOnAction(ae -> {
            stage.setScene(aboutScene());
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                Platform.exit();
            }
        }); 
       
        menu.getItems().addAll(
                welcomeItem,
                employeeItem,
                groupItem,
                rankItem,
                aboutItem,
                exitItem
        );
        
 
        
        menuBar.getMenus().addAll(menu);

        return menuBar;
    }
    
    private Scene miniFFUpdate(Efactfile emp, Stage stage, int winID) {
        Efactfile updatedEmp = new Efactfile(emp.employee, controller.getDateFrom(winID), controller.getDateTo(winID), emp.participating);
        controller.assignEmployee(updatedEmp, winID);
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(700);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(700);
        content.setPannable(false);
        
        Scene scene = new Scene(root, 690, 700);
        scene.getStylesheets().add(css2);  
        
        Line rTop = new Line(30, 90, 680, 90);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(30, 90, 30, 690);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(680, 90, 680, 690);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(30, 690, 680, 690);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(5, 0, 5, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox layoutFix = new VBox();
        VBox factFile = getEFactFile(updatedEmp, stage, winID, content);
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setTop(generateMiniBar(emp, stage));
        root.setCenter(layoutFix);
        
        return scene;
    }
    
    private Scene miniFFUpdate(Group grp, Stage stage, int winID) {
        Group updatedGrp = new Group(grp, controller.getDateFrom(winID), controller.getDateTo(winID));
        controller.assignGroup(updatedGrp, winID);
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(700);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(700);
        content.setPannable(false);
        
        Scene scene = new Scene(root, 690, 700);
        scene.getStylesheets().add(css2);  
        
        Line rTop = new Line(30, 90, 680, 90);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(30, 90, 30, 690);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(680, 90, 680, 690);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(30, 690, 680, 690);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(5, 0, 5, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox layoutFix = new VBox();
        VBox factFile = getGFactFile(updatedGrp, stage, winID, content);
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setTop(generateMiniBar(grp, stage));
        root.setCenter(layoutFix);
        
        return scene;
    }
    
    private void openFFWindow(Efactfile emp, int winID) {
        //System.out.println("openFFWindow called");
        Stage newWindow = new Stage();
        newWindow.setResizable(false);     
        
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(700);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(700);
        content.setPannable(false);
        
        Scene scene = new Scene(root, 680, 690);
        scene.getStylesheets().add(css2);        
        
        Line rTop = new Line(30, 90, 680, 90);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(30, 90, 30, 690);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(680, 90, 680, 690);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(30, 690, 680, 690);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(5, 0, 5, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox layoutFix = new VBox();
        VBox factFile = getEFactFile(emp, newWindow, winID, content);
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setTop(generateMiniBar(emp, newWindow));
        root.setCenter(layoutFix);
        
        
        newWindow.setTitle("FactFile: " + emp.employee);
        newWindow.setScene(scene);
        newWindow.show();
    }
    
    private void openFFWindow(Group grp, int winID) {
        //System.out.println("openFFWindow called");
        Stage newWindow = new Stage();
        newWindow.setResizable(false);     
        
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(700);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(700);
        content.setPannable(false);
        
        Scene scene = new Scene(root, 680, 690);
        scene.getStylesheets().add(css2);        
        
        Line rTop = new Line(30, 90, 680, 90);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(30, 90, 30, 690);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(680, 90, 680, 690);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(30, 690, 680, 690);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(5, 0, 5, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox layoutFix = new VBox();
        VBox factFile = getGFactFile(grp, newWindow, winID, content);
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setTop(generateMiniBar(grp, newWindow));
        root.setCenter(layoutFix);
        
        
        newWindow.setTitle("FactFile: " + grp.name);
        newWindow.setScene(scene);
        newWindow.show();
    }    
    
    private BorderPane getDefaultFFContent() {
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(680);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(680);
        content.setPannable(false);
        
        Line rTop = new Line(29, 64, 679, 64);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(29, 64, 29, 664);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(679, 64, 679, 664);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(29, 664, 679, 664);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(9, 0, 9, 680);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox wrapper = new VBox();
        VBox layoutFix = new VBox();
        Label titleLabel = new Label("Fact File Viewer");
        titleLabel.setFont(new Font(27));
        titleLabel.setUnderline(true);
        Label emptyLabel = new Label("Please select an Item");
        emptyLabel.setFont(new Font(25));
        
        wrapper.getChildren().add(layoutFix);
        VBox.setMargin(layoutFix, new Insets(0, 0, 0, 0));          //unused
        layoutFix.getChildren().addAll(titleLabel, emptyLabel);
        VBox.setMargin(titleLabel, new Insets(10, 0, 0, 30));
        VBox.setMargin(emptyLabel, new Insets(250,0,0,200));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setCenter(layoutFix);
        
        return root;
    }
    
    private BorderPane getEFFWindowContent(Efactfile emp, Stage stage, int winID, ScrollPane parent) {
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(680);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(680);
        content.setPannable(false);
        
        Line rTop = new Line(29, 64, 679, 64);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(29, 64, 29, 664);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(679, 64, 679, 664);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(29, 664, 679, 664);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(9, 0, 9, 680);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox wrapper = new VBox();
        VBox layoutFix = new VBox();
        wrapper.getChildren().add(layoutFix);
        VBox.setMargin(layoutFix, new Insets(10, 0, 0, 1));  
        VBox factFile = getEFactFile(emp, stage, winID, parent);    //this method is only for main window.
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setCenter(layoutFix);
        
        return root;
    }
    
    private BorderPane getGFFWindowContent(Group grp, Stage stage, int winID, ScrollPane parent) {
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(680);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(680);
        content.setPannable(false);
        
        Line rTop = new Line(29, 64, 679, 64);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(29, 64, 29, 664);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(679, 64, 679, 664);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(29, 664, 679, 664);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(9, 0, 9, 680);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox wrapper = new VBox();
        VBox layoutFix = new VBox();
        wrapper.getChildren().add(layoutFix);
        VBox.setMargin(layoutFix, new Insets(10, 0, 0, 1));  
        VBox factFile = getGFactFile(grp, stage, winID, parent);    //this method is only for main window.
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 0));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setCenter(layoutFix);
        
        return root;
    }
    
    private void openFFWindow(Efactfile emp) {
        //System.out.println("openFFWindow called");
        Stage newWindow = new Stage();
        newWindow.setResizable(false);
        String windowID = "FFWindow" + Integer.toString(controller.size());
        Date min;
        Date max;
        if (emp.dateFrom == null) {
            min = new Date(1997-1900, 01-1, 01);
            max = new Date(2002-1900, 06-1, 22);
        }
        else {
            min = emp.dateFrom;
            max = emp.dateTo;
        }
        controller.addPair(windowID, min, max);
        int winID = controller.size()-1;
        controller.assignEmployee(emp, winID);
        //need to know who calls the new window ?
        //^ as extension perhaps - for now each new window is by default period when opened.
        
        
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(700);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(700);
        content.setPannable(false);
        
        Scene scene = new Scene(root, 680, 690);
        scene.getStylesheets().add(css2);        
        
        Line rTop = new Line(30, 90, 680, 90);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(30, 90, 30, 690);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(680, 90, 680, 690);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(30, 690, 680, 690);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(5, 0, 5, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox layoutFix = new VBox();
        VBox factFile = getEFactFile(emp, newWindow, winID, content);
        layoutFix.getChildren().add(factFile);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setTop(generateMiniBar(emp, newWindow));
        root.setCenter(layoutFix);
        
        
        newWindow.setTitle("FactFile: " + emp.employee);
        newWindow.setScene(scene);
        newWindow.show();
    }
    
    private void openFFWindow(Group grp) {
        //System.out.println("openFFWindow called");
        Stage newWindow = new Stage();
        newWindow.setResizable(false);      
        String windowID = "FFWindow" + Integer.toString(controller.size());
        Date min;
        Date max;
        if (grp.fromDate == null) {
            min = new Date(1997-1900, 01-1, 01);
            max = new Date(2002-1900, 06-1, 22);
        }
        else {
            min = grp.fromDate;
            max = grp.toDate;
        }
        controller.addPair(windowID, min, max);
        int winID = controller.size()-1;
        controller.assignGroup(grp, winID);
        //need to know who calls the new window ?
        //^ as extension perhaps - for now each new window is by default period when opened.
        
        
        BorderPane root = new BorderPane();
        root.setPrefWidth(680);
        root.setPrefHeight(700);
        ScrollPane content = new ScrollPane();
        content.setPrefWidth(680);
        content.setPrefHeight(700);
        content.setPannable(false);
        
        Scene scene = new Scene(root, 680, 690);
        scene.getStylesheets().add(css2);        
        
        Line rTop = new Line(30, 90, 680, 90);    //top line
        rTop.setStrokeWidth(5);
        rTop.setStroke(Color.LIGHTBLUE);
        Line rLeft = new Line(30, 90, 30, 690);    //left line
        rLeft.setStrokeWidth(5);
        rLeft.setStroke(Color.LIGHTBLUE);
        Line rRight = new Line(680, 90, 680, 690);    //right line
        rRight.setStrokeWidth(5);
        rRight.setStroke(Color.LIGHTBLUE);
        Line rBottom = new Line(30, 690, 680, 690);    //bottom line
        rBottom.setStrokeWidth(5);
        rBottom.setStroke(Color.LIGHTBLUE);
        Line splitLine = new Line(5, 0, 5, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        
        VBox layoutFix =  new VBox();
        VBox factFile = getGFactFile(grp, newWindow, winID, content);
        VBox.setMargin(factFile, new Insets(0, 30, 0, 10));
        
        layoutFix.getChildren().add(factFile);
        root.getChildren().addAll(rTop, rLeft, rBottom, rRight, splitLine);
        root.setTop(generateMiniBar(grp, newWindow));
        root.setCenter(layoutFix);
        
        
        newWindow.setTitle("FactFile: Group" + grp.name);
        newWindow.setScene(scene);
        newWindow.show();
    }
    
    private VBox getEFactFile(Efactfile emp, Stage stage, int winID, ScrollPane parent) {
        VBox layoutFix = new VBox();
        VBox container = new VBox();
        VBox internal = new VBox(); //internal VBox for layout
        GridPane internalTop = new GridPane();
        VBox.setMargin(internal, new Insets(10,0,0,15));
        ColumnConstraints column1 = new ColumnConstraints(400);
        ColumnConstraints column2 = new ColumnConstraints(200);
        internalTop.setHgap(5);
        internalTop.getColumnConstraints().addAll(column1, column2);
        internal.setMargin(internalTop, new Insets(0, 0, 15, 0));
        container.setPrefWidth(700);
        container.setPadding(new Insets(10, 0, 0, 20));
        Label label1 = new Label("Fact File Viewer");
        label1.setFont(new Font(27));
        label1.setUnderline(true);
        Text name = new Text("Name:\t" + emp.employee);
        name.setFont(new Font(20));
        VBox.setMargin(name, new Insets(0, 0, 25, 0));
        Button openWindow = new Button("Open in New Window");
        openWindow.getStyleClass().add("main-button");
        openWindow.setOnAction((ae) -> {
            openFFWindow(emp);
        });
        //internalTop.setGridLinesVisible(true);
        internalTop.add(name, 0, 0);
        internalTop.add(openWindow, 1, 0);
        GridPane.setHalignment(openWindow, HPos.RIGHT);
        GridPane.setHalignment(name, HPos.LEFT);
        //---------------------------
        Text number1 = new Text("Number of Groups Participating in: \t\t\t\t\t\t"+emp.getnoGroups());
        number1.setFont(new Font(18));
        VBox.setMargin(number1, new Insets(15, 0, 10, 15));
        
        Text number2 = new Text("Emails within selected period: \t\t\t\t\t\t\t"+emp.getnoEmail());
        number2.setFont(new Font(18));
        VBox.setMargin(number2, new Insets(0, 0, 20, 15));
        
        
        VBox layoutTOP = new VBox();
        layoutTOP.getChildren().addAll(number1, number2);
        StackPane sectionA = new StackPane();
        Rectangle section1A = new Rectangle();
        section1A.setX(0);
        section1A.setY(0);
        section1A.setWidth(600);
        section1A.setHeight(70);
        section1A.setFill(Color.WHITE);
        section1A.setStrokeWidth(2);
        section1A.setStroke(Color.BLACK);
        sectionA.getChildren().addAll(section1A,layoutTOP); 
   
        Label label2 = new Label("Most Emails sent to: ");
        label2.setFont(new Font(16));
        VBox.setMargin(label2, new Insets(0, 0, 10, 0));
        //needs to be followed by a few pairwise strings - TEMP pairwise strings below
        //DISPLAY AS SIMPLE RETRIEVED STRINGS WITH DATA (SPACED TO WINDOW DESIGNS)
        
        VBox layoutPW = new VBox();
        //Temporary pair method below to display 3 pairs
        ObservableList<Edge> contactList = FXCollections.observableArrayList(emp.mostContacted());  
        ObservableList<String> most = FXCollections.<String>observableArrayList();
        for (int i = 0; i < 3; i++) {
            try{
            most.add(contactList.get(i).toString());
            } catch (Exception e){
                
            }
        }
        //WE DONT WANT ANY CSS EFFECTS ON THIS LISTVIEW
        final ListView<String> mostView = new ListView<>(most);
        mostView.getStyleClass().add("SmallListView");
        mostView.setPrefHeight(123);
        mostView.setMaxWidth(598);
        mostView.setMouseTransparent(true);
        mostView.setFocusTraversable(false);
        VBox.setMargin(mostView, new Insets(2, 6, 0, 6));
        
        layoutPW.getChildren().addAll(mostView);
        
        StackPane section1 = new StackPane();
        Rectangle section1R = new Rectangle(600, 124.5);
        section1R.setX(49);
        section1R.setY(200);
        section1R.setFill(Color.TRANSPARENT);
        section1R.setStrokeWidth(2);
        section1R.setStroke(Color.BLACK);
        section1.getChildren().addAll(section1R,layoutPW); 
        
        Label label3 = new Label("Participating Groups: (right-click to open)");
        label3.setFont(new Font(16));
        VBox.setMargin(label3, new Insets(20,0,15,0));
        //needs to be followed by ListView of Groups involved.
        
        
        
        //USED FOR DEBUG GROUPVIEW DATA ==============
        ObservableList<Group> groups = FXCollections.observableArrayList(emp.getGroups());
        ObservableList<String> groupNames = FXCollections.<String>observableArrayList();
      
        for (int i = 0; i < groups.size(); i++) {
            try {
            groupNames.add("Group " + groups.get(i).name);
            } catch (Exception e){
            }
        }
        final ListView<String> groupView = new ListView<>(groupNames);
        groupView.getStyleClass().add("SmallListView");
        groupView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (observable != null && observable.getValue() != null) {
                if (winID == 0) {
                    ((ListView) ((VBox) ((ScrollPane) ((BorderPane) parent.getParent()).getLeft()).getContent()).getChildren().get(1)).getSelectionModel().clearSelection();
                }
            }
        });
        //============================================  
        
                //context menu for list elements
        final ContextMenu rClick2 = new ContextMenu();
        MenuItem openInCurrent2 = new MenuItem("Open in Current Window");
        rClick2.getItems().add(openInCurrent2);
        if (stage.getTitle() == "Enron Corpus Analytics") {
            openInCurrent2.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {           
                    //Group target = groups.get(groupView.getSelectionModel().getSelectedIndex());
                    Group target = new Group(groups.get(groupView.getSelectionModel().getSelectedIndex()), controller.getDateFrom(winID), controller.getDateTo(winID));
                    BorderPane containerRHS = getGFFWindowContent(target, stage, winID, parent);
                    controller.assignGroup(target, winID);
                    containerRHS.setPrefWidth(containerRHS.getBoundsInParent().getWidth());
                    parent.setContent(containerRHS);
                }
            });
        } else {
            openInCurrent2.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int itemIndex = groupView.getSelectionModel().getSelectedIndex();        
                    Group updatedGroup = new Group(groups.get(itemIndex), controller.getDateFrom(winID), controller.getDateTo(winID));
                    stage.setScene(miniFFUpdate(updatedGroup, stage, winID));
                    stage.setTitle("FactFile: " + groups.get(itemIndex).name);
                }
            });
        }
        MenuItem openFFWindow2 = new MenuItem("Open in New Window");
        rClick2.getItems().add(openFFWindow2);
        openFFWindow2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int itemIndex = groupView.getSelectionModel().getSelectedIndex();
                Group target = new Group(groups.get(groupView.getSelectionModel().getSelectedIndex()), controller.getDateFrom(winID), controller.getDateTo(winID));
                openFFWindow(target);
            }
        });
        
        
        groupView.setPrefHeight(125);
        groupView.setMinWidth(600);
        groupView.setMaxWidth(600);
        groupView.setContextMenu(rClick2);
        VBox.setMargin(groupView, new Insets(0, 0, 0, 10));
        
        Button graphView = new Button("View Line Graph");
        graphView.setOnAction((event)->{
            if (winID == 0) {
                parent.setContent(graphScene(emp, null, winID, stage, parent));
            }
            else {
                System.out.println("winID: " + winID);
                Scene scene = new Scene(graphScene(emp, null, winID, stage, parent), 680, 690);
                scene.getStylesheets().add(css2);
                stage.setScene(scene);
                stage.show();
            }
            
        });
        graphView.getStyleClass().add("main-button");
        VBox.setMargin(graphView, new Insets(0, 0, 0, 515));
        
        Button pSelect = new Button("Select Period");
        pSelect.getStyleClass().add("main-button");
        pSelect.setOnAction( new EventHandler<ActionEvent>() {
           @Override public void handle(ActionEvent e) {
                stage.getScene().getRoot().setMouseTransparent(true);
                graphView.setDisable(true);
                boolean successStatus = periodScene(winID, pSelect);
                isUpdated.setUpdated(false);
                if (successStatus == true) {
                    System.out.println("WindowID: " + controller.getInstanceNumber(winID) + controller.getDateFrom(winID).toString() + " -- " + controller.getDateTo(winID).toString());
                    System.out.println("Success status confirmed true");
                    Efactfile emp2 = new Efactfile(emp.employee, controller.getDateFrom(winID), controller.getDateTo(winID), emp.participating);
                    if (winID == 0) {
                        parent.setContent(getEFFWindowContent(emp2, stage, winID, parent));
                    }
                    else {
                        stage.hide();
                        openFFWindow(emp2, winID);
                    }
                    graphView.setDisable(false);
                    stage.getScene().getRoot().setMouseTransparent(false);
                }
                else {
                    System.out.println("Failed to confirm success status");
                    graphView.setDisable(false);
                    stage.getScene().getRoot().setMouseTransparent(false);
                }
           }
        });
        VBox.setMargin(pSelect, new Insets(5, 0, 5, 520));
        
        container.setSpacing(20);
        
        VBox.setMargin(container, new Insets(0, 10, 0, 0));
        layoutFix.getChildren().add(container);
        container.getChildren().addAll(label1,internal, graphView);
        internal.getChildren().addAll(internalTop, sectionA, pSelect, label2, section1, label3, groupView);
        return layoutFix;
    }
    
    private VBox getGFactFile(Group grp, Stage stage, int winID, ScrollPane parent) {
        VBox layoutFix = new VBox();
        VBox container = new VBox();
        VBox internal = new VBox(); //internal VBox for layout
        GridPane internalTop = new GridPane();
        VBox.setMargin(internal, new Insets(10,0,0,15));
        ColumnConstraints column1 = new ColumnConstraints(400);
        ColumnConstraints column2 = new ColumnConstraints(200);
        internalTop.setHgap(5);
        internalTop.getColumnConstraints().addAll(column1, column2);
        internal.setMargin(internalTop, new Insets(0, 0, 15, 0));
        container.setPrefWidth(700);
        
        Label label1 = new Label("Fact File Viewer");
        label1.setFont(new Font(27));
        label1.setUnderline(true);
        Text name = new Text("Name:\t Group " + grp.name);
        name.setFont(new Font(20));
        VBox.setMargin(name, new Insets(0, 0, 25, 0));
        Button openWindow = new Button("Open in New Window");
        openWindow.getStyleClass().add("main-button");
        openWindow.setOnAction((ae) -> {
            openFFWindow(grp);                                                            
        });
        //internalTop.setGridLinesVisible(true);
        internalTop.add(name, 0, 0);
        internalTop.add(openWindow, 1, 0);
        GridPane.setHalignment(openWindow, HPos.RIGHT);
        GridPane.setHalignment(name, HPos.LEFT);
        //---------------------------
        Text number1 = new Text("Number of Members: \t\t\t\t\t\t\t\t\t"+grp.countMembers());
        number1.setFont(new Font(18));
        VBox.setMargin(number1, new Insets(15, 0, 10, 15));
        
        Text number2 = new Text("Emails within selected period: \t\t\t\t\t\t\t"+ grp.getEmailCount() );
        number2.setFont(new Font(18));
        VBox.setMargin(number2, new Insets(0, 0, 20, 15));
        
        
        VBox layoutTOP = new VBox();
        layoutTOP.getChildren().addAll(number1, number2);
        StackPane sectionA = new StackPane();
        Rectangle section1A = new Rectangle();
        section1A.setX(0);
        section1A.setY(0);
        section1A.setWidth(600);
        section1A.setHeight(70);
        section1A.setFill(Color.WHITE);
        section1A.setStrokeWidth(2);
        section1A.setStroke(Color.BLACK);
        sectionA.getChildren().addAll(section1A,layoutTOP); 
        
        Label label2 = new Label("Highest Pairwise Communications in Group: ");
        label2.setFont(new Font(16));
        VBox.setMargin(label2, new Insets(0, 0, 10, 0));
        //needs to be followed by a few pairwise strings - TEMP pairwise strings below
        //DISPLAY AS SIMPLE RETRIEVED STRINGS WITH DATA (SPACED TO WINDOW DESIGNS)
        
        VBox layoutPW = new VBox();
        //Temporary pair method below to display 3 pairs
        ObservableList<Pair> pairList = FXCollections.observableArrayList(grp.getTopPairs());   
        ObservableList<String> pairs = FXCollections.<String>observableArrayList();;
        for (int i = 0; i < 3; i++) {
            try{
            pairs.add(pairList.get(i).toString());
            } catch (Exception e){
                
            }
        }
        //WE DONT WANT ANY CSS EFFECTS ON THIS LISTVIEW
        final ListView<String> pairView = new ListView<>(pairs);
        pairView.getStyleClass().add("SmallListView");
        pairView.setPrefHeight(123);
        pairView.setMaxWidth(598);
        pairView.setMouseTransparent(true);
        pairView.setFocusTraversable(false);
     
        if (winID == 0) {
            container.setPadding(new Insets(10, 0, 0, 30));
            VBox.setMargin(pairView, new Insets(2, 0, 0, 6));
        }
        else {
            container.setPadding(new Insets(10, 0, 0, 20));
            VBox.setMargin(pairView, new Insets(2, 0, 0, 9));
        }
   
        layoutPW.getChildren().addAll(pairView);
        
        StackPane section1 = new StackPane();
        Rectangle section1R = new Rectangle(600, 124.5);
        section1R.setX(49);
        section1R.setY(200);
        section1R.setFill(Color.TRANSPARENT);
        section1R.setStrokeWidth(2);
        section1R.setStroke(Color.BLACK);
        section1.getChildren().addAll(section1R,layoutPW); 
        
        Label label3 = new Label("Participating Individuals: (right-click to open)");
        label3.setFont(new Font(16));
        VBox.setMargin(label3, new Insets(20,0,15,0));
        //needs to be followed by ListView of Groups involved.
        
        
        //USED FOR DEBUG EMPVIEW DATA ==============
        ObservableList<Efactfile> employees = FXCollections.observableArrayList(grp.getEmployees()) ;
        ObservableList<String> employeesNames = FXCollections.<String>observableArrayList();
        for (int i = 0; i < employees.size(); i++) {
            employeesNames.add(employees.get(i).employee);
        }
        ListView<String> empView = new ListView<>(employeesNames);
        empView.getStyleClass().add("SmallListView");
        empView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (observable != null && observable.getValue() != null) {
                if (winID == 0) {
                    ((ListView) ((VBox) ((ScrollPane) ((BorderPane) parent.getParent()).getLeft()).getContent()).getChildren().get(1)).getSelectionModel().clearSelection();
                }
            }
        });
        //============================================  

        //context menu for list elements
        final ContextMenu rClick2 = new ContextMenu();
        MenuItem openInCurrent2 = new MenuItem("Open in Current Window");
        IntegerProperty identifier = new SimpleIntegerProperty();
        if (stage.getTitle() == "Enron Corpus Analytics") {
            openInCurrent2.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {           
                    //Efactfile target = employees.get(empView.getSelectionModel().getSelectedIndex());
                    identifier.setValue(empView.getSelectionModel().getSelectedIndex());
                    Efactfile target = new Efactfile(employees.get(identifier.getValue()).employee, controller.getDateFrom(winID), controller.getDateTo(winID), employees.get(identifier.getValue()).participating);
                    BorderPane containerRHS = getEFFWindowContent(target, stage, winID, parent);
                    controller.assignEmployee(target, winID);
                    containerRHS.setPrefWidth(containerRHS.getBoundsInParent().getWidth());
                    System.out.println("Utility used on Window: " + winID + ", with dates: " + controller.getDateFrom(winID).toString() + " to " + controller.getDateTo(winID).toString());
                    parent.setContent(containerRHS);
                }
            });
        } else {
            openInCurrent2.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int itemIndex = empView.getSelectionModel().getSelectedIndex();     
                    Efactfile updatedEmp = new Efactfile(employees.get(itemIndex).employee, controller.getDateFrom(winID), controller.getDateTo(winID), employees.get(itemIndex).participating);
                    stage.setScene(miniFFUpdate(employees.get(itemIndex), stage, winID));
                    stage.setTitle("FactFile: " + employees.get(itemIndex).employee);
                }
            });
        }
        MenuItem openFFWindow2 = new MenuItem("Open in New Window");
        rClick2.getItems().addAll(openInCurrent2, openFFWindow2);
        openFFWindow2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int itemIndex = empView.getSelectionModel().getSelectedIndex();
                Efactfile updatedEmp = new Efactfile(employees.get(itemIndex).employee, controller.getDateFrom(winID), controller.getDateTo(winID), employees.get(itemIndex).participating);
                openFFWindow(updatedEmp);
            }
        });
        
        empView.setPrefHeight(125);
        empView.setMinWidth(600);
        empView.setMaxWidth(600);
        empView.setContextMenu(rClick2);
        
        VBox.setMargin(empView, new Insets(0, 0, 0, 10));
        Button graphView = new Button("View Line Graph");
        graphView.setOnAction((event)->{
            if (winID == 0) {
                parent.setContent(graphScene(null, grp, winID, stage, parent));
            }
            else {
                System.out.println("winID: " + winID);
                Scene scene = new Scene(graphScene(null, grp, winID, stage, parent), 680, 690);
                scene.getStylesheets().add(css2);
                stage.setScene(scene);
                stage.show();
            }
        });
        graphView.getStyleClass().add("main-button");
        VBox.setMargin(graphView, new Insets(0, 0, 0, 515));
        
        Button pSelect = new Button("Select Period");
        pSelect.getStyleClass().add("main-button");
        pSelect.setOnAction( new EventHandler<ActionEvent>() {
           @Override public void handle(ActionEvent e) {
                stage.getScene().getRoot().setMouseTransparent(true);
                graphView.setDisable(true);
                boolean successStatus = periodScene(winID, pSelect);
                isUpdated.setUpdated(false);
                stage.getScene().getRoot().setMouseTransparent(false);
                if (successStatus == true) {
                    System.out.println(controller.getDateFrom(winID).toString() + " -- " + controller.getDateTo(winID).toString());
                    System.out.println("Success status confirmed true");
                    Group grp2 = new Group(grp, controller.getDateFrom(winID), controller.getDateTo(winID));
                    if (winID == 0) {
                        parent.setContent(getGFFWindowContent(grp2, stage, winID, parent));
                    }
                    else {
                        stage.hide();
                        openFFWindow(grp2, winID);
                    }
                    graphView.setDisable(false);
                }
                else {
                    System.out.println("Failed to confirm success status");
                    graphView.setDisable(false);
                }
           }
        });
        VBox.setMargin(pSelect, new Insets(5, 0, 5, 520));
        
        container.setSpacing(20);
        
        //VBox.setMargin(container, new Insets(0, 30, 0, 10));
        layoutFix.getChildren().add(container);
        container.getChildren().addAll(label1,internal, graphView);
        internal.getChildren().addAll(internalTop, sectionA, pSelect, label2, section1, label3, empView);
        return layoutFix;
    }
    
    private ObservableList<String> populateGroups() {
        ObservableList<Group> groups = ListElements.getGroups();
        
        SortedList<Group> sortedList = groups.sorted(new OBSLComparator());
        
        //GROUPS NEED SORTING BASED ON THE INTEGER COUNT.
        //perhaps leave to backend group to presort (near impossible to do from ObservableList).
        //can be done via using the comparable interface.
        //need name matching to id of employee to make factfile system work
        ObservableList<String> groupNames = FXCollections.<String>observableArrayList();
      
        for (int i = 0; i < sortedList.size(); i++) {
            groupNames.add("Group "+sortedList.get(i).name + " | Emails: " + sortedList.get(i).getEmailCount());
        }
        return groupNames;
    }

    public Scene welcomeScene() {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1360, 700, Color.WHITE);
        scene.getStylesheets().add(css1);
        //attaching a stylesheet has some quirks, may run into troubles with this later.
        stage.setResizable(false);  //I'm not sure if this is a good option.
        MenuBar menuBar = generateMenuBar();
        
        Text gettingStarted = new Text("Getting Started");
        Text info = new Text("");
        Text title = new Text("Welcome to Enron Corpus Analytics");
        Text menu = new Text("Menu Select\n");
        info.setText("Select one of the menu options using the selections to the right to access data about individual’s "
                + "communication patterns or groups (clique) communications.\n\n" 
                + "The tab menu (top) also provides easy access to various menus.\n\n"
                + "Please use the ‘About & Help’ page under Menu if you are in need of assistance.\n\n"
                + "Please note that by default data is displayed for the entire time period of the corpus’ data, "
                + "please use the ‘Select Period’ option on the windows where available to filter (Fact Files & Ranking windows).\n\n"
                + "When selecting employees or groups from a list, right-clicking provides a context menu for extra functionality.");
        info.setWrappingWidth(400);
        //info.setFill(Color.WHITE);
        
        Font calibri_large = Font.font("Calibri", 36);
        Font calibri_medium = Font.font("Calibri", 24);
        Font calibri_small = Font.font("Calibri", 18);
        Font calibri_title = Font.font("Calibri", 40);
        gettingStarted.setFont(calibri_large);
        gettingStarted.setUnderline(false);
        info.setFont(calibri_small);
        title.setFont(calibri_title);
        title.setFill(Color.BLUE);
        menu.setFont(calibri_medium);
        menu.setUnderline(false);
        
        title.setUnderline(false);
        
        VBox leftSide = new VBox();
        leftSide.setPadding(new Insets(100, 80, 50, 150));
        leftSide.setSpacing(20);
        leftSide.setAlignment(Pos.TOP_LEFT);
        /*leftSide.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 1;"
                + "-fx-border-color: black");*/
        VBox rightSide = new VBox();
        /*rightSide.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 1;"
                + "-fx-border-color: blue");*/
        rightSide.setPadding(new Insets(80, 80, 50, 50));
        rightSide.setSpacing(25);
        rightSide.setAlignment(Pos.TOP_CENTER);
        //margin insets (top, right, bottom, left)
        rightSide.setMargin(menu, new Insets(70, 450, 0, 0));
        
        Button btnEmployee, btnGroup, btnRank, btnAbout;
        //main menu controls
        btnEmployee = new Button("View all Employee Data");
        btnEmployee.setOnAction(ae -> {
            //stage.setScene(employeeScene());
            this.selectionModel.select(1);
        });
        btnGroup = new Button("View all Group Data");
        btnGroup.setOnAction(ae -> {
            //stage.setScene(groupScene());
            this.selectionModel.select(2);
        });
        btnRank = new Button("View Connectedness & Centrality Rankings");
        btnRank.setOnAction(ae -> {
            //stage.setScene(rankScene());
            this.selectionModel.select(3);
        });
        btnAbout = new Button("About ECA & Help");
        btnAbout.setOnAction(ae -> {
            //stage.setScene(aboutScene());
            this.selectionModel.select(4);
        });
        
        
        btnEmployee.setMaxWidth(600);
        btnEmployee.setFont(calibri_medium);
        btnEmployee.getStyleClass().add("main-button");
        btnGroup.setMaxWidth(600);
        btnGroup.setFont(calibri_medium);
        btnGroup.getStyleClass().add("main-button");
        btnRank.setMaxWidth(600);
        btnRank.setFont(calibri_medium);
        btnRank.getStyleClass().add("main-button");
        btnAbout.setMaxWidth(600);
        btnAbout.setFont(calibri_medium);
        btnAbout.getStyleClass().add("main-button");
                
        rightSide.getChildren().addAll(
                title, 
                menu, 
                btnEmployee, 
                btnGroup,
                btnRank,
                btnAbout);
       
        
        Rectangle designRect = new Rectangle(100, 65, 500, 550);
        designRect.setStroke(Color.ORANGE);
        designRect.setStrokeWidth(10.0);
        designRect.setFill(Color.rgb(179, 196, 255));
        designRect.setArcHeight(100);
        designRect.setArcWidth(100);
        root.getChildren().add(designRect);
        
        leftSide.getChildren().addAll(gettingStarted, info);
        
        //root.setTop(menuBar);
        root.setLeft(leftSide);
        root.setRight(rightSide);
        
        return scene;
    }
    
    public Scene groupScene() {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1360, 700);
        scene.getStylesheets().add(css2);
        
        /*
        StackPane has wierd behaviours, adding new lines disregards
        their positioning. 
        The stackpane will attempt to resize its child to fill the entire content area
        if it isn't capapble of this resizing, its alignment will be set as Pos.CENTER.
        Directly on top of each other.
        (this positioning is not visible)
        
        Adding a new gridpane that spans over it covers the entire specified area
        (aka not transparent).
        
        New Plan:
        Scene with BorderPane as the root, two SubScene's for the left and right sides
        Is compatable with previous Alpha_Project Methods.
        */
        
        
        ScrollPane lhs = new ScrollPane();
        lhs.setPannable(false);
        lhs.setMinHeight(700);
        lhs.setMinWidth(670);
        ScrollPane rhs = new ScrollPane();
        rhs.setPannable(false);
        rhs.setMinHeight(700);
        rhs.setMinWidth(690);
        VBox contentL = new VBox();
        VBox contentR = new VBox();
        lhs.setContent(contentL);
        rhs.setContent(contentR);
        
        root.setLeft(lhs);
        root.setRight(rhs);
        
        //ABOVE IS THE SCENE LAYOUT CODE
        //BELOW IS THE LHS LABEL + LISTVIEW 
        
        Label groupsLbl = new Label("All Groups (sorted by email count)");
        groupsLbl.setUnderline(true);
        Font calibri = Font.font("Calibri", 30);
        groupsLbl.setFont(calibri);
        
        ObservableList<Group> groups = ListElements.getGroups();
        
        SortedList<Group> sortedList = groups.sorted(new OBSLComparator());
        ObservableList<String> groupNames = populateGroups();   //can be a key point for dynamic population 
        final ListView<String> grpListView = new ListView<>(groupNames);
        grpListView.setId("LargeListView");
        
        //selection listening
        IntegerProperty identifier = new SimpleIntegerProperty();
        grpListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (observable != null && observable.getValue() != null) {
                //only the string is picked up here, not the employee object
                //id.setText(observable.getValue());
                identifier.setValue(grpListView.getSelectionModel().getSelectedIndex());
                //Group target = groups.get(identifier.getValue());
                Group target = new Group(groups.get(identifier.getValue()), controller.getDateFrom(0), controller.getDateTo(0));
                BorderPane containerRHS = getGFFWindowContent(target, stage, 0, rhs);
                controller.assignGroup(target, 0);
                containerRHS.setPrefWidth(containerRHS.getBoundsInParent().getWidth());
                rhs.setContent(containerRHS);
            }
        });
        
        //context menu for list elements
        final ContextMenu rClick = new ContextMenu();
        MenuItem openFFWindow = new MenuItem("Open in New Window");
        rClick.getItems().addAll(openFFWindow);
        openFFWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int itemIndex = grpListView.getSelectionModel().getSelectedIndex();
                openFFWindow(groups.get(itemIndex));
            }
        });
        
        grpListView.setPrefWidth(450);
        grpListView.setMaxWidth(Double.MAX_VALUE);
        grpListView.setPrefHeight(450);
        grpListView.setContextMenu(rClick);
        
        //Text text = new Text("This is the Group View");
        
        contentL.getChildren().addAll(groupsLbl, grpListView);
        contentR.getChildren().add(getDefaultFFContent());
        
        VBox.setMargin(groupsLbl, new Insets(43, 0, 0, 125));
        VBox.setMargin(grpListView, new Insets(34, 0, 0, 108));
        
        MenuBar menuBar = generateMenuBar();
        
        //root.setTop(menuBar);
        
        return scene;
    }
    
    public Scene employeeScene() {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1360, 700);
        scene.getStylesheets().add(css2);
        
        /*
        StackPane has wierd behaviours, adding new lines disregards
        their positioning. 
        The stackpane will attempt to resize its child to fill the entire content area
        if it isn't capapble of this resizing, its alignment will be set as Pos.CENTER.
        Directly on top of each other.
        (this positioning is not visible)
        
        Adding a new gridpane that spans over it covers the entire specified area
        (aka not transparent).
        
        New Plan:
        Scene with BorderPane as the root, two SubScene's for the left and right sides
        Is compatable with previous Alpha_Project Methods.
        */
        
        
        ScrollPane lhs = new ScrollPane();
        lhs.setPannable(false);
        lhs.setMinHeight(700);
        lhs.setMinWidth(670);
        ScrollPane rhs = new ScrollPane();
        rhs.setPannable(false);
        rhs.setMinHeight(700);
        rhs.setMinWidth(690);
        VBox contentL = new VBox();
        VBox contentR = new VBox();
        lhs.setContent(contentL);
        rhs.setContent(contentR);
        
        root.setLeft(lhs);
        root.setRight(rhs);
        
        //ABOVE IS THE SCENE LAYOUT CODE
        //BELOW IS THE LHS LABEL + LISTVIEW 
        
        Label employeesLbl = new Label("All Employees (sorted alphabetically)");
        employeesLbl.setUnderline(true);
        Font calibri = Font.font("Calibri", 30);
        employeesLbl.setFont(calibri);
        
        ObservableList<Efactfile> employees = ListElements.getEmployees();
        ObservableList<String> employeesNames = FXCollections.<String>observableArrayList();
        for (int i = 0; i < employees.size(); i++) {
            employeesNames.add(employees.get(i).employee);
        }
        SortedList sorted = employeesNames.sorted(); //did this actually do anything?
        //need name matching to id of employee to make factfile system work
        final ListView<String> empListView = new ListView<>(sorted);
        empListView.setId("LargeListView");
        //selection listening
        //Text id = new Text("No fact file is currently selected.");
        IntegerProperty identifier = new SimpleIntegerProperty();
        empListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (observable != null && observable.getValue() != null) {
                //only the string is picked up here, not the employee object
                //id.setText(observable.getValue());
                identifier.setValue(empListView.getSelectionModel().getSelectedIndex());
                //Efactfile target = employees.get(identifier.getValue());      
                Efactfile target = new Efactfile(employees.get(identifier.getValue()).employee, controller.getDateFrom(0), controller.getDateTo(0), employees.get(identifier.getValue()).participating);
                BorderPane containerRHS = getEFFWindowContent(target, stage, 0, rhs);
                controller.assignEmployee(target, 0);
                containerRHS.setPrefWidth(containerRHS.getBoundsInParent().getWidth());
                rhs.setContent(containerRHS);
            }
        });
        
        //context menu for list elements
        final ContextMenu rClick = new ContextMenu();
        MenuItem openFFWindow = new MenuItem("Open in New Window");
        rClick.getItems().addAll(openFFWindow);
        openFFWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int itemIndex = empListView.getSelectionModel().getSelectedIndex();
                openFFWindow(employees.get(itemIndex));
            }
        });
        
        empListView.setPrefWidth(450);
        empListView.setMaxWidth(Double.MAX_VALUE);
        empListView.setPrefHeight(450);
        empListView.setContextMenu(rClick);
        
        //Text text = new Text("This is the Employee View");
        
        contentL.getChildren().addAll(employeesLbl, empListView);
        contentR.getChildren().add(getDefaultFFContent());
        
        VBox.setMargin(employeesLbl, new Insets(43, 0, 0, 125));
        VBox.setMargin(empListView, new Insets(34, 0, 0, 108));
        
        MenuBar menuBar = generateMenuBar();
        
        //root.setTop(menuBar);
        
        return scene;
    }
    
    public Scene rankScene() {
        //layout tools
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1360, 700, Color.WHITE);
        scene.getStylesheets().add(css2);
        VBox leftSide = new VBox();
        VBox rightSide = new VBox();
        root.setLeft(leftSide);
        root.setRight(rightSide);
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        GridPane gridpane2 = new GridPane();
        gridpane2.setHgap(10);
        gridpane2.setVgap(10);
        rightSide.getChildren().add(gridpane2);
        rightSide.setMargin(gridpane2, new Insets(40, 50, 30, 100));
        leftSide.getChildren().add(gridpane);
        leftSide.setMargin(gridpane, new Insets(50, 50, 20, 70));
        
        
        //Rankings LEFT Label 
        Label employeesLbl = new Label("Employees by Centrality Ranking\n"
                + "(Number of unique groups an employee is in)");
        employeesLbl.setUnderline(false);
        Font calibri = Font.font("Calibri", 27);
        employeesLbl.setFont(calibri);
        GridPane.setHalignment(employeesLbl, HPos.CENTER);
        gridpane.setPadding(new Insets(10));
        gridpane.setMargin(employeesLbl, new Insets(0, 0, 10, 0));
        gridpane.add(employeesLbl, 0, 0);
        
        Text note = new Text("Centrality 0 represents employees that aren't in a group bigger than 3"
                + "\nwith more than 2 messages sent between each person.");
        note.setFont(new Font(14));
        
        //Rankings RIGHT Label 
        Label pairwiseLbl = new Label("Highest Email Count by \nPairwise Communication");
        pairwiseLbl.setUnderline(false);
        pairwiseLbl.setFont(calibri);
        pairwiseLbl.setTextAlignment(TextAlignment.CENTER);
        GridPane.setHalignment(pairwiseLbl, HPos.CENTER);
        gridpane2.setPadding(new Insets(10));
        gridpane2.setMargin(pairwiseLbl, new Insets(-20, 40, 30, 0));
        gridpane2.add(pairwiseLbl, 0, 0);
        

        //LHS ListView =======================================================================
        ObservableList<Efactfile> employees = FXCollections.observableArrayList(Backend.getCentralityRanking()) ;
        ObservableList<String> centralityRanks = FXCollections.<String>observableArrayList();;
        for (int i = 0; i < employees.size(); i++) {
            centralityRanks.add(employees.get(i).employee+"   "+Integer.toString(employees.get(i).getnoGroups())); //GIVES NAME AND CENTRALITY
        }
        final ListView<String> empListView = new ListView<>(centralityRanks);
        empListView.setId("LargeListView");
        
                //context menu for list elements
        final ContextMenu rClick = new ContextMenu();
        MenuItem openFFWindow = new MenuItem("Open in New Window");
        rClick.getItems().add(openFFWindow);
        openFFWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int itemIndex = empListView.getSelectionModel().getSelectedIndex();
                openFFWindow(employees.get(itemIndex));
            }
        });
        
        empListView.setPrefWidth(450);
        empListView.setMaxWidth(Double.MAX_VALUE);
        empListView.setPrefHeight(450);
        empListView.setContextMenu(rClick);
        
        gridpane.add(empListView, 0, 1);   
        gridpane.add(note, 0, 2);
        //LHS ListView END ===================================================================================
        
        //RHS ListView =======================================================================================
        
        ObservableList<Pair> pairs = ListElements.getPairs();
        ObservableList<String> pairStrings = FXCollections.<String>observableArrayList();
        for (int i = 0; i < pairs.size(); i++) {
            pairStrings.add(pairs.get(i).toString());
        }
        
        final ListView<String> pairListView = new ListView<>(pairStrings);  //MAKE VISIBLE TBD
        ScrollPane container = new ScrollPane();
        container.setPannable(false);
        container.setPrefWidth(500);
        container.setPrefHeight(400);
        container.setContent(pairListView);
        pairListView.setId("cntr");
        pairListView.setSelectionModel(new DisabledSelectionModel<>());
        
        pairListView.setMouseTransparent(false);
        pairListView.setFocusTraversable(false);
        
        pairListView.setPrefWidth(500);
        pairListView.setMaxWidth(Double.MAX_VALUE);
        pairListView.setPrefHeight(400);
        
        gridpane2.add(container, 0, 1);  
        
        Text timeStamp = new Text("Currently displaying for:\n\n\t " + controller.getDateFrom(0) + " to " + controller.getDateTo(0));
        timeStamp.setFont(new Font(14));
        gridpane2.add(timeStamp, 0, 3);
        
        Button selectPeriod = new Button("Select Period");
        selectPeriod.getStyleClass().add("main-button");
        selectPeriod.setOnAction( new EventHandler<ActionEvent>() {
           @Override public void handle(ActionEvent e) {
                stage.getScene().getRoot().setMouseTransparent(true);
                boolean successStatus = periodScene(0, selectPeriod);
                isUpdated.setUpdated(false);
                stage.getScene().getRoot().setMouseTransparent(false);
                if (successStatus == true) {
                    System.out.println(controller.getDateFrom(0).toString() + " -- " + controller.getDateTo(0).toString());
                    System.out.println("Success status confirmed true");
                    ObservableList<Pair> updatedPairs = FXCollections.observableArrayList(Backend.filterPairRankings(controller.getDateFrom(0), controller.getDateTo(0)));
                    ObservableList<String> pairStrings = FXCollections.<String>observableArrayList();
                    for (int i = 0; i < pairs.size(); i++) {
                        pairStrings.add(updatedPairs.get(i).toString());
                    }
                    final ListView<String> pairListView = new ListView<>(pairStrings);
                    pairListView.setId("cntr");
                    pairListView.setPrefWidth(500);
                    pairListView.setMaxWidth(Double.MAX_VALUE);
                    pairListView.setPrefHeight(400);
                    timeStamp.setText("Currently displaying for:\n\n\t " + controller.getDateFrom(0) + " to " + controller.getDateTo(0));
                    container.setContent(pairListView);
                }
                else {
                    System.out.println("Failed to confirm success status");
                }
           }
        });
        gridpane2.add(selectPeriod, 0, 2);
        gridpane2.setHalignment(selectPeriod, HPos.CENTER);
        
        //Text text1 = new Text(10, 40, "This is the Rankings View");
        //root.getChildren().add(text1);
        
        Line splitLine = new Line(680, 0, 680, 700);
        splitLine.setStroke(Color.ORANGE);
        splitLine.setStrokeWidth(5);
        root.getChildren().add(splitLine);
        
        MenuBar menuBar = generateMenuBar();
        
        //root.setTop(menuBar);

        return scene;
    }
    
    public Scene aboutScene() {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1360, 700, Color.WHITE);
        MenuBar menuBar = generateMenuBar();
        VBox container = new VBox();
        
        Text aboutECATitle = new Text("About ECA & Team 16");
        aboutECATitle.setFont(new Font(18));
        aboutECATitle.setUnderline(true);
        
        Text aboutECA = new Text("ECA is a tool designed for the analysis of the Enron company dataset (corpus). \n"
                + "The aims of this program were to be able to show the connectedness of all employees and to highlight \n"
                + "the central figures of the company (assuming their inbox was provided).\n\n"
                + "Enron Corpus Analytics is the result of the efforts of all members of Team 16 for Software Engineering's project of 2016. \n"
                + "This program was made using Java/JavaFX 8, importJSON, and Google Guava libraries.");
        
        Text credits = new Text("Team 16: \n\n"
                + "\tUI Developer &  Project Manager: Christian Burberry\n"
                + "\tSystems Developers: Kameron Singh Bahia, Prashant Meeheelaul\n"
                + "\tBA & Testing: Shehel Yousuf\n"
                + "\tDocumentation: Cameron Mason");
        
        Text helpTitle = new Text("Using ECA");
        helpTitle.setFont(new Font(18));
        helpTitle.setUnderline(true);
        
        Text help = new Text("ECA allows users to select various employees or groups within the company based on a selection,\n"
                + "the selection can then be further queried using the 'Select Period' buttons to view a specific time frame of data.\n\n"
                + "Note: selectable lists have a right-click menu to open a factfile in the current window.\n"
                + "For a specific usage guide on the application, please click the button below to view the PDF guide.");
        
        Button viewPDF = new Button("View PDF");
        viewPDF.setOnAction(actionEvent -> {  
            final File file = new File("Manual.pdf");  
            getHostServices().showDocument(file.toURI().toString());  
        });  
        
        Rectangle rect = new Rectangle(40, 30, 1280, 630);
        rect.setStrokeWidth(5);
        rect.setStroke(Color.BLACK);
        rect.setFill(Color.LIGHTBLUE);
        
        root.getChildren().add(rect);
        
        container.getChildren().addAll(aboutECATitle, aboutECA, credits, helpTitle, help, viewPDF);
        
        VBox.setMargin(aboutECATitle, new Insets(50, 0, 0, 50));
        VBox.setMargin(aboutECA, new Insets(10, 0, 0, 50));
        VBox.setMargin(credits, new Insets(20, 0, 0, 50));
        VBox.setMargin(helpTitle, new Insets(50, 0, 0, 50));
        VBox.setMargin(help, new Insets(10, 0, 0, 50));
        VBox.setMargin(viewPDF, new Insets(10, 0, 0, 500));
        
        root.setCenter(container);
        //root.setTop(menuBar);
        BorderPane.setMargin(container, new Insets(0, 0, 0, 20));

        return scene;
    }
   
    public class OBSLComparator implements Comparator<Group> {
        @Override
        public int compare(Group g1, Group g2) {
            return (g1.getEmailCount() > g2.getEmailCount()) ? -1 : 1;
        }
    }

    
     static class DisabledSelectionModel<T> extends MultipleSelectionModel<T> {
       DisabledSelectionModel() {
         super.setSelectedIndex(-1);
         super.setSelectedItem(null);
       }
       @Override
       public ObservableList<Integer> getSelectedIndices() { return FXCollections.<Integer>emptyObservableList() ; }
       @Override
       public ObservableList<T> getSelectedItems() { return FXCollections.<T>emptyObservableList() ; }
       @Override
       public void selectAll() {}
       @Override
       public void selectFirst() {}
       @Override
       public void selectIndices(int index, int... indicies) {}
       @Override
       public void selectLast() {}
       @Override
       public void clearAndSelect(int index) {}
       @Override
       public void clearSelection() {}
       @Override
       public void clearSelection(int index) {}
       @Override
       public boolean isEmpty() { return true ; }
       @Override
       public boolean isSelected(int index) { return false ; }
       @Override
       public void select(int index) {}
       @Override
       public void select(T item) {}
       @Override
       public void selectNext() {}
       @Override
       public void selectPrevious() {}
       
     }    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

class updateBoolean {
    private boolean updated = false;
    private boolean wasCancelled = false;
    
    public void setUpdated(boolean x) {
        this.updated = x;
    }
    
    public boolean getUpdated() {
        return this.updated;
    }
    
    public void setCancelled(Boolean x) {
        this.wasCancelled = x;
    }
    
    public Boolean getCancelled() {
        return this.wasCancelled;
    }
}


