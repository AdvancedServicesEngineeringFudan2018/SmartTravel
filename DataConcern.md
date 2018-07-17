# Data Concern
## Data source
- Vessel data
  - ship number, latitude and longitude information, ship size, ship speed, ship direction, and emergence type.
- Weather data :
  - Weather information about vessel warnings includes temperatures for a certain time period in a region.

## Data and service usage
We collect the data to simulate the vessel-IoT-device by subscribing/publishing services. besides , we abtain the weather information with OpenAPI. 

## Quality of data
Because our data comes from the public free data of the website of chinaports at any time. So the reliability , compleness of the collected data can be promised. Also , all data are real-time and up-to-date.


## Quality of service
With employing AWS IoT, using Quality of Service (QoS) to subscribe to a topic means that the message will be sent zero or more times, ensuring that a reasonable timeout  is set within a certain amount of time.

## Context
The URL for data source :  http://www.chinaports.com

## Reference
