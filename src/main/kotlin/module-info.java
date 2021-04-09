module javafxModule {
    requires javafx.controls;
    requires javafx.graphics;
    requires tornadofx;
    requires kotlin.stdlib;

    exports com.example.demo.view;
    exports com.example.demo.app;
}