import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import java.io.IOException;

public class launcher extends Application{


    public static void main(String[] args) {
        Application.launch(args);
    }

    public launcher() {

    }

    @Override
    public void start(final Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(launcher.class.getResource("UI.fxml"));
        Pane root = (Pane) loader.load();

        Scene scene = new Scene(new Group(root));
        stage.setTitle("JDBC - DBMS");
        stage.setScene(scene);
        stage.show();
        letterbox(scene, root);
        stage.setMaximized(true);
    }

    private void letterbox(final Scene scene, final Pane contentPane) {
        final double initWidth  = scene.getWidth();
        final double initHeight = scene.getHeight();
        final double ratio = initWidth / initHeight;
        SceneSizeChangeListener sizeListener = new SceneSizeChangeListener(scene, ratio, initHeight, initWidth, contentPane);
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
    }

    private static class SceneSizeChangeListener implements ChangeListener<Number> {
        private final Scene scene;
        private final double ratio;
        private final double initHeight;
        private final double initWidth;
        private final Pane contentPane;

        public SceneSizeChangeListener(Scene scene, double ratio, double initHeight, double initWidth, Pane contentPane) {
            this.scene = scene;
            this.ratio = ratio;
            this.initHeight = initHeight;
            this.initWidth = initWidth;
            this.contentPane = contentPane;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            final double newWidth  = scene.getWidth();
            final double newHeight = scene.getHeight();
            double scaleFactor = newWidth / newHeight > ratio ? newHeight / initHeight : newWidth / initWidth;

            Scale scale = new Scale(scaleFactor, scaleFactor);
            scale.setPivotX(0);
            scale.setPivotY(0);
            scene.getRoot().getTransforms().setAll(scale);

            contentPane.setPrefWidth (newWidth  / scaleFactor);
            contentPane.setPrefHeight(newHeight / scaleFactor);
        }
    }
}

