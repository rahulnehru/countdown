# Countdown

## Domain
  - Standup - Stream of work running the standup
  - Team - Team providing update in standup
  - Speaker - Team member speaking in standup
## Endpoints
  #### Standups: ``` GET /standups```
  ##### Code: 200 OK
  > Example ```/standups```
```json
[
    {
      "name": "main",
      "displayName": "Main Standup"
    },
    {
      "name": "test",
      "displayName": "Testing Standup"
    }
]
```
  #### Standups: ``` POST /standups ```
  ##### Body: 
  ```json
 {
  "id": 0,
  "name": "String",
  "displayName": "String",
  "teams": [
    {
      "id": 0,
      "name": "String",
      "speaker": "String",
      "allocationInSeconds": 60
    }, 
    {
      "id": 1,
      "name": "String",
      "speaker": "String",
      "allocationInSeconds": 60
    }
  ]
 } 
  ```
  ##### Code: 201 CREATED
  ##### Code: 409 Standup with that name exists
  ##### Code: 422 Invalid JSON format
  
  #### Standups: ``` PUT /standups ```
```json
{
    "id": 0,
    "name": "String",
    "displayName": "String",
    "teams": [
      {
        "id": 0,
        "name": "String",
        "speaker": "String",
        "allocationInSeconds": 60
      }, 
      {
        "id": 1,
        "name": "String",
        "speaker": "String",
        "allocationInSeconds": 60
      }
    ]
} 
```
   ##### Code: 200 OK
   ##### Code: 404 Standup to edit not found
   ##### Code: 422 Invalid JSON format
   
  #### Standups: ``` DELETE /standups ```
```json
{
    "id": 0,
    "name": "String",
    "displayName": "String",
    "teams": [
      {
        "id": 0,
        "name": "String",
        "speaker": "String",
        "allocationInSeconds": 60
      }, 
      {
        "id": 1,
        "name": "String",
        "speaker": "String",
        "allocationInSeconds": 60
      }
    ]
} 
```
   ##### Code: 200 OK
   ##### Code: 404 Standup to delete not found
   ##### Code: 422 Invalid JSON format
   
  #### A Standup details: ```GET /standups/[stream name]```
  ##### Code : 200 OK
 > Example ```/standups/S1```
```json
{
    "id": 1,
    "name": "S1",
    "teams": [
        {
          "id": 1,
          "name": "Team 1",
          "speaker": "Dave",
          "allocationInSeconds": 10
        },
        {
          "id": 2,
          "name": "Team 2",
          "speaker": "Tom",
          "allocationInSeconds": 10
        }
    ]
}
```
  #### Running a Standup
    
  We have admin and client connection. Admin connection is used to manage the standup while client connection is used to
  enquire standup status.  
    
  ##### Admin
  An admin can 
  * connect: Connect to the standup "instance"
  * join: Join the standup if it is live
  * start: Start the standup if it is not live
  * stop: Stop the standup
  * pause: Pause an update
  * next: Skip the update  
  
  ###### To connect: ```GET    /admin/standups/:name/start```
   
  ###### To join a running standup: ```join```
    
  ###### To start: ```start```
  > Sample response ```{"name":"Team 2","speaker":"Tom","remainingSeconds":7}```
  
  ###### To pause: ```pause```
  > Sample response ```{"name":"Team 2","speaker":"Tom","remainingSeconds":7}```
  
  ###### To skip to next: ```next```
  > Sample response ``` {"name":"Team 2","speaker":"Tom","remainingSeconds":7} ```

  ###### To stop the standup : ```stop```
  > Sample response ``` {"message":"Standup S1 finished"} ```

  ##### Client
  An client can 
  * connect : Connect to the standup in progress to get status update
  * disconnect: Disconnect from the standup in progress

  ##### To obtain status: ```GET    /client/standups/:name/status```
    
  > Sample response ``` {"name":"Team 1","speaker":"Dave","remaining":"161 seconds"} ```
  
  ##### To disconnect: ```disconnect```
  > Sample response ``` {"message":"Disconnecting from S1."} ```    

## Runnning the application

### Prerequisite
* Java 8
* Sbt 1.x

    This is a typical play application which ca either be run from ide in development or using ```sbt run```. 
This application will start on port 9000 and will be accessible through the end points listed above.
    This application is operated using web-sockets command. To test or develop the application, a chrome
plugin ```Dark WebSocket Terminal``` can be used. 

![](dwst.png)