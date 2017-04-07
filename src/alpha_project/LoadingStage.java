
package alpha_project;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadingStage extends Stage {
    static Task worker;
    
    LoadingStage(Stage primaryStage, Scene scene) throws InterruptedException {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setOnCloseRequest((event) -> event.consume());
        VBox root = new VBox();
        root.setMinHeight(70);
        root.setPadding(new Insets(10));
        root.setSpacing(5);
        Label label = new Label("ECA Starting up...");
        
        ProgressBar pb = new ProgressBar(0);
        label.setPrefWidth(300);
        pb.setPrefWidth(300);
        root.getChildren().addAll(label, pb);
        
        stage.setScene(new Scene(root));
        stage.show();
        
        worker = createWorker();
        
        worker.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                stage.hide();
                primaryStage.setScene(scene);
                primaryStage.show();
                //call new stage here
            }
        });
        
        pb.progressProperty().unbind();
        pb.progressProperty().bind(worker.progressProperty());
        
        stage.show();
        new Thread(worker).start();      
    }
    
    private Task createWorker() throws InterruptedException {
        return new Task<Void>() {
            
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < 500; i++) {
                    updateProgress(i + 1, 500);
                    Thread.sleep(10);
                }
                
                return null;
            }
        };
    }
    
}