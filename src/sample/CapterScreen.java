package sample;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class CapterScreen extends Application {
    ImageView iv;   // 切成图片展示区域
    Stage primaryStage; // 主舞台
    Stage stage;    // 切图时候辅助舞台
    double start_x; // 切图起始位置x
    double start_y; // 切图起始位置y
    double w;   // 切图区域宽
    double h;   // 切图区域高
    HBox hbox;  // 切图区域

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
        AnchorPane root = new AnchorPane();
        // 创建截图按钮和显示区域
        Button btn = new Button("截图");
        iv = new ImageView();
        iv.setFitHeight(400);
        iv.setPreserveRatio(true);
        root.getChildren().addAll(iv, btn);
        AnchorPane.setTopAnchor(btn, 50d);
        AnchorPane.setLeftAnchor(btn, 50d);
        AnchorPane.setTopAnchor(iv, 100d);
        AnchorPane.setLeftAnchor(iv, 50d);

        // 设置场景和舞台大小
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("截图工具");
        primaryStage.setHeight(800);
        primaryStage.setWidth(800);

        btn.setOnAction(event -> {
            show();
        });

        KeyCombination keyCombination = KeyCombination.valueOf("ctrl+alt+p");
        Mnemonic mc = new Mnemonic(btn, keyCombination);
        scene.addMnemonic(mc);
        primaryStage.show();
    }

    /**
     * todo
     */
    private void show() {
        // 缩小主舞台到任务栏
        primaryStage.setIconified(true);

        stage = new Stage();
        //锚点布局采用半透明
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: #85858522");
        //场景设置白色全透明
        Scene scene = new Scene(anchorPane);
        scene.setFill(Paint.valueOf("#ffffff00"));
        stage.setScene(scene);

        stage.setFullScreenExitHint("");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setFullScreen(true);
        stage.show();

        //切图窗口绑定鼠标按下事件
        anchorPane.setOnMousePressed(event -> {
            // 清空
            anchorPane.getChildren().clear();

            // 创建截图区域
            hbox = new HBox();
            hbox.setBackground(null);

            hbox.setBorder(new Border(
                    new BorderStroke(Paint.valueOf("#c03700"), BorderStrokeStyle.SOLID, null, new BorderWidths(3))));
            anchorPane.getChildren().addAll(hbox);
            start_x = event.getSceneX();
            start_y = event.getSceneY();
            AnchorPane.setLeftAnchor(hbox,start_x);
            AnchorPane.setTopAnchor(hbox,start_y);

        });

        anchorPane.setOnMouseDragged(event -> {
            // 用label 来记录切图的区域的长宽
            Label label = new Label();
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(170, 30);
            anchorPane.getChildren().add(label);
            AnchorPane.setLeftAnchor(label, start_x + 30);
            AnchorPane.setTopAnchor(label, start_y);
            label.setTextFill(Paint.valueOf("#ffffff"));
            label.setStyle("-fx-background-color: #000000");
            // 计算宽高并完成切图区域的动态效果
            w = Math.abs(event.getSceneX() - start_x);
            h = Math.abs(event.getSceneY() - start_y);
            hbox.setPrefWidth(w);
            hbox.setPrefHeight(h);
            label.setText("宽："+w+" 高："+h);
        });

        anchorPane.setOnMouseReleased(event -> {
            //记录最终长宽
            w = Math.abs(event.getSceneX()-start_x);
            h = Math.abs(event.getSceneY()-start_y);
            anchorPane.setStyle("-fx-background-color: #00000000");
            //添加剪切按钮，并显示在切图区域的底部
            Button b = new Button("剪切");
            hbox.setBorder(new Border(
                    new BorderStroke(Paint.valueOf("85858544"), BorderStrokeStyle.SOLID, null, new BorderWidths(3))));
            hbox.getChildren().add(b);
            hbox.setAlignment(Pos.BOTTOM_RIGHT);
            b.setOnAction(event1 -> {
                stage.close();
                try {
                    capterImg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 主舞台还原
                primaryStage.setIconified(false);
            });
        });

        scene.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ESCAPE){
                stage.close();
                primaryStage.setIconified(false);
            }
        });

    }

    /**
     * todo
     */
    private void capterImg() throws Exception {
        Robot robot=new Robot();
        Rectangle rectangle = new Rectangle((int) start_x, (int) start_y, (int) w, (int) start_y);
        BufferedImage screenCapture  = robot.createScreenCapture(rectangle);

        //截图图片背景透明处理
        //BufferedImage bufferedImage = Picture4.transferAlpha(screenCapture);
        //不进行背景透明处理
        BufferedImage bufferedImage = screenCapture;
        //转换图片格式展示在主舞台的场景中
        WritableImage writableImage = SwingFXUtils.toFXImage(bufferedImage, null);
        iv.setImage(writableImage);

        //将截图内容，放入系统剪切板
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putImage(writableImage);
        cb.setContent(content);
        //将截取图片放入到系统固定位置
//        ImageIO.write(bufferedImage, "png", new File(System.getProperty("user.dir") +File.separator+ LocalDateTime.now().format(
//                DateTimeFormatter.ofPattern("yyyy-MM-ddhh:MM:ss")) + ".png"));

        ImageIO.write(bufferedImage, "png", new File("d:/a.png"));


    }


    public static void main(String[] args) {
        launch(args);
    }
}
