# Quality of Data
## Domain-specific metrics
- Need specific tools and expertise for determining metrics.
  - For ship management  service, we download data from chinaports website and save them as simple file. And considering vessel state and emergency, we choose some main metrics, such as longitude, latitude , speed, direction, emergency type, timeStamp.
  - For weather management service, we callGaode api to get weather infomation. So we choose some main metrics : weather type, location, timeStamp.
## Evaluation
- Cannot done by software only: humans are required
- Exact versus inexact evaluation due to big and streaming data.
  - human is required to evaluate.

## Complex integration model
- Where to put QoD evaluators and why?
- How evaluators obtain the data to be evaluated?

## Impact on performance of data analytics workflows

## Elasticity
