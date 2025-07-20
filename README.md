# Workflow
 
This News PubSub project consists 5 repository with 4 microservices + 1 library:

1. NewsManager: Responsible for extracting news from [NewsAPI.org](https://newsapi.org/) or [NewsData.io](https://newsapi.org/) periodically and broadcasting the news to the Solace Event Broker using direct publishing. Currently, it supports only BBC, CNN, Reuters, The New York Times, and The Economist.
2. NewsSubscriber: Listens to all incoming news and stores them for display on the landing page. This is just for fun, not a big concern.
3. Notifications: The core microservice. It handles user subscriptions and broadcasts relevant news to clients. The logic is as follows: it continuously listens to the broker, and upon receiving a News object, checks whether the source of the news is currently subscribed by any user. If not, it filters the news out; if yes, it publishes the news to the broker’s message queue.
4. SEMP: Primarily used by the frontend’s Header component to display the number of unread messages in the notification bell dropdown (as shown in the demo video).
5. Utils: A shared utility library that provides common structures such as News, error types, API responses, and, most importantly, the Connector class. The Connector encapsulates the core logic and simplest way to interact with the Solace Event Broker API.

## Workflow
![](/Users/ahsoka/Desktop/NewsPubSub Workflow.png)
Note: Checkout comments in the code!!!

## Video

https://drive.google.com/file/d/1MtZMuUqARbkJe2uo9zDpwUd70Ymzu8QC/view?usp=sharing

## Reference:
* Solace Event Broker: https://docs.solace.com/Get-Started/get-started-lp.htm
* Solace Event Broker code samples Java: https://github.com/SolaceSamples/solace-samples-java
* Solace Event Broker code samples JavaScript: https://github.com/SolaceSamples/solace-samples-javascript
* NewsData.io: https://newsdata.io/documentation#latest-news
* NewsApi.org: https://newsapi.org/docs/endpoints/everything