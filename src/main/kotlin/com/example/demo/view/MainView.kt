package com.example.demo.view

import com.example.demo.app.Styles
import tornadofx.*

class MainView : View("WaiterRobot_Mediator") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}