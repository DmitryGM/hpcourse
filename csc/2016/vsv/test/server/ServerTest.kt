package server

import communication.CommunicationProtos
import org.junit.After
import org.junit.Assert
import org.junit.ClassRule
import org.junit.Test
import util.client.ServerResource
import util.client.TaskWorker


/**
 * @author Sergey Voytovich (voytovich.sergeey@gmail.com) on 10.04.16
 */
class ServerTest {

    companion object {

        val port = 4774

        @ClassRule
        @JvmField
        val server = ServerResource(port)
    }

    @After
    fun separate_tests_outputs() {
        println("------------------------------")
    }


    @Test
    fun submit_task_test() {
        val client = TaskWorker(port, "first-client")
        val str = client.submit(param(100), param(15), param(23), param(11), 10)
        Thread.sleep(1000)
        val response = client.list()
        Assert.assertEquals(response.status, CommunicationProtos.Status.OK)
        for (task in response.tasksList) {
            if (str.submittedTaskId == task.taskId) {
                Assert.assertEquals(calculate(100, 15, 23, 11, 10), task.result)
            }
        }
    }

    @Test(timeout = 1000)
    fun list_running_task_test() {
        val client = TaskWorker(port, "first-client")
        client.submit(param(100), param(15), param(23), param(11), Long.MAX_VALUE)
        val response = client.list()
        Assert.assertEquals(response.status, CommunicationProtos.Status.OK)
    }

    fun param(p: Long): CommunicationProtos.Task.Param {
        return CommunicationProtos.Task.Param.newBuilder()
                .setValue(p)
                .build()
    }

    fun dependentTask(id: Int): CommunicationProtos.Task.Param {
        return CommunicationProtos.Task.Param.newBuilder()
                .setDependentTaskId(id)
                .build()
    }

    fun calculate(_a: Long, _b: Long, _p: Long, _m: Long, _n: Long): Long {
        var a = _a
        var b = _b
        var p = _p
        var m = _m
        var n = _n
        while (n-- > 0) {
            b = (a * p + b) % m;
            a = b;
        }
        return a
    }

}
