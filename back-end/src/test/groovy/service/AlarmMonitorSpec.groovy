package service

import spock.lang.Specification
import zg.acelera.service.AlarmMonitor
import zg.acelera.service.AlarmService

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class AlarmMonitorSpec extends Specification {
    def "schedules one-minute polling once and stop cancels it"() {
        given:
        def scheduler = Mock(ScheduledExecutorService)
        def future = Mock(ScheduledFuture)
        def service = new AlarmService(null, null) {
            @Override
            boolean hasMonitorableAlarms() {
                true
            }
        }
        def monitor = new AlarmMonitor(service, scheduler)

        when:
        monitor.start()
        monitor.start()
        monitor.stop()

        then:
        1 * scheduler.scheduleAtFixedRate(_, 0L, 1L, TimeUnit.MINUTES) >> future
        1 * future.isCancelled() >> false
        1 * future.isDone() >> false
        1 * future.cancel(false)
        !monitor.isRunning()
    }

    def "a failed poll does not cancel future polling"() {
        given:
        def scheduler = Mock(ScheduledExecutorService)
        def future = Mock(ScheduledFuture)
        def poll
        def service = new AlarmService(null, null) {
            @Override
            void checkDueAlarms() {
                throw new IllegalStateException("temporary failure")
            }
        }
        def monitor = new AlarmMonitor(service, scheduler)

        when:
        monitor.start()
        poll.run()
        poll.run()

        then:
        1 * scheduler.scheduleAtFixedRate(_, 0L, 1L, TimeUnit.MINUTES) >> { Runnable task, long initialDelay, long period, TimeUnit unit ->
            poll = task
            future
        }
        0 * future.cancel(_)
    }

    def "stops when no monitorable alarms remain and closes the scheduler"() {
        given:
        def scheduler = Mock(ScheduledExecutorService)
        def future = Mock(ScheduledFuture)
        def service = new AlarmService(null, null) {
            @Override
            boolean hasMonitorableAlarms() {
                false
            }
        }
        def poll
        def monitor = new AlarmMonitor(service, scheduler)

        when:
        monitor.start()
        poll.run()
        monitor.close()

        then:
        1 * scheduler.scheduleAtFixedRate(_, 0L, 1L, TimeUnit.MINUTES) >> { Runnable task, long initialDelay, long period, TimeUnit unit ->
            poll = task
            future
        }
        1 * future.cancel(false)
        1 * scheduler.shutdown()
    }
}
