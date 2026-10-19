package controller

import spock.lang.Specification
import zg.acelera.Main
import zg.acelera.app.UserMenu
import zg.acelera.util.interface_user.TaskInterface
import zg.acelera.util.interface_user.UserInterface

class ApplicationLifecycleSpec extends Specification {
    def "builds the application collaborators and shuts down the alarm monitor"() {
        given:
        def userInterface = Mock(UserInterface)
        def taskInterface = Mock(TaskInterface)
        def userMenu = new UserMenu(null, userInterface)
        def factory = Main.getDeclaredMethod(
                "getMainMenu",
                UserInterface,
                TaskInterface,
                UserMenu
        )
        factory.accessible = true

        when:
        def context = factory.invoke(null, userInterface, taskInterface, userMenu)
        def monitor = context.monitor()
        def schedulerField = monitor.class.getDeclaredField("scheduler")
        schedulerField.accessible = true
        def scheduler = schedulerField.get(monitor)
        monitor.close()

        then:
        context != null
        monitor != null
        scheduler.isShutdown()
    }
}
