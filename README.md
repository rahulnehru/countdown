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
{
    "streams": ["auk", "ba", "test"]
}
```

  #### A Standup details: ```GET /standups/[stream name]```
  ##### Code : 200 OK
 ###### Example ```/standups/auk```
```json
{
    "team": [
        {name: "team1", "speaker":"tom", allocation: 120 seconds},
        {name: "team2", "speaker":"harry", allocation: 60 seconds}
    ]
}
```
  #### A Standup status: ```GET /standups/[stream name]/?status```
  ##### Code : 200 OK
  Returned ```status``` could be 
  >in progress
  >paused
  ###### Example ```/standups/auk/?status```
```json
{
    name: "team1",
    "speaker":"tom",
    remaining: 20 seconds
    status: "in progress"
}
```
  ##### Code : 410 Gone/Finished
  
  #### Start a standup: ```GET /standups/[stream name]/start```
  ##### Code : 200 OK
  Returned ```status``` will be 
  >in progress
  ###### Example ```/standups/auk/start```
```json
{
    name: "team1",
    "speaker":"tom",
    remaining: 120 seconds
    status: "start"
}
```

  #### Pause a standup update: ```GET /standups/[stream name]/pause```
  ##### Code : 200 OK
  Returned ```status``` will be 
  >paused
  ###### Example ```/standups/auk/pause```
```json
{
    name: "team1",
    "speaker":"tom",
    remaining: 20 seconds
    status: "paused"
}
```
  ##### Code : 410 Gone/Finished

  #### Skip a standup update: ```GET /standups/[stream name]/next```
  ##### Code : 200 OK
  Returned ```status``` will be 
  >in progress
  ###### Example ```/standups/auk/pause```
```json
{
    name: "team2",
    "speaker":"harry",
    remaining: 120 seconds
    status: "in progress"
}
```
  ##### Code : 410 Gone/Finished
