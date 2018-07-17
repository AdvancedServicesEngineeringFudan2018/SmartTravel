## Service Design
we use vessel data from the chinaports website as simple file and simulate the IoT device.The other service’s responsibility is as following ：
- Vessel Management Services：these services are used to manage the real-time state information, such as vessel position, speed, destination. we can acquire the statu of vessel, such as voyaging , anchoring, docking, and when the vessel arrives at the port and departures, how long the vessel will stay . Also, we can call some services to control the navigation, such as start, pause, and stop.
- Emergency Management Services ： these services are used to report emergency information, such as bad weather, arrival delay.

## Business value
Our business focuses on the management of ships by shipping companies.
 - Shipping tracing and scheduling.
 - Predict emergency situations and make adjustment in advance.

## Realizing Business value.
- Shipping Company can obtain real-time state of ship. these include some useful shipping information, such as the location of the item, the port to be reached, and the estimate arrival time.
- Shiping company can control the anchoring and docking duration of ship and when to start, pause, or stop.
- The ship will actively send some asynchronous events while sailing. When the shipping company receives these asynchronous events, it will give feedback control the navigation. These events include some emergencies, For emergencies, we can deal with them, such as delays or adjustments to routes.
