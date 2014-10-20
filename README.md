Hystrix Lab
===========

Originally created for ca_java competence day
  
Members: Johan Haleby

Tags: ca_java

Introduction
------------
The purpose of this lab is to get basic acquaintance with <a href="https://github.com/Netflix/Hystrix">Hystrix</a> from Netflix. 
It's encouraged to experiment with various <a href="https://github.com/Netflix/Hystrix/wiki/Configuration">configurations</a>, 
<a href="https://github.com/Netflix/Hystrix/wiki/How-To-Use#Common-Patterns">patterns</a> and <a href="https://github.com/Netflix/Hystrix/wiki">other features</a>
(such as semaphores instead of threads, request collapsing etc).

Prerequisites
-------------

1. Install the following: 
    * Java 8 
    * MongoDB (for example `brew install mongodb`).
2. Start MongoDB.
3. Clone this repository and build the todo application by running `./gradlew build`.
4. Open the project in your favorite IDE.
5. Download and start the Hystrix dashboard:
    ```bash
    $ git clone https://github.com/Netflix/Hystrix.git
    $ cd Hystrix
    $ ./gradlew :hystrix-dashboard:jettyRun
    ```
    
    You should see something like this once it's started:
    ```bsh
    > Building > :hystrix-dashboard:jettyRun > Running at http://localost:7979/hystrix-dashboard
    ```
6. Install Nginx (for example `brew install nginx`) (you can defer this until you reach section 5 in the lab instructions).
7. Install <a href="https://github.com/Netflix/Turbine">Turbine</a> (you can defer this until you reach section 6 in the lab instructions):
    
    ```bash
    $ git clone git@github.com:Netflix/Turbine.git
    $ cd Turbine
    $ ./gradlew build
    ```

To start the todo application either run the main method in `com.jayway.hystrixlab.Boot` from your IDE or run `./gradlew shadowJar && java -jar build/libs/hystrix-lab-1.0-all.jar`.    

Lab Instructions
----------------
The code is an overly simplistic TODO application that stores todos in MongoDB. It has a simple HTTP interface 
(<a href="https://github.com/jayway/lab_hystrix/blob/master/src/main/java/com/jayway/hystrixlab/http/TodoResource.java">TodoResource</a>) and
a repository (<a href="https://github.com/jayway/lab_hystrix/blob/master/src/main/java/com/jayway/hystrixlab/repository/TodoRepository.java">TodoRepository</a>)
for storing and retrieving todos.

1. Add Hystrix as a layer between the `TodoResource` and `TodoRepository` so that the application behaves deterministically when MongoDB is down. 
    Things to answer and think about:
    1. How do you name Hystrix commands and group?
    2. How do you determine what Hystrix should do when MongoDB is down?
    
    After you've implemented the Hystrix layer make sure to run `./gradlew test` to see that the tests are still passing.
2. Now that we have Hystrix protecting the `TodoRepository` we want statistics. Expose the statistics by 
   <a href="https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-metrics-event-stream#installation">installing</a> the 
   <a href="https://github.com/Netflix/Hystrix/tree/master/hystrix-contrib/hystrix-metrics-event-stream">hystrix-metrics-event-stream</a> module to `com.jayway.hystrixlab.Boot` (don't forget to restart the server afterwards).
3. Start the Hystrix Dashboard (if you've not done so already) and point it to your hystrix event stream, for example `http://localhost:8080/hystrix.stream`.
   Use something like RestClient, Postman or "test RESTful Web Service" (if you're using IntelliJ) to send commands to the `TodoResource`. 
   You should see <a href="https://github.com/Netflix/Hystrix/tree/master/hystrix-dashboard#example">graphs</a> popping up in the Hystrix UI.
4. Now it's time to see what happens when you close MongoDB. 
    1. Run the main method in `com.jayway.hystrixlab.repository.RandomTodoCommands`. This will create, delete and find random todos (i.e. simulate all `TodoResource` commands).
    2. Make sure you see some action in the Hystrix Dashboard.
    3. Shutdown MongoDB and see what happens in the Hystrix Dashboard.
    4. After a while turn on MongoDB again and see what happens in the Hystrix Dashboard. Is everything working the same was as before?
5. Now we're going to create small cluster of two todo applications and we going to use nginx as a round robin load balancer to distribute requests
   between our servers.
    1. Start two instance of the todo application on different ports:
        1. `./gradlew clean && ./gradlew shadowJar` 
        2. `java -jar build/libs/hystrix-lab-1.0-all.jar -p 8080`
        3. `java -jar build/libs/hystrix-lab-1.0-all.jar -p 8081`         
    2. Configure nginx to load balance between these two instances (see <a href="http://nginx.org/en/docs/http/load_balancing.html">docs</a>).
    3. Verify that it works!
6. <a href="https://github.com/Netflix/Turbine/wiki/Getting-Started-(1.x)#configure-turbine">Configure</a> a Turbine cluster with the two instances. Don't use anything fancy such as Eureka, settle for the pure config option.
7. Point your Hystrix Dashboard to the Turbine endpoint that should be aggregating the streams and send some commands to the ngnix load balancer. The graph should indicate that the cluster size is 2.
8. Redo what you did in step 4 but this time change so that `com.jayway.hystrixlab.repository.RandomTodoCommands` goes through nginx.
9. Create an alert system that monitors the Turbine stream (same stream that you pointed out in the dashboard) and trigger notifications when a circuit breaker is opened or closed. 
   Preferably use RxJava or if you want to be really fancy use Clojure channels and transducers.