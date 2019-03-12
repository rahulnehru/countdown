# Countdown

## Domain
  - Standup - Stream of work running the standup
  - Team - Team providing update in standup
  - Speaker - Team member speaking in standup
## Endpoints
  #### Standups: ``` GET /standups```
  ##### Code : 200 OK
  ###### Example ```/standups```
```json
[
    "S1",
    "S2"
]
```

  #### A Standup details: ```GET /standups/[stream name]```
  ##### Code : 200 OK
 ###### Example ```/standups/S1```
```json
{
    id: 1,
    name: "S1",
    teams: [
        {
        id: 1,
        name: "Team 1",
        speaker: "Dave",
        allocation: "180 seconds"
        },
        {
        id: 2,
        name: "Team 2",
        speaker: "Tom",
        allocation: "120 seconds"
        }
    ]
}
```
  #### Running a Standup
    
    Websocket connection is obtained before executing commands like start, next, pause, stop, exit etc
  
  ##### To connect: ```GET    /standups/:name/connect```
  ###### Sample response
  ```
  system:	Connection established.
  ```  
  ##### To start: ```start```
    Once standup is started, other clients may connect and enquire standup status or close connection using ```close```
  ###### Sample response
  ```json
  {"name":"Team 1","speaker":"Dave","remaining":"161 seconds"}
  ```
  
  ##### To find status: ```status```
  ###### Sample response
  ```json
  {"name":"Team 1","speaker":"Dave","remaining":"161 seconds"}
  ```

  ##### To pause: ```pause```
  ###### Sample response
  ```json
  {"name":"Team 1","speaker":"Dave","remaining":"161 seconds"}
  ```
  
  ##### To skip to next: ```next```
  ###### Sample response
  ```json
  {"name":"Team 2","speaker":"Tom","remaining":"161 seconds"}
  ```
  ###### If no more team left to update
    ```{"message":"Standup S1 finished"}```
    
  ##### For client to exit : ```exit```
  ###### Sample response
  ```json
  {"message":"Exiting. Standup may already be running"}
  ```
    
  ##### To stop the standup : ```stop```
  ###### Sample response
  ```json
  	{"message":"Standup S1 finished"}
  ```

## Runnning the application

### Prerequisite
* Java 8
* Sbt 1.x

    This is a typical play application which ca either be run from ide in development or using ```sbt run```. 
This application will start on port 9000 and will be accessible through the end points listed above.
    This application is operated using web-sockets command. To test or develop the application, a chrome
plugin ```Dark WebSocket Terminal``` can be used. 

![](dwst.png)
        
