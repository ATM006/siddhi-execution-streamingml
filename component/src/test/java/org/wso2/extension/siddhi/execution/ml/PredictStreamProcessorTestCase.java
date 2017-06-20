/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.ml;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Properties;

public class PredictStreamProcessorTestCase {
    static final Logger LOG = Logger.getLogger(PredictStreamProcessorTestCase.class);
    private volatile boolean eventArrived;
    private String modelStorageLocation = System.getProperty("user.dir") + File.separator + "src" + File.separator
            + "test" + File.separator + "resources" + File.separator + "test-model";

    @BeforeMethod
    public void init() {
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        valueHolder.setMlProperties(new Properties());
        eventArrived = false;
    }

    @Test
    public void predictFunctionTest() throws InterruptedException, URISyntaxException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String inputStream = "define stream InputStream "
                + "(NumPregnancies double, PG2 double, DBP double, TSFT double, SI2 double, " +
                "BMI double, DPF double, Age double);";

        String query = "@info(name = 'query1') " + "from InputStream#ml:predict('" + modelStorageLocation
                + "','double') " + "select * " + "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    AssertJUnit.assertEquals(1.0, inEvents[0].getData(8));
                    eventArrived = true;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] { 2, 84, 0, 0, 0, 0.0, 0.304, 21 });
        sleepTillArrive(5001);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
        siddhiManager.shutdown();
    }

    @Test
    public void predictFunctionWithSelectedAttributesTest() throws InterruptedException, URISyntaxException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String inputStream = "define stream InputStream "
                + "(NumPregnancies double, PG2 double, DBP double, TSFT double, SI2 double, " +
                "BMI double, DPF double, Age double);";

        String query = "@info(name = 'query1') " + "from InputStream#ml:predict('" + modelStorageLocation
                + "', 'double', NumPregnancies, PG2, DBP, TSFT, SI2, BMI, DPF, Age) " + "select * "
                + "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    AssertJUnit.assertEquals(1.0, inEvents[0].getData(8));
                    eventArrived = true;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] { 2, 84, 0, 0, 0, 0.0, 0.304, 21 });
        sleepTillArrive(5001);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void predictFunctionWithSelectPredictionTest() throws InterruptedException, URISyntaxException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String inputStream = "define stream InputStream "
                + "(NumPregnancies double, PG2 double, DBP double, TSFT double, SI2 double, " +
                "BMI double, DPF double, Age double);";

        String query = "@info(name = 'query1') " + "from InputStream#ml:predict('" + modelStorageLocation
                + "', 'double', NumPregnancies, PG2, DBP, TSFT, SI2, BMI, DPF, Age) " + "select Class "
                + "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    AssertJUnit.assertEquals(1.0, inEvents[0].getData(0));
                    eventArrived = true;
                }
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[] { 2, 84, 0, 0, 0, 0.0, 0.304, 21 });
        sleepTillArrive(5001);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    private void sleepTillArrive(int milliseconds) {
        int totalTime = 0;
        while (!eventArrived && totalTime < milliseconds) {
            int t = 1000;
            try {
                Thread.sleep(t);
            } catch (InterruptedException ignore) {
            }
            totalTime += t;
        }
    }
}
